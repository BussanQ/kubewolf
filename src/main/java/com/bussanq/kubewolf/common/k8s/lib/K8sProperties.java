package com.bussanq.kubewolf.common.k8s.lib;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Data
@ConfigurationProperties("k8s")
public class K8sProperties {

    private String config;

    private String namespace;
}
