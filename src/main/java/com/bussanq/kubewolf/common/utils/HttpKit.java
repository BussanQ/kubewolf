package com.bussanq.kubewolf.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bussanq.kubewolf.common.error.ApiException;
import jakarta.annotation.PreDestroy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

@Component
public class HttpKit {
    private final CloseableHttpClient client = HttpClients.custom()
            .setMaxConnTotal(20).setMaxConnPerRoute(10).disableAutomaticRetries()
            .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(3000)
                    .setSocketTimeout(10000).setConnectionRequestTimeout(3000).build())
            .evictExpiredConnections().build();

    public JSONObject request(String method, String url, String token, Object body, boolean allowMissing) {
        RequestBuilder builder = RequestBuilder.create(method).setUri(url)
                .setHeader("Authorization", "Bearer " + token);
        if (body != null) builder.setHeader("Content-Type", "application/json")
                .setEntity(new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = client.execute(builder.build())) {
            int code = response.getStatusLine().getStatusCode();
            if (code == 404 && allowMissing) return null;
            if (code < 200 || code >= 300) throw new ApiException(503, "网关请求失败，HTTP " + code);
            String text = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JSONObject result = JSON.parseObject(text);
            // One-API uses HTTP 200 for GORM's missing-record response.
            if (allowMissing && "GET".equals(method) && result != null
                    && Boolean.FALSE.equals(result.getBoolean("success"))
                    && "record not found".equals(result.getString("message"))) return null;
            if (result == null || !Boolean.TRUE.equals(result.getBoolean("success")))
                throw new ApiException(503, "网关未确认操作成功");
            return result;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(503, "网关连接或响应异常: " + e.getClass().getSimpleName());
        }
    }
    @PreDestroy
    public void close() throws java.io.IOException { client.close(); }
}
