package com.bussanq.kubewolf.web.model.vo;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ServeTaskInput {
    @Size(max = 32) private String taskId;
    @NotBlank @Size(max = 63)
    @Pattern(regexp = "[a-z0-9]([-a-z0-9]*[a-z0-9])?") private String taskName;
    @NotBlank @Size(max = 255) private String image;
    @NotBlank @Pattern(regexp = "run|vllm|sglang") private String type = "run";
    @NotNull @Min(1) @Max(100) private Integer replicas = 1;
    @NotNull @Min(1) @Max(65535) private Integer port = 8080;
    @Size(max = 1000) private String cmd;
    @Size(max = 1000) private String env;
    @Size(max = 255) private String modelPath;
    @Size(max = 63) private String modelCode;
    @Min(1) @Max(1000000) private Integer cpu = 500;
    @Min(1) @Max(1000000) private Integer mem = 1024;
    @Min(0) @Max(128) private Integer gpu = 0;
    @NotBlank @Size(max = 128) @Pattern(regexp = "[a-z0-9.-]+/[A-Za-z0-9._-]+")
    private String gpuResource = "nvidia.com/gpu";
}
