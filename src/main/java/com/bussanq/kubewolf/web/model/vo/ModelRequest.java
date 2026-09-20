package com.bussanq.kubewolf.web.model.vo;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ModelRequest {
    @Size(max = 32) private String id;
    @NotBlank @Size(max = 255) private String name;
    @Size(max = 63)
    @Pattern(regexp = "([a-z0-9]([-a-z0-9]*[a-z0-9])?)?")
    private String code;
    @NotBlank @Size(max = 255) private String modelPath = "/model";
    @NotBlank @Pattern(regexp = "vllm|sglang") private String type;
    @Size(max = 1000) private String env;
    @Size(max = 1000) private String custom;
    @Size(max = 64) private String version;
    @Size(max = 1000) private String description;
    @Size(max = 255) private String image;
    @Size(max = 1000) private String cmd;
}
