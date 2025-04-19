package com.bussanq.kubewolf.api.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author bussanq
 * @date 2025/04/19
 */
@Data
public class OneApiChannel {
    Integer id;
    String name;
    Integer type;
    String key;

    @JSONField(name = "base_url")
    String baseUrl;
    @JSONField(name = "model_mapping")
    String modelMapping;
    @JSONField(name = "system_prompt")
    String systemPrompt;
    String models;
    String group;

}
