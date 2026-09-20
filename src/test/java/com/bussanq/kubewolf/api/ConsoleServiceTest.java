package com.bussanq.kubewolf.api;

import com.bussanq.kubewolf.api.service.ConsoleService;
import com.bussanq.kubewolf.api.service.ModelService;
import com.bussanq.kubewolf.common.error.ApiException;
import com.bussanq.kubewolf.common.k8s.lib.K8sService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeBuilder;
import io.fabric8.kubernetes.api.model.NodeListBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetricsBuilder;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetricsListBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsoleServiceTest {
    private final KubernetesClient client = mock(KubernetesClient.class, RETURNS_DEEP_STUBS);
    private final K8sService kubernetes = mock(K8sService.class);
    private final ModelService models = mock(ModelService.class);
    private final ConsoleService service = new ConsoleService(kubernetes, models, new ObjectMapper());

    ConsoleServiceTest() {
        when(kubernetes.requireClient()).thenReturn(client);
        when(models.count()).thenReturn(7L);
    }

    @Test
    void aggregatesClusterCapacityAndWeightedUsageWithKubernetesUnits() {
        nodes(node("a", "4", "4Gi"), node("b", "6000m", "6291456Ki"));
        metrics(metric("a", "250m", "1Gi"), metric("b", "750000000n", "2048Mi"), metric("deleted", "100", "100Gi"));
        var result = service.query();
        assertThat(result.getImageCount()).isEqualTo(7);
        assertThat(result.getNodes()).isEqualTo(2);
        assertThat(result.getCpu()).isEqualByComparingTo("10");
        assertThat(result.getMemory()).isEqualByComparingTo("10");
        assertThat(result.getMemoryUsed()).isEqualByComparingTo("3");
        assertThat(result.getCpuRate()).isEqualByComparingTo("0.1");
        assertThat(result.getMemRate()).isEqualByComparingTo("0.3");
        assertThat(result.getGpuRate()).isNull();
        verify(client, never()).raw(anyString());
    }

    @Test
    void fallsBackToNodeStatisticsAndKeepsZeroAsRealData() {
        nodes(node("a", "4", "2Gi"));
        when(client.top().nodes().metrics()).thenThrow(new KubernetesClientException("metrics unavailable"));
        when(client.raw("/api/v1/nodes/a/proxy/stats/summary")).thenReturn(stats("0", "0", Instant.now()));
        var result = service.query();
        assertThat(result.getCpuRate()).isEqualByComparingTo("0");
        assertThat(result.getMemRate()).isEqualByComparingTo("0");
        assertThat(result.getMemoryUsed()).isEqualByComparingTo("0");
    }

    @Test
    void unavailableNodeDoesNotProduceAMisleadingPartialUsageTotal() {
        nodes(node("a", "4", "2Gi"), node("b", "4", "2Gi"));
        metrics(metric("a", "1", "1Gi"));
        when(client.raw("/api/v1/nodes/b/proxy/stats/summary")).thenThrow(new KubernetesClientException("forbidden"));
        var result = service.query();
        assertThat(result.getNodes()).isEqualTo(2);
        assertThat(result.getCpu()).isEqualByComparingTo("8");
        assertThat(result.getMemory()).isEqualByComparingTo("4");
        assertThat(result.getCpuRate()).isNull();
        assertThat(result.getMemRate()).isNull();
        assertThat(result.getMemoryUsed()).isNull();
    }

    @Test
    void rejectsStaleMetricsAndFallsBackOnlyForTheAffectedNode() {
        nodes(node("a", "2", "2Gi"), node("b", "2", "2Gi"));
        NodeMetrics stale = metric("a", "2", "2Gi");
        stale.setTimestamp(Instant.now().minusSeconds(600).toString());
        metrics(stale, metric("b", "500m", "1Gi"));
        when(client.raw("/api/v1/nodes/a/proxy/stats/summary")).thenReturn(stats("500000000", "1073741824", Instant.now()));
        var result = service.query();
        assertThat(result.getCpuRate()).isEqualByComparingTo("0.25");
        assertThat(result.getMemRate()).isEqualByComparingTo("0.5");
        verify(client, never()).raw("/api/v1/nodes/b/proxy/stats/summary");
    }

    @Test
    void rejectsStaleFallbackStatistics() {
        nodes(node("a", "2", "2Gi"));
        metrics();
        when(client.raw("/api/v1/nodes/a/proxy/stats/summary")).thenReturn(stats("0", "0", Instant.now().minusSeconds(600)));
        assertThat(service.query().getCpuRate()).isNull();
    }

    @Test
    void preservesModelCountWhenKubernetesIsUnavailable() {
        when(kubernetes.requireClient()).thenThrow(new ApiException(503, "unavailable"));
        var result = service.query();
        assertThat(result.getImageCount()).isEqualTo(7);
        assertThat(result.getNodes()).isNull();
        assertThat(result.getCpu()).isNull();
        assertThat(result.getMemory()).isNull();
        assertThat(result.getCpuRate()).isNull();
    }

    @Test
    void emptyClusterAndDatabaseFailureRemainDistinctFromFakeUtilization() {
        when(models.count()).thenThrow(new IllegalStateException("database unavailable"));
        nodes();
        metrics();
        var result = service.query();
        assertThat(result.getImageCount()).isNull();
        assertThat(result.getNodes()).isZero();
        assertThat(result.getCpu()).isEqualByComparingTo("0");
        assertThat(result.getCpuRate()).isNull();
        assertThat(result.getMemRate()).isNull();
    }

    private void nodes(Node... nodes) {
        when(client.nodes().list()).thenReturn(new NodeListBuilder().withItems(nodes).build());
    }

    private void metrics(NodeMetrics... metrics) {
        when(client.top().nodes().metrics()).thenReturn(new NodeMetricsListBuilder().withItems(metrics).build());
    }

    private static Node node(String name, String cpu, String memory) {
        return new NodeBuilder().withNewMetadata().withName(name).endMetadata().withNewStatus()
                .withCapacity(Map.of("cpu", new Quantity(cpu), "memory", new Quantity(memory)))
                .endStatus().build();
    }

    private static NodeMetrics metric(String name, String cpu, String memory) {
        return new NodeMetricsBuilder().withNewMetadata().withName(name).endMetadata()
                .withTimestamp(Instant.now().toString())
                .withUsage(Map.of("cpu", new Quantity(cpu), "memory", new Quantity(memory))).build();
    }

    private static String stats(String cpu, String memory, Instant timestamp) {
        return """
                {"node":{"cpu":{"time":"%s","usageNanoCores":%s},
                         "memory":{"time":"%s","workingSetBytes":%s}}}
                """.formatted(timestamp, cpu, timestamp, memory);
    }
}
