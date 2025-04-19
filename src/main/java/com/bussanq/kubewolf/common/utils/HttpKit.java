package com.bussanq.kubewolf.common.utils;

import cn.hutool.core.util.StrUtil;
import com.jfinal.kit.Kv;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author bussanq
 * @date 2025/04/19
 */
@Slf4j
public class HttpKit {

    private static PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    private static RequestConfig defaultRequestConfig;
    private static HttpRequestRetryHandler httpRequestRetryHandler;
    private static CloseableHttpClient httpClient = null;

    static {
        cm.setMaxTotal(2000);
        cm.setDefaultMaxPerRoute(1000);
        defaultRequestConfig = RequestConfig.custom().setSocketTimeout(10000)
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(60000)
                .build();
        // 请求重试处理
        httpRequestRetryHandler = (exception, executionCount, context) -> {
            if (executionCount >= 5) {// 如果已经重试了5次，就放弃
                return false;
            }
            if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                return true;
            }
            if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                return false;
            }
            if (exception instanceof InterruptedIOException) {// 超时
                return false;
            }
            if (exception instanceof UnknownHostException) {// 目标服务器不可达
                return false;
            }
            if (exception instanceof SSLException) {// SSL握手异常
                return false;
            }

            HttpClientContext clientContext = HttpClientContext
                    .adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的，就再次尝试
            if (!(request instanceof HttpEntityEnclosingRequest)) {
                return true;
            }
            return false;
        };
        httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setRetryHandler(httpRequestRetryHandler).setDefaultRequestConfig(defaultRequestConfig)
                .build();
    }

    public static CloseableHttpClient getHttpclient() {
        return httpClient;
    }

    public static InputStream doGetStream(String url) {
        HttpGet httpGet;
        HttpResponse response;
        try {
            httpGet = new HttpGet(url);
            response = httpClient.execute(httpGet);
            if (response != null) {
                return response.getEntity().getContent();
            }
        } catch (Exception ex) {
            log.error("url:{},error:{}", url, ex.getMessage());
        }
        return null;
    }

    public static String doGet(String url) {
        return doGetWithHeader(url, Kv.create());
    }

    public static String doGetWithHeader(String url, Map<String, String> headers) {
        HttpGet httpGet;
        String result = null;
        HttpResponse response;
        try {
            httpGet = new HttpGet(url);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpGet.setHeader(header.getKey(), header.getValue());
            }
            response = httpClient.execute(httpGet);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception ex) {
            log.error("url:{},error:{}", url, ex.getMessage());
        }
        return result;
    }

    public static String doGet(String url, Map<String, Object> map) {
        HttpGet httpGet;
        String result = null;
        HttpResponse response;
        try {
            CloseableHttpClient httpClient = getHttpclient();
            //设置参数
            URIBuilder uriBuilder = new URIBuilder(url);

            map.forEach((param, value) ->
                    uriBuilder.setParameter(param, value.toString())
            );

            URI build = uriBuilder.build();
            httpGet = new HttpGet(build);

            response = httpClient.execute(httpGet);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception ex) {
            log.error("url:{},error:{}", url, ex.getMessage());
        }
        return result;
    }

    public static String doDelete(String url) {
        return doDelete(url, Kv.create());
    }

    public static String doDelete(String url, Map<String, String> headers) {
        HttpDelete httpDelete;
        String result = null;
        HttpResponse response;
        try {
            httpDelete = new HttpDelete(url);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpDelete.setHeader(header.getKey(), header.getValue());
            }
            response = httpClient.execute(httpDelete);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception ex) {
            log.error("url:{},error:{}", url, ex.getMessage());
        }
        return result;
    }

    public static String doPost(String url, Map<String, String> map, Map<String, String> headers) {
        HttpPost httpPost = null;
        String result = null;
        HttpResponse response = null;
        try {
            httpPost = new HttpPost(url);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpPost.addHeader(header.getKey(), header.getValue());
            }
            //设置参数
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> elem = (Map.Entry<String, String>) iterator.next();
                list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
            }
            if (list.size() > 0) {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, StandardCharsets.UTF_8);
                httpPost.setEntity(entity);
            }
            response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception ex) {
            log.error("url:{},error:{}", url, ex.getMessage());
        } finally {
        }
        return result;
    }

    public static String postJson(String url, String jsonData, Map<String, String> headers) {
        HttpPost httpPost = null;
        String result = null;
        HttpResponse response = null;
        try {
            httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpPost.addHeader(header.getKey(), header.getValue());
            }
            if (StrUtil.isNotBlank(jsonData)) {
                StringEntity entity = new StringEntity(jsonData, StandardCharsets.UTF_8);
                httpPost.setEntity(entity);
            }
            response = httpClient.execute(httpPost);
            Header h = response.getFirstHeader("location");

            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception ex) {
            log.error("url:{},error:{}", url, ex.getMessage());
        } finally {
        }
        return result;
    }

    public static String postJson(String url, Kv dataMap) {
        return postJson(url, dataMap.toString(), Kv.create());
    }

    public static String postJson(String url, String jsonData) {
        return postJson(url, jsonData, Kv.create());
    }

}
