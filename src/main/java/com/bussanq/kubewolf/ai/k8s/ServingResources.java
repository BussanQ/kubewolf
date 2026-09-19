package com.bussanq.kubewolf.ai.k8s;

import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.api.validation.TaskValidation;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.*;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class ServingResources {
    public static final String TASK_ID = "kubewolfResourceId";
    public static final String GENERATION = "bussanq.com/generation";

    public Deployment deployment(ServeTask task) {
        Map<String, String> labels = Map.of("kubewolf", "ServeTask", TASK_ID, task.getTaskId());
        int port = Integer.parseInt(task.getPort());
        Map<String, Quantity> resources = new HashMap<>();
        resources.put("cpu", new Quantity(task.getCpu() + "m"));
        resources.put("memory", new Quantity(task.getMem() + "Mi"));
        if (task.getGpu() != null && task.getGpu() > 0)
            resources.put(task.getGpuResource(), new Quantity(task.getGpu().toString()));
        ProbeBuilder readiness = new ProbeBuilder().withPeriodSeconds(5).withTimeoutSeconds(3).withFailureThreshold(3);
        if ("run".equals(task.getType())) readiness.withNewTcpSocket().withPort(new IntOrString(port)).endTcpSocket();
        else readiness.withNewHttpGet().withPath("/health").withPort(new IntOrString(port)).endHttpGet();
        Probe ready = readiness.build();
        Probe startup = new ProbeBuilder(ready).withPeriodSeconds(10).withFailureThreshold(300).build();
        ContainerBuilder container = new ContainerBuilder().withName("inference").withImage(task.getImage())
                .withImagePullPolicy("IfNotPresent").withReadinessProbe(ready).withStartupProbe(startup)
                .withResources(new ResourceRequirementsBuilder().withRequests(resources).withLimits(resources).build());
        if (task.getCmd() != null && !task.getCmd().isBlank()) container.withCommand("sh", "-c", task.getCmd());
        container.withEnv(TaskValidation.environment(task.getEnv()).entrySet().stream()
                .map(e -> new EnvVar(e.getKey(), e.getValue(), null)).toList());
        List<Volume> volumes = new ArrayList<>();
        if (task.getModelPath() != null && !task.getModelPath().isBlank()) {
            TaskValidation.modelVolume(task.getModelPath(), task.getModelCode());
            container.addNewVolumeMount().withName("model").withMountPath(task.getModelPath()).withReadOnly(true).endVolumeMount();
            volumes.add(new VolumeBuilder().withName("model").withNewPersistentVolumeClaim()
                    .withClaimName(task.getModelCode()).endPersistentVolumeClaim().build());
        }
        return new DeploymentBuilder().withNewMetadata().withName(task.getResourceName()).withNamespace(task.getNamespace())
                .withLabels(labels).addToAnnotations(GENERATION, Long.toString(task.getGeneration())).endMetadata()
                .withNewSpec().withReplicas(task.getReplicas()).withProgressDeadlineSeconds(3600)
                .withNewSelector().withMatchLabels(Map.of(TASK_ID, task.getTaskId())).endSelector()
                .withNewTemplate().withNewMetadata().withLabels(labels).endMetadata()
                .withNewSpec().withContainers(container.build()).withVolumes(volumes).endSpec().endTemplate()
                .endSpec().build();
    }

    public Service service(ServeTask task) {
        return new ServiceBuilder().withNewMetadata().withName(task.getResourceName()).withNamespace(task.getNamespace())
                .addToLabels(TASK_ID, task.getTaskId()).endMetadata()
                .withNewSpec().withType("ClusterIP").withSelector(Map.of(TASK_ID, task.getTaskId()))
                .addNewPort().withName("http").withPort(Integer.parseInt(task.getPort()))
                .withTargetPort(new IntOrString(Integer.parseInt(task.getPort()))).endPort().endSpec().build();
    }
}
