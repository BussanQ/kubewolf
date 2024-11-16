package com.bussanq.kubewolf.api.controller;

import com.bussanq.kubewolf.api.service.ServingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@RestController
public class ServingC {

    @Autowired
    ServingService servingService;

    @GetMapping("/startTask")
    public String index() {
        return "";
    }
}
