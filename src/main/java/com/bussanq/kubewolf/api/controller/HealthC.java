package com.bussanq.kubewolf.api.controller;

import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.common.k8s.lib.K8sProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

@RestController
@RequestMapping("/api/v1/health")
public class HealthC {
    private final AIService ai;
    private final DataSource dataSource;
    private final K8sProperties properties;
    public HealthC(AIService ai, DataSource dataSource, K8sProperties properties) {
        this.ai = ai; this.dataSource = dataSource; this.properties = properties;
    }
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        boolean database;
        try (Connection connection = dataSource.getConnection()) { database = connection.isValid(2); }
        catch (Exception e) { database = false; }
        boolean cluster = !properties.isEnabled() || ai.ready();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("database", database ? "up" : "down");
        result.put("kubernetes", !properties.isEnabled() ? "disabled" : ai.ready() ? "up" : "unavailable");
        if (dataSource instanceof HikariDataSource pool && pool.getHikariPoolMXBean() != null) {
            result.put("activeConnections", pool.getHikariPoolMXBean().getActiveConnections());
            result.put("maxConnections", pool.getMaximumPoolSize());
        }
        return ResponseEntity.status(database && cluster ? 200 : 503).body(result);
    }
}
