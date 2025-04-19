package com.bussanq.kubewolf.ai.k8s;

import com.bussanq.kubewolf.api.service.GatewayService;
import com.bussanq.kubewolf.common.k8s.lib.K8sService;
import com.jfinal.template.Engine;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author bussanq
 * @date 2025/04/04
 */
@Slf4j
@Service
public class K8sOperator {

    @Value("${k8s.namespace:kubewolf}")
    String namespace;

    @Autowired(required = false)
    K8sService k8sService;

    @Autowired
    Engine engine;

    @Resource
    GatewayService gatewayService;

    public static SharedIndexInformer<Deployment> deploymentInformer;
    private static final String Gateway = "bussanq.com/gateway";
    private static final String ModelPort = "bussanq.com/modelport";
    private static final String ModelName = "bussanq.com/model";

    @PostConstruct
    private void init() {
        deploymentInformer = k8sService.getClient().apps().deployments().inNamespace(namespace).
                withLabel("kubewolf", "ServeTask").inform(new ResourceEventHandler<>() {
                    @Override
                    public void onAdd(io.fabric8.kubernetes.api.model.apps.Deployment deployment) {
                        String name = deployment.getMetadata().getName();
                        log.info("Add deployment {}", name);
                        Map<String, String> annos = deployment.getMetadata().getAnnotations();
                        if ("false".equals(annos.get(Gateway))) {
                            String modelName = annos.get(ModelName);
                            String port = annos.get(ModelPort);
                            String res = gatewayService.addRoute(name, name, modelName, port);
                            log.info("Add route {}", res);
//                            k8sService.apply(engine.getTemplate("HTTPRoute").renderToString(
//                                    Kv.by("name", name)
//                            ));
                            k8sService.addDeployAnnotations(name, Gateway, "true");
                        }
                    }

                    @Override
                    public void onUpdate(io.fabric8.kubernetes.api.model.apps.Deployment deployment, io.fabric8.kubernetes.api.model.apps.Deployment t1) {

                    }

                    @Override
                    public void onDelete(io.fabric8.kubernetes.api.model.apps.Deployment deployment, boolean b) {
                        String name = deployment.getMetadata().getName();
                        log.info("Del deployment {}", name);
                        Map<String, String> annos = deployment.getMetadata().getAnnotations();
                        if ("true".equals(annos.get(Gateway))) {
                            gatewayService.delRoute(name);
//                            k8sService.delete(engine.getTemplate("HTTPRoute").renderToString(
//                                    Kv.by("name", name)
//                            ));
                        }
                    }
                }, 180 * 1000L);
    }
}
