package com.bussanq.kubewolf.api.service;

import com.bussanq.kubewolf.api.model.InferFrameWork;
import com.bussanq.kubewolf.common.error.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FrameWorkService {
    @Value("${inference.vllm-image}") private String vllmImage;
    @Value("${inference.sglang-image}") private String sglangImage;

    public InferFrameWork getFrame(String name) {
        return getFrame(name, "/model");
    }

    public InferFrameWork getFrame(String name, String modelPath) {
        if (modelPath == null || !modelPath.startsWith("/") || modelPath.contains(".."))
            throw new ApiException(400, "模型部署需要有效的容器内绝对路径");
        String path = "'" + modelPath.replace("'", "'\"'\"'") + "'";
        if (name == null) throw new ApiException(400, "请选择推理框架");
        InferFrameWork frame = new InferFrameWork();
        frame.setName(name);
        frame.setPort("8080");
        switch (name) {
            case "vllm" -> {
                frame.setCmd("vllm serve " + path + " -tp 1 --host 0.0.0.0 --port 8080 --served-model-name model");
                frame.setImage(vllmImage);
            }
            case "sglang" -> {
                frame.setCmd("python3 -m sglang.launch_server --model-path " + path + " --tp 1 --host 0.0.0.0 --port 8080 --served-model-name model");
                frame.setImage(sglangImage);
            }
            default -> throw new ApiException(400, "不支持的推理框架: " + name);
        }
        return frame;
    }
}
