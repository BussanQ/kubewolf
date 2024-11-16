package com.bussanq.kubewolf.common.k8s.model;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import lombok.Data;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Data
public class K8sRes {
    private String kind;
    private String apiVersion;
    private Object spec;
    private Object status;
    private K8sMeta meta;

    public static K8sRes fromCR(GenericKubernetesResource resource) {
        if (resource == null) {
            return null;
        }
        K8sRes k8sRes = new K8sRes();
        k8sRes.setKind(resource.getKind());
        k8sRes.setApiVersion(resource.getApiVersion());
        K8sMeta meta = new K8sMeta();
        meta.setLabels(resource.getMetadata().getLabels());
        meta.setAnnotations(resource.getMetadata().getAnnotations());
        meta.setName(resource.getMetadata().getName());
        meta.setNamespace(resource.getMetadata().getNamespace());
        meta.setCreationTimestamp(resource.getMetadata().getCreationTimestamp());
        meta.setOwnerReferences(resource.getMetadata().getOwnerReferences());
        k8sRes.setMeta(meta);
        k8sRes.setSpec(resource.getAdditionalProperties().get("spec"));
        k8sRes.setStatus(resource.getAdditionalProperties().get("status"));
        return k8sRes;
    }

}
