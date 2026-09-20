package com.bussanq.kubewolf.api;

import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.service.*;
import com.bussanq.kubewolf.common.db.ActiveRecordPluginConfig;
import com.bussanq.kubewolf.common.error.ApiException;
import com.bussanq.kubewolf.common.k8s.lib.K8sProperties;
import com.bussanq.kubewolf.web.model.vo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfinal.plugin.activerecord.Db;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

@EnabledIfEnvironmentVariable(named = "KUBEWOLF_TEST_DB_URL", matches = ".+")
class ModelMetadataTest {
    @Test
    void preservesMetadataAndUsesSeparateDeploymentDefaults() {
        new ApplicationContextRunner().withUserConfiguration(ActiveRecordPluginConfig.class)
                .withPropertyValues(
                        "spring.datasource.url=" + System.getenv("KUBEWOLF_TEST_DB_URL"),
                        "spring.datasource.username=" + System.getenv("KUBEWOLF_TEST_DB_USERNAME"),
                        "spring.datasource.password=" + System.getenv("KUBEWOLF_TEST_DB_PASSWORD"),
                        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    ModelService models = new ModelService();
                    ModelRequest request = new ModelRequest();
                    request.setName("元数据回归模型"); request.setType("vllm");
                    request.setModelPath("/models/qwen"); request.setVersion("2.5");
                    request.setDescription("模型描述"); request.setCustom("独立备注");
                    request.setImage("custom:test"); request.setCmd("custom serve /models/qwen");
                    var model = models.save(request);
                    request.setId(model.getId());
                    try {
                        assertThat(model.getVersion()).isEqualTo("2.5");
                        assertThat(model.getDescription()).isEqualTo("模型描述");
                        assertThat(model.getCustom()).isEqualTo("独立备注");
                        assertThat(model.getCode()).isNull();
                        var json = new ObjectMapper().valueToTree(model);
                        assertThat(json.get("version").asText()).isEqualTo("2.5");
                        assertThat(json.get("image").asText()).isEqualTo("custom:test");
                        assertThat(models.list(new PageQuery(), request.getName(), null).getList().getFirst().getCmd())
                                .isEqualTo(request.getCmd());

                        FrameWorkService frameworks = new FrameWorkService();
                        ReflectionTestUtils.setField(frameworks, "vllmImage", "vllm:test");
                        ReflectionTestUtils.setField(frameworks, "sglangImage", "sglang:test");
                        ServeService services = new ServeService(new TaskRepository(context.getBean(DataSource.class)),
                                frameworks, mock(AIService.class), new K8sProperties());
                        DeployModelRequest deploy = new DeployModelRequest();
                        deploy.setModelId(model.getId()); deploy.setTaskName("metadata-regression");
                        deploy.setType("vllm"); deploy.setReplicas(3);
                        assertThatThrownBy(() -> services.startModel(deploy)).isInstanceOf(ApiException.class);

                        request.setCode("qwen-pvc"); request.setVersion("2.5.1");
                        model = models.update(request);
                        assertThat(model.getVersion()).isEqualTo("2.5.1");
                        assertThat(model.getCode()).isEqualTo("qwen-pvc");
                        var task = services.startModel(deploy);
                        assertThat(task.getModelPath()).isEqualTo("/models/qwen");
                        assertThat(task.getModelCode()).isEqualTo("qwen-pvc");
                        assertThat(task.getImage()).isEqualTo("custom:test");
                        assertThat(task.getCmd()).isEqualTo(request.getCmd());
                        assertThat(task.getReplicas()).isEqualTo(3);

                        deploy.setTaskName("metadata-other-framework"); deploy.setType("sglang");
                        var other = services.startModel(deploy);
                        assertThat(other.getImage()).isEqualTo("sglang:test");
                        assertThat(other.getCmd()).contains("--model-path '/models/qwen'");
                    } finally {
                        Db.delete("delete from serve_task where model_id=?", request.getId());
                        models.delete(request.getId());
                    }
                });
    }
}
