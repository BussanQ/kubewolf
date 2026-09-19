package com.bussanq.kubewolf.ai;

import com.bussanq.kubewolf.ai.k8s.ServingResources;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServingResourcesTest {
    @Test void buildsSafeNamespacedResourcesWithReadinessAndReplicas() {
        ServeTask task = mock(ServeTask.class);
        when(task.getTaskId()).thenReturn("id");when(task.getNamespace()).thenReturn("test-ns");
        when(task.getResourceName()).thenReturn("kw-id");when(task.getPort()).thenReturn("8080");
        when(task.getType()).thenReturn("vllm");when(task.getImage()).thenReturn("vllm:test");
        when(task.getCpu()).thenReturn(500);when(task.getMem()).thenReturn(1024);when(task.getGpu()).thenReturn(1);
        when(task.getGpuResource()).thenReturn("nvidia.com/gpu");when(task.getReplicas()).thenReturn(3);
        when(task.getModelPath()).thenReturn("/model");when(task.getModelCode()).thenReturn("model-pvc");
        when(task.getEnv()).thenReturn("TOKEN:x\nkind: Service");when(task.getGeneration()).thenReturn(4L);
        var builder = new ServingResources();
        var deployment = builder.deployment(task);
        assertEquals(3,deployment.getSpec().getReplicas());
        assertEquals("test-ns",deployment.getMetadata().getNamespace());
        var container = deployment.getSpec().getTemplate().getSpec().getContainers().getFirst();
        assertEquals("/health",container.getReadinessProbe().getHttpGet().getPath());
        assertNotNull(container.getStartupProbe());
        assertEquals("x\nkind: Service",container.getEnv().getFirst().getValue());
        assertEquals("model-pvc",deployment.getSpec().getTemplate().getSpec().getVolumes().getFirst().getPersistentVolumeClaim().getClaimName());
        assertEquals("test-ns",builder.service(task).getMetadata().getNamespace());
    }
}
