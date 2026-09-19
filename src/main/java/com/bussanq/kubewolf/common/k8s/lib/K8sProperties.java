package com.bussanq.kubewolf.common.k8s.lib;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("k8s")
public class K8sProperties {
    private boolean enabled = true;
    private String config = "";
    private String namespace = "default";
    private String[] noProxy = {"127.0.0.1", "localhost", "::1", ".svc", ".cluster.local"};
    private int connectionTimeout = 3000;
    private int requestTimeout = 10000;
}
