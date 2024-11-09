package com.bussanq.kubewolf.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@RestController
public class IndexC {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
