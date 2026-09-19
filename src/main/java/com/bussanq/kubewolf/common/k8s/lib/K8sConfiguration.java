package com.bussanq.kubewolf.common.k8s.lib;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(K8sProperties.class)
public class K8sConfiguration {
    @Bean(destroyMethod = "close")
    public K8sService k8sService(K8sProperties properties) {
        K8sService service = new K8sService();
        service.init(properties);
        return service;
    }
}
