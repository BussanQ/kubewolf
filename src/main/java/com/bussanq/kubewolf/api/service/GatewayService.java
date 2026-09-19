package com.bussanq.kubewolf.api.service;

import com.alibaba.fastjson.*;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.common.error.ApiException;
import com.bussanq.kubewolf.common.utils.HttpKit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GatewayService {
    private final HttpKit http;
    private final String url;
    private final String token;
    private final boolean enabled;
    public GatewayService(HttpKit http, @Value("${oneapi.url}") String url,
                          @Value("${oneapi.accessToken:}") String token,
                          @Value("${oneapi.enabled:true}") boolean enabled) {
        this.http = http; this.url = url.replaceAll("/+$", ""); this.token = token; this.enabled = enabled;
    }
    public boolean enabled() { return enabled; }
    private void configured() {
        if (!enabled || token.isBlank()) throw new ApiException(503, "网关未启用或缺少访问令牌");
    }
    private JSONObject request(String method, String path, Object body, boolean missing) {
        configured();
        return http.request(method, url + path, token, body, missing);
    }
    private JSONObject byId(Integer id) {
        if (id == null) return null;
        JSONObject result = request("GET", "/api/channel/" + id, null, true);
        return result == null ? null : result.getJSONObject("data");
    }
    private JSONObject find(ServeTask task) {
        JSONObject saved = byId(task.getGatewayChannelId());
        if (saved != null) {
            if (!task.getResourceName().equals(saved.getString("name")))
                throw new ApiException(409, "网关渠道归属不匹配");
            return saved;
        }
        Set<Integer> seen = new HashSet<>();
        for (int page = 0; page < 100; page++) {
            JSONObject result = request("GET", "/api/channel/?p=" + page + "&page_size=100", null, false);
            Object data = result.get("data");
            JSONArray rows = data instanceof JSONArray array ? array :
                    data instanceof JSONObject object ? object.getJSONArray("items") : null;
            if (rows == null) throw new ApiException(503, "网关渠道列表响应格式不兼容");
            if (rows.isEmpty()) return null;
            boolean advanced = false;
            for (int i = 0; i < rows.size(); i++) {
                JSONObject channel = rows.getJSONObject(i);
                advanced |= seen.add(channel.getInteger("id"));
                if (task.getResourceName().equals(channel.getString("name"))) return channel;
            }
            if (!advanced) throw new ApiException(503, "网关渠道分页未前进，请检查接口兼容性");
        }
        throw new ApiException(503, "网关渠道查询超过分页上限");
    }
    public Integer ensureRoute(ServeTask task) {
        if (!enabled && !task.isGatewayRegistered()) return null;
        configured();
        JSONObject existing = find(task);
        Map<String, Object> route = new LinkedHashMap<>();
        route.put("name", task.getResourceName()); route.put("type", 50); route.put("key", "kubewolf");
        route.put("base_url", "http://" + task.getResourceName() + "." + task.getNamespace() + ".svc.cluster.local:" + task.getPort() + "/v1");
        route.put("models", task.getTaskName()); route.put("group", "default");
        route.put("model_mapping", JSON.toJSONString(Map.of(task.getTaskName(), task.getModelName())));
        if (existing == null) {
            request("POST", "/api/channel/", route, false);
            existing = find(task); // Recoverable even if the POST response is lost.
            if (existing == null) throw new ApiException(503, "网关渠道创建后尚不可见");
        } else if (!Objects.equals(existing.getString("base_url"), route.get("base_url"))
                || !Objects.equals(existing.getString("models"), route.get("models"))
                || !Objects.equals(existing.getString("model_mapping"), route.get("model_mapping"))) {
            route.put("id", existing.getInteger("id"));
            request("PUT", "/api/channel/", route, false);
        }
        Integer id = existing.getInteger("id");
        if (id == null) throw new ApiException(503, "网关渠道缺少 ID");
        return id;
    }
    public void removeRoute(ServeTask task) {
        if (!enabled && !task.isGatewayRegistered()) return;
        configured();
        JSONObject existing = find(task);
        if (existing != null) request("DELETE", "/api/channel/" + existing.getInteger("id"), null, true);
    }
}
