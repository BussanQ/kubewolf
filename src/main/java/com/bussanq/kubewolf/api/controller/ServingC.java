package com.bussanq.kubewolf.api.controller;

import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.api.service.ServeService;
import com.bussanq.kubewolf.web.model.vo.ServeTaskReq;
import com.bussanq.kubewolf.web.res.ResultJson;
import com.bussanq.kubewolf.web.res.ResultTable;
import com.jfinal.plugin.activerecord.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        Page<ServeTask> datas = serveService.list(serveTask, page);
        return ResultTable.ok(datas.getTotalRow(), datas.getList());
    }

    @PostMapping("/start")
    public ResultJson start(@RequestBody ServeTaskReq taskReq) {
        return ResultJson.res(serveService.start(taskReq));
    }

    @PostMapping("/stop")
    public ResultJson stop(@RequestBody ServeTaskReq taskReq) {
        return ResultJson.res(serveService.stop(taskReq));
    }

    @PostMapping("/delete")
    public ResultJson delete(@RequestBody ServeTaskReq taskReq) {
        return ResultJson.res(serveService.delete(taskReq));
    }

    @PostMapping("/update")
    public ResultJson update(@RequestBody ServeTask serveTask) {
        return ResultJson.res(serveService.update(serveTask));
    }

    @PostMapping("/create")
    public ResultJson save(@RequestBody ServeTask serveTask) {
        return ResultJson.res(serveService.save(serveTask), serveTask.getTaskId());
    }
}
