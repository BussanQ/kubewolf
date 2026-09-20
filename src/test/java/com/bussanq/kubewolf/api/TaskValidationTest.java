package com.bussanq.kubewolf.api;

import com.bussanq.kubewolf.api.validation.TaskValidation;
import com.bussanq.kubewolf.common.error.ApiException;
import com.bussanq.kubewolf.web.model.vo.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskValidationTest {
    @Test void preservesColonsAndRejectsInvalidEnvironment() {
        assertEquals("https://a:443", TaskValidation.environment("URL:https://a:443;EMPTY:").get("URL"));
        assertThrows(ApiException.class, () -> TaskValidation.environment("invalid"));
        assertThrows(ApiException.class, () -> TaskValidation.environment("A:1;A:2"));
        assertThrows(ApiException.class, () -> TaskValidation.modelVolume("/model", "bad/pvc"));
    }
    @Test void modelMetadataDoesNotRequireDeploymentStorage() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var request = new ModelRequest();
            request.setName("Qwen"); request.setType("vllm");
            request.setVersion("2.5"); request.setDescription("模型描述");
            request.setModelPath("/models/qwen");
            assertTrue(factory.getValidator().validate(request).isEmpty());
            request.setCode("");
            assertTrue(factory.getValidator().validate(request).isEmpty());
            request.setCode("invalid/pvc");
            assertFalse(factory.getValidator().validate(request).isEmpty());
            assertThrows(ApiException.class, () -> TaskValidation.modelVolume(request.getModelPath(), null));
        }
    }

    @Test void rejectsUnboundedPaginationAndInvalidReplicas() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            PageQuery query = new PageQuery();
            assertTrue(validator.validate(query).isEmpty());
            query.setPageNum(0); query.setPageSize(101);
            assertEquals(2, validator.validate(query).size());
            DeployModelRequest request = new DeployModelRequest();
            request.setModelId("model");request.setTaskName("valid-name");request.setType("vllm");request.setReplicas(3);
            assertTrue(validator.validate(request).isEmpty());
            request.setReplicas(-1);
            assertFalse(validator.validate(request).isEmpty());
        }
    }
}
