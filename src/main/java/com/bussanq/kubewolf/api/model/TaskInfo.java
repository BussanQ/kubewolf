package com.bussanq.kubewolf.api.model;

import lombok.Data;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Data
public class TaskInfo {
    String name;
    String status;
    String inUrl;
    String outUrl;
}
