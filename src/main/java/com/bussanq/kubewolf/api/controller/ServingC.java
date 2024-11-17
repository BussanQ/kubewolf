package com.bussanq.kubewolf.api.controller;

import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.api.service.ServeService;
import com.bussanq.kubewolf.web.res.ResultJson;
import com.bussanq.kubewolf.web.res.ResultTable;
import com.jfinal.plugin.activerecord.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@RestController
@RequestMapping("/api/v1/serve")
public class ServingC {

    @Autowired
    ServeService serveService;

    @GetMapping("/list")
    public ResultJson list(ServeTask serveTask, Page page) {
        Page<ServeTask> datas = serveService.listServeTasks(serveTask, page);
        return ResultTable.ok(datas.getTotalRow(), datas.getList());
    }

    @GetMapping("/startTask")
    public String index() {
        return "";
    }
}
