package com.bussanq.kubewolf.api.controller;

import com.bussanq.kubewolf.api.service.ConsoleService;
import com.bussanq.kubewolf.web.res.ResultJson;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/console")
public class ConsoleC {
    private final ConsoleService service;

    public ConsoleC(ConsoleService service) {
        this.service = service;
    }

    @GetMapping
    public ResultJson metrics() {
        return ResultJson.ok(service.query());
    }
}
