package com.bussanq.kubewolf.ai.k8s;

import com.bussanq.kubewolf.common.k8s.lib.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.*;

@Slf4j
@Service
public class K8sOperator {
    private final K8sService service;
    private final K8sProperties properties;
    private volatile SharedIndexInformer<Deployment> informer;

    public K8sOperator(K8sService service, K8sProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 15000)
    public synchronized void ensureStarted() {
        if (!properties.isEnabled() || (informer != null && informer.isRunning())) return;
        service.init(properties);
        if (service.getClient() == null) return;
        try {
            if (informer != null) informer.close();
            informer = service.getClient().apps().deployments().inNamespace(properties.getNamespace())
                    .withLabel("kubewolf", "ServeTask").runnableInformer(30000);
            informer.addIndexers(Map.of("taskId", deployment -> {
                Map<String, String> labels = deployment.getMetadata().getLabels();
                String id = labels == null ? null : labels.get("kubewolfResourceId");
                return id == null ? List.of() : List.of(id);
            }));
            informer.exceptionHandler((started, error) -> {
                log.warn("Kubernetes watch retry: {}", error.getClass().getSimpleName());
                return true;
            });
            informer.start(); // Never block Spring startup on a remote cluster.
        } catch (Exception e) {
            log.warn("Unable to start Kubernetes watch: {}", e.getClass().getSimpleName());
        }
    }

    public boolean ready() {
        var current = informer;
        return current != null && current.hasSynced() && current.isWatching();
    }

    public Deployment findByTaskId(String id) {
        var current = informer;
        if (current == null) return null;
        List<Deployment> matches = current.getIndexer().byIndex("taskId", id);
        return matches.isEmpty() ? null : matches.getFirst();
    }

    @PreDestroy
    public void close() {
        if (informer != null) informer.close();
    }
}
