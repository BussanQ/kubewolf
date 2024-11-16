package com.bussanq.kubewolf.api.service;

import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.model.TaskInfo;
import com.bussanq.kubewolf.api.model.dto.ServingTask;
import com.bussanq.kubewolf.common.utils.DbUtil;
import com.bussanq.kubewolf.k8s.lib.K8sService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Slf4j
@Service
public class ServingService {

    @Autowired
    AIService aiService;

    public boolean startTask(ServingTask task) {
        TaskInfo taskInfo = aiService.startServingTask(task);
        if (taskInfo == null) {
            return false;
        }else {
            return true;
        }
    }

}
