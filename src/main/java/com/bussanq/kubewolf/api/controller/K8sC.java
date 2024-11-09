package com.bussanq.kubewolf.api.controller;

import com.bussanq.kubewolf.k8s.lib.K8sService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Slf4j
@RestController
public class K8sC {

    @Autowired(required = false)
    K8sService k8sService;

    @GetMapping("/start")
    public String index() {
        log.info("start");
        k8sService.createConfigMap("acs", "a", "2");
        k8sService.apply("apiVersion: v1\n" +
                "data:\n" +
                "  c: \"2\"\n" +
                "kind: ConfigMap\n" +
                "metadata:\n" +
                "  name: acs2");
        return "ok";
    }
}
