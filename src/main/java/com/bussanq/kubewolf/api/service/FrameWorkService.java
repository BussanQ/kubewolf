package com.bussanq.kubewolf.api.service;

import com.bussanq.kubewolf.api.model.InferFrameWork;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author bussanq
 * @date 2025/02/25
 */
@Slf4j
@Service
public class FrameWorkService {

    public InferFrameWork getFrame(String frameName) {
        switch (frameName){
            case "vllm":
                InferFrameWork framework = new InferFrameWork();
                framework.setName("vllm");
                framework.setCmd("vllm serve /model -tp 1 --host 0.0.0.0 --port 8080 --served-model-name qwen");
                framework.setImage("vllm-openapi:latest");
                framework.setPort("8080");
//                framework.setExtraArgs("--model_config_file=/app/models.config");
                return framework;
            case "sglang":
                InferFrameWork sglang = new InferFrameWork();
                sglang.setName("sglang");
                sglang.setCmd("python3 -m sglang.launch_server --model-path /model --tp 1 --host 0.0.0.0 --port=8080  --served-model-name qwen");
                sglang.setImage("sglang:latest");
                sglang.setPort("8080");
            default:
                return new InferFrameWork();
        }
    }

}
