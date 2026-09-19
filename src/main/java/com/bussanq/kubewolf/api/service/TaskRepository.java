package com.bussanq.kubewolf.api.service;

import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.web.model.vo.PageQuery;
import com.bussanq.kubewolf.common.error.ApiException;
import com.jfinal.plugin.activerecord.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.function.Consumer;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@DependsOn("activeRecordPlugin")
public class TaskRepository {
    private final DataSource dataSource;
    public TaskRepository(DataSource dataSource) { this.dataSource = dataSource; }

    // A session lock serializes external side effects across application replicas.
    // The database releases the lock automatically if this process disconnects.
    public void reconcileLocked(String id, Consumer<ServeTask> action) {
        String lock = "kubewolf:" + id;
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select get_lock(?, 0)")) {
                statement.setString(1, lock);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next() || result.getInt(1) != 1) return;
                }
            }
            try {
                ServeTask current = ServeTask.dao.findFirst("select * from serve_task where task_id=? and next_retry_at<=now()", id);
                if (current != null && Set.of("running", "stopped", "deleted").contains(current.getDesiredState()))
                    action.accept(current);
            } finally {
                try (PreparedStatement statement = connection.prepareStatement("select release_lock(?)")) {
                    statement.setString(1, lock);
                    statement.execute();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("任务同步锁不可用", e);
        }
    }
    public ServeTask find(String id) { return ServeTask.dao.findById(id); }
    public ServeTask byName(String name) { return ServeTask.dao.findFirst("select * from serve_task where task_name=?", name); }
    public Page<ServeTask> page(PageQuery page, String name, String type) {
        List<Object> values = new ArrayList<>();
        String sql = "from serve_task where 1=1";
        if (name != null && !name.isBlank()) { sql += " and task_name=?"; values.add(name); }
        if (type != null && !type.isBlank()) { sql += " and type=?"; values.add(type); }
        return ServeTask.dao.paginate(page.getPageNum(), page.getPageSize(), "select *", sql + " order by create_time desc, task_id", values.toArray());
    }
    public void insert(ServeTask task) {
        try { if (!task.save()) throw new ApiException(500, "保存任务失败"); }
        catch (ActiveRecordException e) { throw translate(e); }
    }
    public static RuntimeException translate(ActiveRecordException e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause())
            if (cause instanceof java.sql.SQLException sql && sql.getErrorCode() == 1062)
                return new ApiException(409, "服务名称已存在");
        return e;
    }
    public void update(ServeTask task) {
        try {
            task.update();
            Db.update("update serve_task set next_retry_at=now() where task_id=?", task.getTaskId());
        } catch (ActiveRecordException e) { throw translate(e); }
    }
    public List<ServeTask> due() {
        return ServeTask.dao.find("select * from serve_task where next_retry_at<=now() and desired_state in ('running','stopped','deleted') order by next_retry_at, task_id limit 50");
    }
    public void intent(ServeTask task, String desired) {
        int changed = Db.update("update serve_task set desired_state=?, actual_status=?, generation=generation+1, retry_count=0, last_error=null, next_retry_at=now(), namespace=?, resource_name=? where task_id=? and generation=?",
                desired, desired.equals("running") ? "pending" : desired.equals("deleted") ? "deleting" : "stopping",
                task.getNamespace(), task.getResourceName(), task.getTaskId(), task.getGeneration());
        if (changed == 0) throw new ApiException(409, "任务已变更，请刷新后重试");
    }
    public void observed(ServeTask task, String status, String error, int retries, long delaySeconds, Integer channelId) {
        String message = error == null ? null : error.substring(0, Math.min(1000, error.length()));
        Db.update("update serve_task set actual_status=?, last_error=?, retry_count=?, next_retry_at=date_add(now(), interval ? second), last_sync_time=now(), gateway_registered=?, gateway_channel_id=? where task_id=? and generation=?",
                status, message, retries, delaySeconds,
                channelId != null, channelId, task.getTaskId(), task.getGeneration());
    }
    public void deleteCompleted(ServeTask task) {
        Db.update("delete from serve_task where task_id=? and generation=? and desired_state='deleted'", task.getTaskId(), task.getGeneration());
    }
}
