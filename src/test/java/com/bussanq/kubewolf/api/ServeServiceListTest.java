package com.bussanq.kubewolf.api;

import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.api.service.*;
import com.bussanq.kubewolf.common.k8s.lib.K8sProperties;
import com.bussanq.kubewolf.web.model.vo.PageQuery;
import com.jfinal.plugin.activerecord.Page;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServeServiceListTest {
    @Test void doesNotAdvertiseRunningBeforeGatewaySynchronization() {
        TaskRepository repository = mock(TaskRepository.class);
        AIService ai = mock(AIService.class);
        ServeTask task = mock(ServeTask.class);
        when(task.getDesiredState()).thenReturn("running");
        when(task.getActualStatus()).thenReturn("pending");
        PageQuery query = new PageQuery();
        when(repository.page(query, null, null)).thenReturn(new Page<>(List.of(task), 1, 10, 1, 1));
        when(ai.status(task)).thenReturn(new AIService.ServingStatus("running", null));
        new ServeService(repository, mock(FrameWorkService.class), ai, new K8sProperties()).list(query, null, null);
        verify(task, never()).setStatus("running");
    }

    @Test void resolvesOnlyCurrentPageAndPreservesSynchronizationErrors() {
        TaskRepository repository = mock(TaskRepository.class);
        AIService ai = mock(AIService.class);
        ServeTask running = mock(ServeTask.class), failed = mock(ServeTask.class), stopped = mock(ServeTask.class);
        when(running.getDesiredState()).thenReturn("running");
        when(failed.getDesiredState()).thenReturn("running");
        when(failed.getActualStatus()).thenReturn("error");
        when(stopped.getDesiredState()).thenReturn("stopped");
        PageQuery query = new PageQuery();
        Page<ServeTask> page = new Page<>(List.of(running, failed, stopped), 1, 3, 100, 300);
        when(repository.page(query, null, null)).thenReturn(page);
        when(ai.status(running)).thenReturn(new AIService.ServingStatus("pending", null));
        ServeService service = new ServeService(repository, mock(FrameWorkService.class), ai, new K8sProperties());
        assertSame(page, service.list(query, null, null));
        verify(ai).status(running);
        verifyNoMoreInteractions(ai);
        verify(running).setStatus("pending");
        verify(failed, never()).setStatus(any());
    }
}
