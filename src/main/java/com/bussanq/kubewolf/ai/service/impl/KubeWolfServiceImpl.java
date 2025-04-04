package com.bussanq.kubewolf.ai.service.impl;

import com.bussanq.kubewolf.ai.k8s.K8sOperator;
import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.model.TaskInfo;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.common.k8s.lib.K8sService;
import com.bussanq.kubewolf.common.utils.DbUtil;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bussanq
 * @date 2024/11/14
 */
@Slf4j
@Service
public class KubeWolfServiceImpl implements AIService {

    @Autowired(required = false)
    K8sService k8sService;
    @Autowired
    K8sOperator k8sOperator;

    @Override
    public TaskInfo startServing(ServeTask task) {
        String name = task.getTaskName();
        task.setTaskName(name.toLowerCase());
        String taskYaml = DbUtil.renderToYaml(task, task.getClass().getSimpleName());
        if (k8sService.apply(taskYaml)) {
            return new TaskInfo();
        } else {
            log.error(taskYaml);
            return null;
        }
    }

    @Override
    public boolean stopServing(ServeTask task) {
        String taskYaml = DbUtil.renderToYaml(task, task.getClass().getSimpleName());
        return k8sService.delete(taskYaml);
    }

    @Override
    public List<TaskInfo> listServing() {
        return k8sOperator.deploymentInformer.getIndexer().list().stream().map(
                this::deployToTaskInfo
        ).collect(Collectors.toList());
    }

    private TaskInfo deployToTaskInfo(Deployment deployment) {
        if (deployment == null) {
            return null;
        }
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskId(deployment.getMetadata().getLabels().get("kubewolfResourceId"));
        taskInfo.setName(deployment.getMetadata().getName());
        Integer readyReplicas = deployment.getStatus().getReadyReplicas();
        if (readyReplicas == null || readyReplicas < deployment.getStatus().getReplicas()) {
            taskInfo.setStatus("pending");
        } else {
            taskInfo.setStatus("running");
        }
        return taskInfo;
    }
}
