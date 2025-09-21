package com.bussanq.kubewolf.web.model.vo;

import lombok.Data;

/**
 * @author bussanq
 * @date 2024/11/17
 */
@Data
public class ServeTaskReq {
    String taskId;
    String modelCode;
    String taskName;
    String type;
}
