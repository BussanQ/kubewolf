package com.bussanq.kubewolf.api;

import com.bussanq.kubewolf.api.service.FrameWorkService;
import com.bussanq.kubewolf.common.error.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class FrameWorkServiceTest {
    @Test void usesAndQuotesTheConfiguredModelPath() {
        var service = new FrameWorkService();
        assertTrue(service.getFrame("vllm", "/models/my model").getCmd().contains("serve '/models/my model'"));
        assertTrue(service.getFrame("sglang", "/models/qwen").getCmd().contains("--model-path '/models/qwen'"));
        assertTrue(service.getFrame("vllm", "/models/it's").getCmd().contains("'/models/it'\"'\"'s'"));
        assertThrows(ApiException.class, () -> service.getFrame("vllm", "relative/path"));
    }

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
