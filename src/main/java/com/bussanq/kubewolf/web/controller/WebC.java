package com.bussanq.kubewolf.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Controller
public class WebC {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/console")
    public String console(ModelMap mmap) {
        mmap.put("imageCount", "10");
        mmap.put("nodes", "5");
        mmap.put("cpu", "32");
        mmap.put("memoryUsed", "64");
        mmap.put("memory", "128");
        mmap.put("cpuRate", "0.2");
        mmap.put("gpuRate", "1");
        mmap.put("memRate", "0.5");
        return "aiplatform/web/console";
    }
}
