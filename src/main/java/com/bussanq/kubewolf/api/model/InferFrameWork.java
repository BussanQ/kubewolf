package com.bussanq.kubewolf.api.model;

import lombok.Data;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Data
public class InferFrameWork {
    String name;
    String cmd;
    String image;
    String port;
    String env;
}
