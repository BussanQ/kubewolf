package com.bussanq.kubewolf.api.service;

import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.model.TaskInfo;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.common.utils.DbUtil;
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

    public Page<ServeTask> listServeTasks(ServeTask serveTask, Page page) {
        Kv condition = Kv.by("task_name=", serveTask.getTaskName()).set("type=", serveTask.getType());
        Page<ServeTask> datas = DbUtil.searchPage(ServeTask.dao, condition, page);
        return datas;
    }

    public boolean startTask(ServeTask task) {
        TaskInfo taskInfo = aiService.startServingTask(task);
        if (taskInfo == null) {
            return false;
        }else {
            return true;
        }
    }

}
