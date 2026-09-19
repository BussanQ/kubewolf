package com.bussanq.kubewolf.api.validation;

import com.bussanq.kubewolf.common.error.ApiException;
import java.util.*;

public final class TaskValidation {
    private TaskValidation() { }
    public static Map<String, String> environment(String source) {
        Map<String, String> values = new LinkedHashMap<>();
        if (source == null || source.isBlank()) return values;
        for (String entry : source.split(";", -1)) {
            int split = entry.indexOf(':');
            if (split < 1 || !entry.substring(0, split).matches("[A-Za-z_][A-Za-z0-9_]*"))
                throw new ApiException(400, "环境变量格式应为 NAME:value;NAME2:value");
            String key = entry.substring(0, split);
            if (values.putIfAbsent(key, entry.substring(split + 1)) != null)
                throw new ApiException(400, "环境变量名称重复");
        }
        return values;
    }
    public static void modelVolume(String path, String code) {
        if (path == null || path.isBlank()) return;
        if (!path.startsWith("/") || path.contains("..") || code == null
                || !code.matches("[a-z0-9]([-a-z0-9]*[a-z0-9])?") || code.length() > 63)
            throw new ApiException(400, "模型挂载路径或 PVC 名称不合法");
    }
}
