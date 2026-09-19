package com.bussanq.kubewolf.common.db;

import com.bussanq.kubewolf.api.model.dto._MappingKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.hikaricp.HikariCpPlugin;
import com.jfinal.template.source.ClassPathSourceFactory;
import com.zaxxer.hikari.HikariConfig;
import org.flywaydb.core.Flyway;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import javax.sql.DataSource;

@Configuration
public class ActiveRecordPluginConfig {
    @Bean(initMethod = "start", destroyMethod = "stop")
    public HikariCpPlugin hikariCpPlugin(Environment environment) {
        HikariCpPlugin plugin = new HikariCpPlugin(environment.getRequiredProperty("spring.datasource.url"),
                environment.getRequiredProperty("spring.datasource.username"),
                environment.getRequiredProperty("spring.datasource.password"),
                environment.getRequiredProperty("spring.datasource.driver-class-name")) {
            @Override
            protected HikariConfig newHikariConfig() {
                HikariConfig config = super.newHikariConfig();
                Binder.get(environment).bind("spring.datasource.hikari", Bindable.ofInstance(config));
                return config;
            }
        };
        // start() applies plugin settings after newHikariConfig(); bind those as well.
        Binder.get(environment).bind("spring.datasource.hikari", Bindable.ofInstance(plugin));
        return plugin;
    }

    // HikariCpPlugin owns the pool; do not close the same DataSource twice.
    @Bean(destroyMethod = "")
    public DataSource dataSource(HikariCpPlugin plugin) {
        return plugin.getDataSource();
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource, Environment environment) {
        return Flyway.configure().dataSource(dataSource)
                .locations(environment.getProperty("spring.flyway.locations", String[].class,
                        new String[]{"classpath:db/migration"}))
                .baselineOnMigrate(environment.getProperty("spring.flyway.baseline-on-migrate", Boolean.class, true))
                .baselineVersion(environment.getProperty("spring.flyway.baseline-version", "0"))
                .cleanDisabled(environment.getProperty("spring.flyway.clean-disabled", Boolean.class, true))
                .load();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @DependsOn("flyway")
    public ActiveRecordPlugin activeRecordPlugin(HikariCpPlugin plugin) {
        ActiveRecordPlugin arp = new ActiveRecordPlugin(plugin);
        arp.setShowSql(false);
        arp.setDevMode(false);
        arp.getEngine().setSourceFactory(new ClassPathSourceFactory());
        arp.addSqlTemplate("/sql/sqls.sql");
        _MappingKit.mapping(arp);
        return arp;
    }
}
