package com.bussanq.kubewolf.ai.service.impl;

import com.bussanq.kubewolf.ai.k8s.*;
import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.common.error.ApiException;
import com.bussanq.kubewolf.common.k8s.lib.K8sService;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
public class KubeWolfServiceImpl implements AIService {
    private final K8sService service;
    private final K8sOperator operator;
    private final ServingResources resources;
    public KubeWolfServiceImpl(K8sService service, K8sOperator operator, ServingResources resources) {
        this.service = service; this.operator = operator; this.resources = resources;
    }
    public boolean ready() { return operator.ready(); }
    private void requireReady() {
        if (!ready()) throw new ApiException(503, "Kubernetes 状态尚未同步，请稍后重试");
    }
    private void owned(HasMetadata resource, ServeTask task) {
        if (resource != null && (resource.getMetadata().getLabels() == null ||
                !task.getTaskId().equals(resource.getMetadata().getLabels().get(ServingResources.TASK_ID))))
            throw new ApiException(409, "同名 Kubernetes 资源不属于当前任务，已停止操作");
    }
    @Override
    public void ensureServing(ServeTask task) {
        requireReady();
        var client = service.requireClient();
        var deployments = client.apps().deployments().inNamespace(task.getNamespace());
        Deployment current = deployments.withName(task.getResourceName()).get();
        owned(current, task);
        if (current == null || current.getMetadata().getAnnotations() == null ||
                !Long.toString(task.getGeneration()).equals(current.getMetadata().getAnnotations().get(ServingResources.GENERATION)))
            deployments.resource(resources.deployment(task)).serverSideApply();
        var services = client.services().inNamespace(task.getNamespace());
        var existing = services.withName(task.getResourceName()).get();
        owned(existing, task);
        if (existing == null || existing.getSpec().getPorts().isEmpty()
                || !Objects.equals(existing.getSpec().getPorts().getFirst().getPort(), Integer.valueOf(task.getPort())))
            services.resource(resources.service(task)).serverSideApply();
    }
    @Override
    public boolean stopServing(ServeTask task) {
        requireReady();
        var client = service.requireClient();
        var deployment = client.apps().deployments().inNamespace(task.getNamespace()).withName(task.getResourceName());
        var endpoint = client.services().inNamespace(task.getNamespace()).withName(task.getResourceName());
        var current = deployment.get();
        var currentService = endpoint.get();
        owned(current, task); owned(currentService, task);
        if (current != null) deployment.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
        if (currentService != null) endpoint.delete();
        return deployment.get() == null && endpoint.get() == null;
    }
    @Override
    public ServingStatus status(ServeTask task) {
        if (!ready()) return new ServingStatus("unknown", "Kubernetes 状态尚未同步");
        Deployment deployment = operator.findByTaskId(task.getTaskId());
        if (deployment == null) return new ServingStatus("pending", "等待创建资源");
        var status = deployment.getStatus();
        if (status == null) return new ServingStatus("pending", "等待调度");
        if (status.getConditions() != null) {
            for (var condition : status.getConditions()) {
                if (("Progressing".equals(condition.getType()) && "False".equals(condition.getStatus())) ||
                        ("ReplicaFailure".equals(condition.getType()) && "True".equals(condition.getStatus())))
                    return new ServingStatus("error", condition.getReason() + ": " + condition.getMessage());
            }
        }
        boolean current = deployment.getMetadata().getAnnotations() != null &&
                Long.toString(task.getGeneration()).equals(deployment.getMetadata().getAnnotations().get(ServingResources.GENERATION));
        boolean observed = status.getObservedGeneration() != null &&
                status.getObservedGeneration() >= deployment.getMetadata().getGeneration();
        if (current && observed && status.getReadyReplicas() != null && status.getUpdatedReplicas() != null
                && status.getReadyReplicas() >= task.getReplicas() && status.getUpdatedReplicas() >= task.getReplicas())
            return new ServingStatus("running", null);
        return new ServingStatus("pending", "等待模型就绪或滚动更新完成");
    }
}
