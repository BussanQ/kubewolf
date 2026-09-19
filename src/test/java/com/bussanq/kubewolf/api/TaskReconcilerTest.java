package com.bussanq.kubewolf.api;

import com.bussanq.kubewolf.api.service.*;
import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.common.error.ApiException;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;

class TaskReconcilerTest {
    TaskRepository repository; AIService ai; GatewayService gateway; ServeTask task; TaskReconciler reconciler;
    @BeforeEach void setup() {
        repository=mock(TaskRepository.class);ai=mock(AIService.class);gateway=mock(GatewayService.class);task=mock(ServeTask.class);
        when(task.getTaskId()).thenReturn("id");when(ai.ready()).thenReturn(true);
        when(task.getGatewayChannelId()).thenReturn(null);
        reconciler=new TaskReconciler(repository,ai,gateway);
    }
    @Test void neverRegistersAnUnreadyModel() {
        when(task.getDesiredState()).thenReturn("running");
        when(ai.status(task)).thenReturn(new AIService.ServingStatus("pending","loading"));
        reconciler.reconcile(task);
        verify(gateway,never()).ensureRoute(any());
        verify(repository).observed(task,"pending","loading",0,15,null);
    }
    @Test void retriesGatewayFailureWithoutMarkingRunning() {
        when(task.getDesiredState()).thenReturn("running");
        when(ai.status(task)).thenReturn(new AIService.ServingStatus("running",null));
        when(gateway.ensureRoute(task)).thenThrow(new ApiException(503,"unavailable"));
        reconciler.reconcile(task);
        verify(repository).observed(task,"error","unavailable",1,5,null);
    }
    @Test void freesComputeButRetainsRecordWhenGatewayCleanupFails() {
        when(task.getDesiredState()).thenReturn("deleted");
        doThrow(new ApiException(503,"unavailable")).when(gateway).removeRoute(task);
        when(ai.stopServing(task)).thenReturn(true);
        reconciler.reconcile(task);
        verify(ai).stopServing(task);verify(repository,never()).deleteCompleted(any());
    }
    @Test void waitsForResourceDeletionBeforeDeletingRecord() {
        when(task.getDesiredState()).thenReturn("deleted");
        when(ai.stopServing(task)).thenReturn(false,true);
        reconciler.reconcile(task);verify(repository,never()).deleteCompleted(any());
        reconciler.reconcile(task);verify(repository).deleteCompleted(task);
    }
    @Test void unavailableClusterDoesNotBecomeStopped() {
        when(ai.ready()).thenReturn(false);when(task.getDesiredState()).thenReturn("stopped");
        reconciler.reconcile(task);
        verify(ai,never()).stopServing(any());
        verify(repository).observed(eq(task),eq("error"),anyString(),eq(1),eq(5L),isNull());
    }
}
