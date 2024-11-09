package com.bussanq.kubewolf.common.utils;

import com.bussanq.kubewolf.api.model.dto.ServingTask;
import com.jfinal.template.Engine;

/**
 * @author bussanq
 * @date 2024/11/09
 */
public class DbUtil {

    public static String renderToYaml (ServingTask m, String name) {
//        Map<String, Object> tasks = CPI.getAttrs(m);
        return Engine.use().getTemplate(m.getClass().getSimpleName()).renderToString();
    }
}
