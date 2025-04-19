package com.bussanq.kubewolf.api.service;

import com.alibaba.fastjson.JSON;
import com.bussanq.kubewolf.api.model.OneApiChannel;
import com.bussanq.kubewolf.common.utils.HttpKit;
import com.jfinal.kit.Kv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author bussanq
 * @date 2025/04/19
 */
@Service
public class GatewayService {

    @Value("${oneapi.url}")
    private String OneApi;
    @Value("${oneapi.accessToken}")
    private String Token;

    public String addRoute(String name, String modelCode, String modelName, String modelPort){
        OneApiChannel oneApiChannel = new OneApiChannel();
        oneApiChannel.setName(name);
        oneApiChannel.setType(50);
        oneApiChannel.setKey("kubewolf");
        oneApiChannel.setBaseUrl("http://" + name + ":" + modelPort + "/v1");
        oneApiChannel.setModels(modelCode);
        oneApiChannel.setGroup("default");
        oneApiChannel.setModelMapping(JSON.toJSONString(Kv.by(modelCode, modelName)));
        return HttpKit.postJson(OneApi + "/api/channel/", JSON.toJSONString(oneApiChannel), getToken());
    }

    public String delRoute(String name){
        OneApiChannel oneApiChannel = getChannel(name);
        String res= HttpKit.doDelete(OneApi + "/api/channel/" + oneApiChannel.getId(), getToken());
        return res;
    }

    private Kv getToken(){
        return Kv.by("Authorization","Bearer " + Token);
    }

    private OneApiChannel getChannel(String name){
        String res = HttpKit.doGetWithHeader(OneApi + "/api/channel/", getToken());
        List<OneApiChannel> list = JSON.parseObject(res).getJSONArray("data").toJavaList(OneApiChannel.class);
        return list.stream().filter(oneApiChannel -> oneApiChannel.getName().equals(name)).findFirst().orElse(null);
    }

}
