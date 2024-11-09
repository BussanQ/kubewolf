package com.bussanq.kubewolf.k8s.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Data
public class K8sMeta {
    String name;
    String namespace;
    String creationTimestamp;
    List ownerReferences;
    Map<String, String> annotations;
    Map<String, String> labels;
}
