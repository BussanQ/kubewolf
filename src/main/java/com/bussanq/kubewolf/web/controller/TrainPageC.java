package com.bussanq.kubewolf.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@Controller
@RequestMapping("/train")
@Slf4j
public class TrainPageC {

    private String prefix = "aiplatform/train";
    
    @GetMapping("/main")
    public String main() {
        return prefix + "/main";
    }
    
}
