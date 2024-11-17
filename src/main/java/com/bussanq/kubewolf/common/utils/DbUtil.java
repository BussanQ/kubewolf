package com.bussanq.kubewolf.common.utils;

import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.CPI;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.TableMapping;
import com.jfinal.template.Engine;

import java.util.List;
import java.util.Map;

/**
 * @author bussanq
 * @date 2024/11/09
 */
public class DbUtil {

    private static final String search = "search";
    private static final String condition = "condition";
    private static final String table = "table";
    private static final String orderCol = "orderCol";

    public static String renderToYaml(Model m, String name) {
        Map<String, Object> tasks = CPI.getAttrs(m);
        return Engine.use().getTemplate(name).renderToString(tasks);
    }

    public static <T extends Model<T>> T searchFirst(T model, Kv conditionKv) {
        String tableName = TableMapping.me().getTable(model.getClass()).getName();
        return model.template(search, Kv.by(condition, conditionKv).set(table, tableName)).findFirst();
    }

    public static <T extends Model<T>> List<T> search(T model, Kv conditionKv) {
        String tableName = TableMapping.me().getTable(model.getClass()).getName();
        return model.template(search, Kv.by(condition, conditionKv).set(table, tableName)).find();
    }

    public static <T extends Model<T>> Page<T> searchPage(T model, Kv conditionKv, Page page) {
        int pageNumber = page.getPageNumber() == 0 ? 1 : page.getPageNumber();
        int pageSize = page.getPageSize() == 0 ? 10 : page.getPageSize();
        String tableName = TableMapping.me().getTable(model.getClass()).getName();
        return model.template(search, Kv.by(condition, conditionKv).set(table, tableName)).
                paginate(pageNumber, pageSize);
    }

    public static <T extends Model<T>> Page<T> searchPageOrder(T model, Kv conditionKv, Page page, String orderBy) {
        int pageNumber = page.getPageNumber() == 0 ? 1 : page.getPageNumber();
        int pageSize = page.getPageSize() == 0 ? 10 : page.getPageSize();
        String tableName = TableMapping.me().getTable(model.getClass()).getName();
        return model.template(search, Kv.by(condition, conditionKv).set(table, tableName).set(orderCol, orderBy)).
                paginate(pageNumber, pageSize);
    }

}
