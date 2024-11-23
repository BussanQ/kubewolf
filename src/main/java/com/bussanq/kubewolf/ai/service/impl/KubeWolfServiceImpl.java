package com.bussanq.kubewolf.ai.service.impl;

import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.model.TaskInfo;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.common.utils.DbUtil;
import com.bussanq.kubewolf.common.k8s.lib.K8sService;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${k8s.namespace}")
    String namespace;

    @Autowired(required = false)
    K8sService k8sService;

    private SharedIndexInformer<Deployment> deploymentInformer;

    @PostConstruct
    private void init() {
        deploymentInformer = k8sService.getClient().apps().deployments().inNamespace(namespace).
                withLabel("kubewolf", "ServeTask").inform(new ResourceEventHandler<>() {
                    @Override
                    public void onAdd(io.fabric8.kubernetes.api.model.apps.Deployment deployment) {

                    }

                    @Override
                    public void onUpdate(io.fabric8.kubernetes.api.model.apps.Deployment deployment, io.fabric8.kubernetes.api.model.apps.Deployment t1) {

                    }

                    @Override
                    public void onDelete(io.fabric8.kubernetes.api.model.apps.Deployment deployment, boolean b) {

                    }
                }, 180 * 1000L);
    }

    @Override
    public TaskInfo startServing(ServeTask task) {
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
        return deploymentInformer.getIndexer().list().stream().map(
                this::deployToTaskInfo
        ).collect(Collectors.toList());
    }

    private TaskInfo deployToTaskInfo(Deployment deployment) {
        if (deployment == null) {
            return null;
        }
        TaskInfo taskInfo = new TaskInfo();
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
