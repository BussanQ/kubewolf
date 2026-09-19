package com.bussanq.kubewolf.api.controller;

import com.bussanq.kubewolf.web.res.ResultJson;
import com.bussanq.kubewolf.web.res.ResultTable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@RestController
@RequestMapping("/api/v1/train")
public class TrainAPIC {

    @GetMapping("/list")
    public ResultJson index() {
        return ResultTable.ok(0, List.of());
    }
}
