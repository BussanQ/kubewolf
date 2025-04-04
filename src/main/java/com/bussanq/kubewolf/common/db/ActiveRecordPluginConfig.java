package com.bussanq.kubewolf.common.db;

import com.bussanq.kubewolf.api.model.dto._MappingKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.hikaricp.HikariCpPlugin;
import com.jfinal.template.source.ClassPathSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author bussanq
 * @date 2024/11/17
 */
@Configuration
@Slf4j
public class ActiveRecordPluginConfig {

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    public ActiveRecordPlugin activeRecordPlugin() {
        HikariCpPlugin db = new HikariCpPlugin(url, username, password, driverClassName);
        db.start();
        ActiveRecordPlugin arp = new ActiveRecordPlugin(db);
        arp.setShowSql(false);
        arp.setDevMode(false);
        arp.getEngine().setSourceFactory(new ClassPathSourceFactory());
        arp.addSqlTemplate("/sql/sqls.sql");
        _MappingKit.mapping(arp);
        arp.start();
        return arp;
    }
}
