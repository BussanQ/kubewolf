package com.bussanq.kubewolf.web;

import com.bussanq.kubewolf.api.controller.ConsoleC;
import com.bussanq.kubewolf.api.service.ConsoleService;
import com.bussanq.kubewolf.web.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConsoleC.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "auth.admin-password=test-admin-password-123")
class ConsoleApiTest {
    @Autowired MockMvc mvc;
    @MockBean ConsoleService service;

    @Test
    void requiresLoginBeforeQueryingClusterInformation() throws Exception {
        mvc.perform(get("/api/v1/console")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void viewerCanQueryNumericMetricsAndMissingGpuRemainsUnknown() throws Exception {
        when(service.query()).thenReturn(new ConsoleService.Metrics(3L, 1, new BigDecimal("4"),
                new BigDecimal("8"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null));
        mvc.perform(get("/api/v1/console").with(user("viewer").roles("VIEWER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.imageCount").value(3))
                .andExpect(jsonPath("$.data.nodes").value(1))
                .andExpect(jsonPath("$.data.cpuRate").value(0))
                .andExpect(jsonPath("$.data.memoryUsed").value(0))
                .andExpect(jsonPath("$.data.gpuRate").doesNotExist());
    }
}
