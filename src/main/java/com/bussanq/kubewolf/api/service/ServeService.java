package com.bussanq.kubewolf.api.service;

import cn.hutool.core.util.StrUtil;
import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.model.TaskInfo;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.common.utils.DbUtil;
import com.bussanq.kubewolf.web.model.vo.ServeTaskReq;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Slf4j
@Service
public class ServeService {

    @Autowired
    AIService aiService;

    public Page<ServeTask> list(ServeTask serveTask, Page page) {
        Kv condition = Kv.by("task_name=", serveTask.getTaskName()).set("type=", serveTask.getType());
        Page<ServeTask> datas = DbUtil.searchPage(ServeTask.dao, condition, page);
        return datas;
    }

    public boolean start(ServeTaskReq taskReq) {
        ServeTask serveTask = findByIdOrName(taskReq);
        TaskInfo taskInfo = aiService.startServingTask(serveTask);
        if (taskInfo == null) {
            return false;
        } else {
            return true;
        }
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
