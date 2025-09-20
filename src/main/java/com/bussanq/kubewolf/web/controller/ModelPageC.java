package com.bussanq.kubewolf.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author bussanq
 * @date 2025/09/20
 */
@Controller
@RequestMapping("/model")
@Slf4j
public class ModelPageC {

    private String prefix = "aiplatform/model";
    
    @GetMapping("/main")
    public String main() {
        return prefix + "/main";
    }
    
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }
    
}
