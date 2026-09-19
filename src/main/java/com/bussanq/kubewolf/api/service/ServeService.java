package com.bussanq.kubewolf.api.service;

import cn.hutool.core.util.IdUtil;
import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.model.dto.*;
import com.bussanq.kubewolf.api.validation.TaskValidation;
import com.bussanq.kubewolf.common.error.ApiException;
import com.bussanq.kubewolf.common.k8s.lib.K8sProperties;
import com.bussanq.kubewolf.web.model.vo.*;
import com.jfinal.plugin.activerecord.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ServeService {
    private final TaskRepository repository;
    private final FrameWorkService frameworks;
    private final AIService ai;
    private final K8sProperties properties;
    public ServeService(TaskRepository repository, FrameWorkService frameworks, AIService ai, K8sProperties properties) {
        this.repository = repository; this.frameworks = frameworks; this.ai = ai; this.properties = properties;
    }
    public ServeTask findById(String id) {
        ServeTask task = repository.find(id);
        if (task == null) throw new ApiException(404, "未找到服务");
        return task;
    }
    public Page<ServeTask> list(PageQuery page, String name, String type) {
        Page<ServeTask> result = repository.page(page, name, type);
        for (ServeTask task : result.getList()) {
            if ("running".equals(task.getDesiredState()) && !"error".equals(task.getActualStatus())) {
                String state = ai.status(task).state();
                // A ready Deployment is not enough until gateway reconciliation succeeds.
                if (!"running".equals(state) || "running".equals(task.getActualStatus())) task.setStatus(state);
            }
        }
        return result;
    }
    public ServeTask save(ServeTaskInput request) {
        ServeTask task = fill(new ServeTask(), request);
        initialize(task, "stopped");
        repository.insert(task);
        return repository.find(task.getTaskId());
    }
    public ServeTask update(ServeTaskInput request) {
        if (request.getTaskId() == null || request.getTaskId().isBlank()) throw new ApiException(400, "缺少服务 ID");
        ServeTask[] result = new ServeTask[1];
        Db.tx(() -> {
            ServeTask task = ServeTask.dao.findFirst("select * from serve_task where task_id=? for update", request.getTaskId());
            if (task == null) throw new ApiException(404, "未找到服务");
            if ("deleted".equals(task.getDesiredState())) throw new ApiException(409, "服务正在删除");
            if (!task.getTaskName().equals(request.getTaskName())) throw new ApiException(409, "服务名称创建后不可修改");
            fill(task, request);
            if ("running".equals(task.getDesiredState())) task.set("actual_status", "pending");
            task.set("generation", task.getGeneration() + 1).set("last_error", null).set("retry_count", 0);
            repository.update(task);
            result[0] = repository.find(task.getTaskId());
            return true;
        });
        return result[0];
    }
    public ServeTask startModel(DeployModelRequest request) {
        var frame = frameworks.getFrame(request.getType());
        ServeTask[] result = new ServeTask[1];
        Db.tx(() -> {
            ModelTpl model = ModelTpl.dao.findFirst("select * from model_tpl where id=? for update", request.getModelId());
            if (model == null) throw new ApiException(404, "未找到模型");
            TaskValidation.modelVolume(model.getModelPath(), model.getCode());
            TaskValidation.environment(model.getEnv());
            ServeTask task = new ServeTask().setTaskName(request.getTaskName()).setType(request.getType())
                    .setImage(frame.getImage()).setCmd(frame.getCmd()).setPort(frame.getPort()).setReplicas(request.getReplicas())
                    .setModelCode(model.getCode()).setModelPath("/model").setEnv(model.getEnv()).setCpu(1000).setMem(16000).setGpu(1);
            task.set("model_id", model.getId());
            initialize(task, "running");
            repository.insert(task);
            result[0] = repository.find(task.getTaskId());
            return true;
        });
        return result[0];
    }
    public void intent(ServeTaskReq request, String desired) {
        ServeTask task = request.getTaskId() != null && !request.getTaskId().isBlank()
                ? repository.find(request.getTaskId()) : repository.byName(request.getTaskName());
        if (task == null) {
            if ("deleted".equals(desired)) return;
            throw new ApiException(404, "未找到服务");
        }
        if ("deleted".equals(task.getDesiredState()) && !"deleted".equals(desired))
            throw new ApiException(409, "服务正在删除");
        if (desired.equals(task.getDesiredState()) && !"error".equals(task.getActualStatus())) return;
        if (task.getNamespace() == null) task.set("namespace", properties.getNamespace());
        if (task.getResourceName() == null) task.set("resource_name", task.getTaskName().toLowerCase(Locale.ROOT));
        repository.intent(task, desired);
    }
    private void initialize(ServeTask task, String desired) {
        task.setTaskId(IdUtil.fastSimpleUUID());
        task.set("namespace", properties.getNamespace()).set("resource_name", "kw-" + task.getTaskId())
                .set("desired_state", desired).set("actual_status", desired.equals("running") ? "pending" : "stopped")
                .set("generation", 1L).set("retry_count", 0)
                .set("gateway_registered", false).set("model_name", "model");
        if (task.getGpuResource() == null) task.set("gpu_resource", "nvidia.com/gpu");
    }
    private ServeTask fill(ServeTask task, ServeTaskInput request) {
        TaskValidation.environment(request.getEnv());
        TaskValidation.modelVolume(request.getModelPath(), request.getModelCode());
        task.setTaskName(request.getTaskName()).setImage(request.getImage()).setType(request.getType())
                .setReplicas(request.getReplicas()).setPort(request.getPort().toString()).setCmd(request.getCmd())
                .setEnv(request.getEnv()).setModelPath(request.getModelPath()).setModelCode(request.getModelCode())
                .setCpu(request.getCpu() == null ? 500 : request.getCpu()).setMem(request.getMem() == null ? 1024 : request.getMem())
                .setGpu(request.getGpu() == null ? 0 : request.getGpu());
        task.set("gpu_resource", request.getGpuResource());
        return task;
    }
}
