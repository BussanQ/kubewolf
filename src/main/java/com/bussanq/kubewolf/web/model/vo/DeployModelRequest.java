package com.bussanq.kubewolf.web.model.vo;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DeployModelRequest {
    @NotBlank @Size(max = 32) private String modelId;
    @NotBlank @Size(max = 63)
    @Pattern(regexp = "[a-z0-9]([-a-z0-9]*[a-z0-9])?") private String taskName;
    @NotBlank @Pattern(regexp = "vllm|sglang") private String type;
    @NotNull @Min(1) @Max(100) private Integer replicas = 1;
}
