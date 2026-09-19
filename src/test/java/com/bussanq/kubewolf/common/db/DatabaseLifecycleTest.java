package com.bussanq.kubewolf.common.db;

import com.bussanq.kubewolf.api.model.dto.ModelTpl;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.hikaricp.HikariCpPlugin;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.util.ClassUtils;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.assertThat;

// Use a disposable empty MySQL schema: this test runs the actual migrations.
@EnabledIfEnvironmentVariable(named = "KUBEWOLF_TEST_DB_URL", matches = ".+")
class DatabaseLifecycleTest {
    @Test
    void migratesBeforeActiveRecordAndClosesTheSharedPool() {
        AtomicReference<HikariDataSource> pool = new AtomicReference<>();
        new ApplicationContextRunner().withUserConfiguration(ActiveRecordPluginConfig.class)
                .withPropertyValues(
                        "spring.datasource.url=" + System.getenv("KUBEWOLF_TEST_DB_URL"),
                        "spring.datasource.username=" + System.getenv("KUBEWOLF_TEST_DB_USERNAME"),
                        "spring.datasource.password=" + System.getenv("KUBEWOLF_TEST_DB_PASSWORD"),
                        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
                        "spring.datasource.hikari.minimum-idle=1",
                        "spring.datasource.hikari.maximum-pool-size=3",
                        "spring.datasource.hikari.connection-timeout=5000")
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(ActiveRecordPlugin.class);
                    assertThat(ClassUtils.isPresent("org.springframework.jdbc.core.JdbcTemplate", getClass().getClassLoader())).isFalse();
                    HikariDataSource dataSource = context.getBean(HikariDataSource.class);
                    pool.set(dataSource);
                    assertThat(context.getBean(HikariCpPlugin.class).getDataSource()).isSameAs(dataSource);
                    assertThat(context.getBean(Flyway.class).getConfiguration().getDataSource()).isSameAs(dataSource);
                    assertThat(context.getBean(Flyway.class).info().current().getVersion().getVersion()).isEqualTo("2");
                    assertThat(dataSource.getMinimumIdle()).isEqualTo(1);
                    assertThat(dataSource.getMaximumPoolSize()).isEqualTo(3);
                    ModelTpl model = new ModelTpl().setId("jdbc-removal-check").setName("数据库模块验证")
                            .setCode("test-pvc").setType("vllm").setModelPath("/model");
                    assertThat(model.save()).isTrue();
                    assertThat(ModelTpl.dao.findById(model.getId()).getName()).isEqualTo("数据库模块验证");
                    assertThat(model.delete()).isTrue();
                });
        assertThat(pool.get()).isNotNull();
        assertThat(pool.get().isClosed()).isTrue();
    }
}
