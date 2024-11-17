package com.bussanq.kubewolf.api.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.model.TaskInfo;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.common.utils.DbUtil;
import com.bussanq.kubewolf.web.model.vo.ServeTaskReq;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Slf4j
@Service
public class ServeService {

    @Autowired
    AIService aiService;

    public boolean save(ServeTask serveTask) {
        ServeTask old = findByName(serveTask.getTaskName());
        if (old != null) {
            throw new RuntimeException("保存失败，服务名称重复");
        }
        serveTask.setTaskId(IdUtil.fastSimpleUUID());
        Date now = new Date();
        serveTask.setCreateTime(now);
        serveTask.setUpdateTime(now);
        if(StrUtil.isBlank(serveTask.getType())) {
            serveTask.setType("run");
        }
        if (serveTask.getReplicas() == null || serveTask.getReplicas() == 0) {
            serveTask.setReplicas(1);
        }
        return serveTask.save();
    }

    public boolean update(ServeTask serveTask) {
        ServeTask old = findByName(serveTask.getTaskName());
        if (old != null && !old.getTaskId().equals(serveTask.getTaskId())) {
            throw new RuntimeException("修改失败，服务名称重复");
        }
        serveTask.setUpdateTime(new Date());
        return serveTask.update();
    }

    public boolean delete(String taskId){
        return ServeTask.dao.deleteById(taskId);
    }

    public boolean delete(ServeTaskReq taskReq){
        return StrUtil.isNotBlank(taskReq.getTaskId())?delete(taskReq.getTaskId()):false ;
    }

    public boolean deleteByName(String taskName){
        return Db.template("deleteByTaskName", taskName).update() > 0;
    }

    public boolean stop(ServeTaskReq taskReq) {
        ServeTask serveTask = findByIdOrName(taskReq);
        if (serveTask == null) {
            throw new RuntimeException("未找到服务");
        }
        return aiService.stopServing(serveTask);
    }

    public Page<ServeTask> list(ServeTask serveTask, Page page) {
        Kv condition = Kv.by("task_name=", serveTask.getTaskName()).set("type=", serveTask.getType());
        Page<ServeTask> datas = DbUtil.searchPage(ServeTask.dao, condition, page);
        return datas;
    }

    public boolean start(ServeTaskReq taskReq) {
        ServeTask serveTask = findByIdOrName(taskReq);
        if (serveTask == null) {
            throw new RuntimeException("未找到服务");
        }
        TaskInfo taskInfo = aiService.startServing(serveTask);
        return taskInfo != null;
    }

    public ServeTask findByIdOrName(ServeTaskReq taskReq) {
        return StrUtil.isNotBlank(taskReq.getTaskId()) ? findById(taskReq.getTaskId()):findByName(taskReq.getTaskName());
    }

    public ServeTask findById(String taskId) {
        return ServeTask.dao.findById(taskId);
    }

    public ServeTask findByName(String taskName) {
        if (StrUtil.isNotBlank(taskName)) {
            return DbUtil.searchFirst(ServeTask.dao, Kv.by("task_name=", taskName));
        }
        return null;
    }
}
