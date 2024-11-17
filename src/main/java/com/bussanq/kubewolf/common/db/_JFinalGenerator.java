package com.bussanq.kubewolf.common.db;

import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.dialect.OracleDialect;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.hikaricp.HikariCpPlugin;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.io.File;
import java.net.URISyntaxException;

/**
 * @author bussanq
 * @date 2024/11/17
 */
public class _JFinalGenerator {

    private static String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/bqinfra?allowMultiQueries=true&useUnicode=true&characterEncoding=utf8&useSSL=false&zeroDateTimeBehavior=convertToNull" ;

    public static DataSource getDataSource() {
        PropKit.use("application.yaml");
        HikariCpPlugin hikariCpPlugin = new HikariCpPlugin(jdbcUrl,"root","bq",PropKit.get("driver-class-name"));
        hikariCpPlugin.start();
        return hikariCpPlugin.getDataSource();
    }

    public static void main(String[] args) throws Exception {
        String baseModelPackage = "com.bussanq.kubewolf.api.model.base";
        String path = PathKit.class.getResource("/").toURI().getPath();
        String ret = new File(path).getParentFile().getParentFile().getCanonicalPath();
        String baseModelOutputDir = ret + "/src/main/java/com/bussanq/kubewolf/api/model/base";
        String modelPackage = "com.bussanq.kubewolf.api.model.dto";
        String modelOutputDir = baseModelOutputDir + "/../dto";
        Generator generator = new Generator(getDataSource(), baseModelPackage, baseModelOutputDir, modelPackage, modelOutputDir);
        // generator.setDialect(new OracleDialect());
        // generator.setMetaBuilder(new _MeraBuilder(getDataSource()));
        // 添加不需要生成的表名
        // generator.addExcludedTable("");
        // 是否在Model生成dao对象
        generator.setGenerateDaoInModel(true);
        // 设置是否生成链式setter方法
        generator.setGenerateChainSetter(true);
        // 设置是否生成字典文件
        generator.setGenerateDataDictionary(false);
        // 设置移除表名前缀
        // generator.setRemovedTableNamePrefixes("t_wx_");
        generator.generate();
    }
}
