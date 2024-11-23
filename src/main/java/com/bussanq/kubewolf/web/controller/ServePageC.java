package com.bussanq.kubewolf.web.controller;

import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.api.service.ServeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Controller
@RequestMapping("/serve")
@Slf4j
public class ServePageC {

    @Autowired
    ServeService serveService;

    private String prefix = "aiplatform/serve";
    
    @GetMapping("/main")
    public String main() {
        return prefix + "/main";
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @GetMapping("/edit")
    public String edit(String taskId, ModelMap mmap) {
        ServeTask serveTask = serveService.findById(taskId);
        if (serveService.findById(taskId) == null) {
            return "error/500";
        }
        mmap.put("serveTask", serveTask);
        return prefix + "/edit";
    }
    
}
