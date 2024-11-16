package com.bussanq.kubewolf.common.k8s.lib;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(K8sProperties.class)
public class K8sConfiguration {

    @Resource
    K8sProperties k8sProperties;

    @Bean
    @ConditionalOnProperty("k8s.config")
    public K8sService k8sService() {
        K8sService k8sService = new K8sService();
        k8sService.init(k8sProperties);
        return k8sService;
    }
}
