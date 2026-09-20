package com.bussanq.kubewolf.api.service;

import com.bussanq.kubewolf.common.k8s.lib.K8sService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ConsoleService {
    private static final BigDecimal GIB = BigDecimal.valueOf(1024L * 1024 * 1024);
    private static final BigDecimal NANO = BigDecimal.valueOf(1_000_000_000L);
    private final K8sService kubernetes;
    private final ModelService models;
    private final ObjectMapper mapper;

    public ConsoleService(K8sService kubernetes, ModelService models, ObjectMapper mapper) {
        this.kubernetes = kubernetes;
        this.models = models;
        this.mapper = mapper;
    }

    @lombok.Value
    public static class Metrics {
        Long imageCount;
        Integer nodes;
        BigDecimal cpu;
        BigDecimal memory;
        BigDecimal memoryUsed;
        BigDecimal cpuRate;
        BigDecimal memRate;
        BigDecimal gpuRate;
    }

    private record Usage(BigDecimal cpu, BigDecimal memory) { }

    public Metrics query() {
        Long modelCount = null;
        try {
            modelCount = models.count();
        } catch (RuntimeException e) {
            log.warn("Console model count unavailable: {}", e.getClass().getSimpleName());
        }
        KubernetesClient client;
        List<Node> nodes;
        try {
            client = kubernetes.requireClient();
            nodes = client.nodes().list().getItems();
        } catch (RuntimeException e) {
            log.warn("Console nodes unavailable: {}", e.getClass().getSimpleName());
            return new Metrics(modelCount, null, null, null, null, null, null, null);
        }
        BigDecimal cpu = capacity(nodes, "cpu");
        BigDecimal memory = capacity(nodes, "memory");
        Map<String, NodeMetrics> metrics = nodeMetrics(client);
        BigDecimal cpuUsed = BigDecimal.ZERO;
        BigDecimal memoryUsed = BigDecimal.ZERO;
        for (Node node : nodes) {
            String name = node.getMetadata().getName();
            NodeMetrics metric = metrics.get(name);
            Usage usage = metric != null && fresh(metric.getTimestamp())
                    ? new Usage(quantity(metric.getUsage(), "cpu"), quantity(metric.getUsage(), "memory"))
                    : new Usage(null, null);
            if (usage.cpu() == null || usage.memory() == null) {
                Usage fallback = nodeStats(client, name);
                usage = new Usage(usage.cpu() == null ? fallback.cpu() : usage.cpu(),
                        usage.memory() == null ? fallback.memory() : usage.memory());
            }
            cpuUsed = add(cpuUsed, usage.cpu());
            memoryUsed = add(memoryUsed, usage.memory());
        }
        if (nodes.isEmpty()) {
            cpuUsed = null;
            memoryUsed = null;
        }
        // GPU busy time is not exposed by the standard resource Metrics API.
        // Never substitute GPU allocation/request counts for actual utilization.
        return new Metrics(modelCount, nodes.size(), cpu, gib(memory), gib(memoryUsed),
                ratio(cpuUsed, cpu), ratio(memoryUsed, memory), null);
    }

    private Map<String, NodeMetrics> nodeMetrics(KubernetesClient client) {
        Map<String, NodeMetrics> result = new HashMap<>();
        try {
            for (NodeMetrics metric : client.top().nodes().metrics().getItems()) {
                if (metric.getMetadata() != null) result.put(metric.getMetadata().getName(), metric);
            }
        } catch (RuntimeException e) {
            log.debug("Console Metrics API unavailable; using node statistics: {}", e.getClass().getSimpleName());
        }
        return result;
    }

    private Usage nodeStats(KubernetesClient client, String name) {
        try {
            JsonNode node = mapper.readTree(client.raw("/api/v1/nodes/" + name + "/proxy/stats/summary")).path("node");
            JsonNode cpu = node.path("cpu");
            JsonNode memory = node.path("memory");
            BigDecimal nanoCores = fresh(cpu.path("time").asText(null)) ? number(cpu.get("usageNanoCores")) : null;
            BigDecimal bytes = fresh(memory.path("time").asText(null)) ? number(memory.get("workingSetBytes")) : null;
            return new Usage(nanoCores == null ? null : nanoCores.divide(NANO), bytes);
        } catch (Exception e) {
            log.debug("Console node statistics unavailable for {}: {}", name, e.getClass().getSimpleName());
            return new Usage(null, null);
        }
    }

    private static boolean fresh(String timestamp) {
        try {
            Instant time = Instant.parse(timestamp);
            Instant now = Instant.now();
            return !time.isBefore(now.minusSeconds(300)) && !time.isAfter(now.plusSeconds(60));
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static BigDecimal capacity(List<Node> nodes, String resource) {
        BigDecimal total = BigDecimal.ZERO;
        for (Node node : nodes) {
            total = add(total, node.getStatus() == null ? null : quantity(node.getStatus().getCapacity(), resource));
        }
        return total;
    }

    private static BigDecimal quantity(Map<String, Quantity> values, String resource) {
        if (values == null || values.get(resource) == null) return null;
        try {
            BigDecimal value = values.get(resource).getNumericalAmount();
            return value.signum() < 0 ? null : value;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static BigDecimal number(JsonNode value) {
        return value != null && value.isNumber() && value.decimalValue().signum() >= 0 ? value.decimalValue() : null;
    }

    private static BigDecimal add(BigDecimal total, BigDecimal value) {
        return total == null || value == null ? null : total.add(value);
    }

    private static BigDecimal gib(BigDecimal bytes) {
        return bytes == null ? null : bytes.divide(GIB, 3, RoundingMode.HALF_UP);
    }

    private static BigDecimal ratio(BigDecimal used, BigDecimal capacity) {
        return used == null || capacity == null || capacity.signum() <= 0 ? null
                : used.divide(capacity, 6, RoundingMode.HALF_UP);
    }
}
