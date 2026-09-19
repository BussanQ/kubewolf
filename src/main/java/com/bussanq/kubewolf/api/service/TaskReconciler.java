package com.bussanq.kubewolf.api.service;

import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.common.error.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskReconciler {
    private final TaskRepository repository;
    private final AIService ai;
    private final GatewayService gateway;
    public TaskReconciler(TaskRepository repository, AIService ai, GatewayService gateway) {
        this.repository = repository; this.ai = ai; this.gateway = gateway;
    }
    @Scheduled(initialDelayString = "${serving.reconcile-delay-ms:5000}", fixedDelayString = "${serving.reconcile-delay-ms:5000}")
    public void reconcileDue() {
        for (ServeTask task : repository.due()) {
            try { repository.reconcileLocked(task.getTaskId(), this::reconcile); }
            catch (RuntimeException e) {
                log.warn("Task {} synchronization deferred: {}", task.getTaskId(), e.getClass().getSimpleName());
            }
        }
    }
    public void reconcile(ServeTask task) {
        try {
            if (!ai.ready()) throw new ApiException(503, "Kubernetes 状态尚未同步");
            if ("running".equals(task.getDesiredState())) {
                ai.ensureServing(task);
                var state = ai.status(task);
                Integer channelId = task.getGatewayChannelId();
                if ("running".equals(state.state())) channelId = gateway.ensureRoute(task);
                else if (task.isGatewayRegistered()) { gateway.removeRoute(task); channelId = null; }
                repository.observed(task, state.state(), state.detail(), 0, 15, channelId);
            } else {
                RuntimeException gatewayError = null;
                try { gateway.removeRoute(task); } catch (RuntimeException e) { gatewayError = e; }
                boolean removed = ai.stopServing(task); // Free compute even when the gateway is down.
                if (gatewayError != null) throw gatewayError;
                if (removed && "deleted".equals(task.getDesiredState())) repository.deleteCompleted(task);
                else repository.observed(task, removed ? "stopped" : "stopping", null, 0, removed ? 300 : 5, null);
            }
        } catch (Exception e) {
            int attempts = Math.min(task.getRetryCount() + 1, 30);
            long delay = Math.min(300, 5L << Math.min(attempts - 1, 6));
            String message = e instanceof ApiException ? e.getMessage() : "资源同步失败: " + e.getClass().getSimpleName();
            log.warn("Task {} reconciliation failed: {}", task.getTaskId(), message);
            repository.observed(task, "error", message, attempts, delay, task.getGatewayChannelId());
        }
    }
}
