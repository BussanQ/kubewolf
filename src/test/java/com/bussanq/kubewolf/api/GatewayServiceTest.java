package com.bussanq.kubewolf.api;

import com.alibaba.fastjson.JSON;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.api.service.GatewayService;
import com.bussanq.kubewolf.common.error.ApiException;
import com.bussanq.kubewolf.common.utils.HttpKit;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GatewayServiceTest {
    @Test void retriesDiscoverExistingChannelInsteadOfDuplicatingIt() {
        var http=mock(HttpKit.class);var task=mock(ServeTask.class);
        when(task.getResourceName()).thenReturn("kw-id");when(task.getNamespace()).thenReturn("ns");
        when(task.getTaskName()).thenReturn("model-api");when(task.getModelName()).thenReturn("model");when(task.getPort()).thenReturn("8080");
        when(http.request(eq("GET"),contains("?p=0"),anyString(),isNull(),eq(false)))
                .thenReturn(JSON.parseObject("{\"success\":true,\"data\":[{\"id\":7,\"name\":\"kw-id\"}]}"));
        var gateway=new GatewayService(http,"http://gateway","token",true);
        assertEquals(7,gateway.ensureRoute(task));
        verify(http,never()).request(eq("POST"),anyString(),anyString(),any(),anyBoolean());
        verify(http).request(eq("PUT"),anyString(),anyString(),any(),eq(false));
    }
    @Test void followsAllPagesEvenIfServerUsesASmallerPageSize() {
        var http=mock(HttpKit.class);var task=mock(ServeTask.class);
        when(task.getResourceName()).thenReturn("kw-id");
        when(http.request(eq("GET"),contains("?p=0"),anyString(),isNull(),eq(false)))
                .thenReturn(JSON.parseObject("{\"success\":true,\"data\":[{\"id\":1,\"name\":\"other\"}]}"));
        when(http.request(eq("GET"),contains("?p=1"),anyString(),isNull(),eq(false)))
                .thenReturn(JSON.parseObject("{\"success\":true,\"data\":[{\"id\":7,\"name\":\"kw-id\"}]}"));
        new GatewayService(http,"http://gateway","token",true).removeRoute(task);
        verify(http).request(eq("DELETE"),eq("http://gateway/api/channel/7"),anyString(),isNull(),eq(true));
    }
    @Test void cannotSilentlyAbandonRegisteredRoutesWhenDisabled() {
        var task=mock(ServeTask.class);when(task.isGatewayRegistered()).thenReturn(true);
        var gateway=new GatewayService(mock(HttpKit.class),"http://gateway","",false);
        assertThrows(ApiException.class,()->gateway.removeRoute(task));
    }
}
