package com.bussanq.kubewolf.web.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.jfinal.template.Engine;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bussanq
 * @date 2024/11/16
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * json 配置
      * @return
     */
    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON);
        fastConverter.setSupportedMediaTypes(fastMediaTypes);
        fastConverter.setFastJsonConfig(fastJsonConfig);
        return new HttpMessageConverters(fastConverter);
    }

    @Bean(name = "jfinalViewResolver")
    public JFinalViewResolver viewResolver() {
        JFinalViewResolver jf = new JFinalViewResolver();
        jf.getEngine().setDevMode(false);
        jf.getEngine().setToClassPathSourceFactory();
        jf.getEngine().setBaseTemplatePath("templates");
        jf.getEngine().addSharedFunction("include.html");
        jf.setSuffix(".html");
        jf.setContentType("text/html;charset=UTF-8");
        jf.setOrder(0);
        return jf;
    }

    @Bean
    public Engine k8sEngine() {
        Engine k8sEngine = Engine.use();
        k8sEngine.setBaseTemplatePath("k8s");
        k8sEngine.setToClassPathSourceFactory();
        return k8sEngine;
    }

}
