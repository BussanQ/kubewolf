package com.bussanq.kubewolf.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author bussanq
 * @date 2024/11/16
 */
@Controller
public class ErrorC implements ErrorController {

    @RequestMapping(value = "/error")
    public String error(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        if (statusCode == 500) {
            return "error/500";
        } else if (statusCode == 404) {
            return "error/404";
        } else if (statusCode == 403) {
            return "error/403";
        } else {
            return "error/404";
        }
    }

}
