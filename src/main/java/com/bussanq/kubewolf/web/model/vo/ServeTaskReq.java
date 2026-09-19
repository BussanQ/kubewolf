package com.bussanq.kubewolf.web.model.vo;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ServeTaskReq {
    @Size(max = 32) private String taskId;
    @Size(max = 63) private String taskName;
    @AssertTrue(message = "必须提供 taskId 或 taskName")
    public boolean isTargetPresent() {
        return (taskId != null && !taskId.isBlank()) || (taskName != null && !taskName.isBlank());
    }
}
