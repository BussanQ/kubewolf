package com.bussanq.kubewolf.common.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author bussanq
 * @date 2024/11/18
 */
public class ServletUtl {

    public static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}
