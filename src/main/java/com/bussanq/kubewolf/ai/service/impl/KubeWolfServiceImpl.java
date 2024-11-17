package com.bussanq.kubewolf.ai.service.impl;

import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.model.TaskInfo;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.common.utils.DbUtil;
import com.bussanq.kubewolf.common.k8s.lib.K8sService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bussanq
 * @date 2024/11/14
 */
@Slf4j
@Service
public class KubeWolfServiceImpl implements AIService {

    @Autowired(required = false)
    K8sService k8sService;

    @Override
    public TaskInfo startServingTask (ServeTask task) {
        String taskYaml = DbUtil.renderToYaml(task, task.getClass().getSimpleName());
        if (k8sService.apply(taskYaml)) {
            return new TaskInfo();
        }else {
            log.error(taskYaml);
            return null;
        }
    }
}
