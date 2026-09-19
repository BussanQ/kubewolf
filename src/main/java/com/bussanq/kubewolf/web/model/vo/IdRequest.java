package com.bussanq.kubewolf.web.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IdRequest {
    @NotBlank @Size(max = 32) private String id;
}
