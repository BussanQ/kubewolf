package com.bussanq.kubewolf.k8s.lib;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.bussanq.kubewolf.k8s.model.K8sRes;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionNames;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Slf4j
@Data
public class K8sService {

    private KubernetesClient client;

    private static String currentNamespace = "default";

    public void init(K8sProperties k8sProperties) {
        try {
            String configStr = k8sProperties.getConfig();
            if (configStr == null) {
                log.error("未读取到配置，初始化 K8sService 失败");
                return;
            }
            if (StrUtil.isNotBlank(configStr)){
                String configData = IoUtil.read(ResourceUtil.getStream(configStr), Charset.defaultCharset());
                Config config = Config.fromKubeconfig(configData);
                client = new KubernetesClientBuilder().withConfig(config).build();
            }else {
                client = new KubernetesClientBuilder().build();
            }
            if (StrUtil.isNotBlank(k8sProperties.getNamespace())) {
                currentNamespace = k8sProperties.getNamespace();
            }
        } catch (Exception e) {
            log.error("初始化 K8sService 失败", e);
        }
    }

    public boolean apply (String yaml) {
        try {
            InputStream stream = IoUtil.toStream(yaml, Charset.defaultCharset());
            if (StrUtil.contains(yaml, "namespace")) {
                client.load(stream).serverSideApply();
//            client.load(stream).createOrReplace();
            }else {
                client.load(stream).inNamespace(currentNamespace).serverSideApply();
            }
            return true;
        }catch (Exception e) {
            log.error("执行apply异常", e);
            return false;
        }
    }

    public boolean apply (String yaml, String namespace) {
        try {
            InputStream stream = IoUtil.toStream(yaml, Charset.defaultCharset());
            client.load(stream).inNamespace(namespace).serverSideApply();
//            client.load(stream).createOrReplace();
            return true;
        }catch (Exception e) {
            log.error("执行apply异常", e);
            return false;
        }
    }

    public boolean delete (String yaml) {
        try {
            InputStream stream = IoUtil.toStream(yaml, Charset.defaultCharset());
            List<StatusDetails> statusDetails = client.load(stream).delete();
            return true;
        }catch (Exception e) {
            log.error("执行delete异常", e);
            return false;
        }
    }

    public boolean getDeployStatus (String name) {
        return getDeployStatus(name, currentNamespace);
    }

    public boolean getDeployStatus (String name, String namespace) {
        return client.apps().deployments().inNamespace(namespace).withName(name).isReady();
    }

    public boolean hasDeploy(String name) {
        return hasDeploy(name, currentNamespace);
    }

    public boolean hasDeploy(String name, String namespace) {
        return client.apps().deployments().inNamespace(namespace).withName(name).get() != null;
    }

    public boolean delDeploy (String name) {
        return delDeploy(name, currentNamespace);
    }

    public boolean delDeploy (String name, String namespace) {
        try {
            List<StatusDetails> statusDetails = client.apps().deployments().inNamespace(namespace).withName(name).delete();
            return !statusDetails.isEmpty();
        } catch (Exception e) {
            log.error("执行delDeploy异常", e);
            return false;
        }
    }

    public boolean delService (String name) {
        return delService(name, currentNamespace);
    }

    public boolean delService (String name, String namespace) {
        try {
            List<StatusDetails> statusDetails = client.services().inNamespace(namespace).withName(name).delete();
            return !statusDetails.isEmpty();
        } catch (Exception e) {
            log.error("执行delService异常", e);
            return false;
        }
    }

    /**
     * 只返回了第一个端口
     * @param name
     * @return
     */
    public String getPort (String name) {
        return getPort(name, currentNamespace);
    }

    /**
     * 只返回了第一个端口
     * @param name
     * @param namespace
     * @return
     */
    public String getPort (String name, String namespace) {
        Service service = client.services().inNamespace(namespace).withName(name).get();
        if (service != null) {
            return service.getSpec().getPorts().get(0).getPort().toString();
        }
        return null;
    }

    public boolean createConfigMap(String name, String key, String value){
        return createConfigMap(name, key, value, currentNamespace);
    }

    public boolean createConfigMap(String name, String key, String value, String namespace){
        try {
            ConfigMap configMap = new ConfigMapBuilder().withNewMetadata().withName(name)
                    .endMetadata().addToData(key, value)
                    .build();
            client.configMaps().inNamespace(currentNamespace).resource(configMap).serverSideApply();
            return true;
        } catch (Exception e) {
            log.error("执行createConfigMap异常", e);
            return false;
        }
    }

    public boolean updateConfigMap(String name, String key, String value){
        return updateConfigMap(name, key, value, currentNamespace);
    }

    public boolean updateConfigMap(String name, String key, String value, String namespace){
        try {
            client.configMaps().inNamespace(namespace).withName(name).edit(
                    c -> new ConfigMapBuilder(c).addToData(key, value).build());
            return true;
        } catch (Exception e) {
            log.error("执行 updateConfigMap 异常", e);
            return false;
        }
    }

    public boolean delConfigMap (String name) {
        return delConfigMap(name, currentNamespace);
    }

    public boolean delConfigMap (String name, String namespace) {
        try {
            List<StatusDetails> statusDetails = client.configMaps().inNamespace(namespace).withName(name).delete();
            return !statusDetails.isEmpty();
        } catch (Exception e) {
            log.error("执行 delConfigMap 异常", e);
            return false;
        }
    }

    public boolean delConfigMapData(String name, String key){
        return delConfigMapData(name, key, currentNamespace);
    }

    public boolean delConfigMapData(String name, String key, String namespace){
        try {
            client.configMaps().inNamespace(namespace).withName(name).edit(
                    c -> new ConfigMapBuilder(c).removeFromData(key).build());
            return true;
        } catch (Exception e) {
            log.error("执行 delConfigMapData 异常", e);
            return false;
        }
    }

    public GenericKubernetesResource getCr(CustomResourceDefinitionContext ctx, String name) {
        return getCr(ctx, name, currentNamespace);
    }

    public GenericKubernetesResource getCr(CustomResourceDefinitionContext ctx, String name, String namespace) {
        return client.genericKubernetesResources(ctx).inNamespace(namespace).withName(name).get();
    }

    public K8sRes getCr(String crdName, String name) {
        return getCr(crdName, name, currentNamespace);
    }

    public K8sRes getCr(String crdName, String name, String namespace) {
        try {
            CustomResourceDefinition crd = client.apiextensions().v1().customResourceDefinitions().list()
                    .getItems().stream().filter(
                            i -> {
                                CustomResourceDefinitionNames c = i.getSpec().getNames();
                                return c.getKind().equals(crdName) || c.getPlural().equals(crdName) ||
                                        c.getSingular().equals(crdName);
                            }
                    ).findFirst().orElse(null);
            if (crd == null) {
                return null;
            }
            CustomResourceDefinitionContext ctx = CustomResourceDefinitionContext.fromCrd(crd);
            GenericKubernetesResource cr = client.genericKubernetesResources(ctx)
                    .inNamespace(namespace).withName(name).get();
            return K8sRes.fromCR(cr);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    public boolean applyCr(String yaml, CustomResourceDefinitionContext ctx, String namespace){
        try {
            client.genericKubernetesResources(ctx).inNamespace(namespace)
                    .load(IoUtil.toStream(yaml, Charset.defaultCharset())).serverSideApply();
            return true;
        } catch (Exception e) {
            log.error("执行 applyCr 异常", e);
            return false;
        }
    }

    public boolean applyCr(String yaml, CustomResourceDefinitionContext ctx){
        try {
            client.genericKubernetesResources(ctx)
                    .load(IoUtil.toStream(yaml, Charset.defaultCharset())).serverSideApply();
            return true;
        } catch (Exception e) {
            log.error("执行 applyCr 异常", e);
            return false;
        }
    }

    public GenericKubernetesResourceList listCr(CustomResourceDefinitionContext ctx, String namespace){
        return client.genericKubernetesResources(ctx).inNamespace(namespace).list();
    }

    public boolean delCr(String name, CustomResourceDefinitionContext ctx){
        return delCr(name, ctx, currentNamespace);
    }

    public boolean delCr(String name, CustomResourceDefinitionContext ctx, String namespace){
        try {
            List<StatusDetails> statusDetails = client.genericKubernetesResources(ctx)
                    .inNamespace(namespace).withName(name).delete();
            return !statusDetails.isEmpty();
        } catch (Exception e) {
            log.error("执行 delCr 异常", e);
            return false;
        }
    }

}
