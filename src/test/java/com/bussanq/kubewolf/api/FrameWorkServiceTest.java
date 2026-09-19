package com.bussanq.kubewolf.api;

import com.bussanq.kubewolf.api.service.FrameWorkService;
import com.bussanq.kubewolf.common.error.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class FrameWorkServiceTest {
    @Test void returnsSupportedFrameworksAndRejectsUnknownValues() {
        var service = new FrameWorkService();
        ReflectionTestUtils.setField(service,"vllmImage","vllm:test");
        ReflectionTestUtils.setField(service,"sglangImage","sglang:test");
        assertEquals("sglang:test", service.getFrame("sglang").getImage());
        assertTrue(service.getFrame("vllm").getCmd().contains("--served-model-name model"));
        assertThrows(ApiException.class, () -> service.getFrame("ollama"));
        assertThrows(ApiException.class, () -> service.getFrame(null));
    }
}
