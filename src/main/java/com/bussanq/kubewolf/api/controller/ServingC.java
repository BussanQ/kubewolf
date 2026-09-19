package com.bussanq.kubewolf.api.controller;

import com.bussanq.kubewolf.api.service.ServeService;
import com.bussanq.kubewolf.web.model.vo.*;
import com.bussanq.kubewolf.web.res.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/serve")
public class ServingC {
    private final ServeService service;
    public ServingC(ServeService service) { this.service = service; }
    @GetMapping("/list")
    public ResultJson list(@Valid PageQuery page, @RequestParam(required = false) String taskName,
                           @RequestParam(required = false) String type) {
        var result = service.list(page, taskName, type);
        return ResultTable.ok(result.getTotalRow(), result.getList());
    }
    @GetMapping("/get")
    public ResultJson get(@RequestParam String taskId) { return ResultJson.ok(service.findById(taskId)); }
    @PostMapping("/create")
    public ResultJson create(@Valid @RequestBody ServeTaskInput request) { return ResultJson.ok(service.save(request)); }
    @PostMapping("/update")
    public ResultJson update(@Valid @RequestBody ServeTaskInput request) { return ResultJson.ok(service.update(request)); }
    @PostMapping("/startModel")
    public ResultJson startModel(@Valid @RequestBody DeployModelRequest request) {
        return ResultJson.ok(service.startModel(request)).setMessage("已受理，正在部署");
    }
    @PostMapping("/start")
    public ResultJson start(@Valid @RequestBody ServeTaskReq request) { service.intent(request, "running"); return accepted(); }
    @PostMapping("/stop")
    public ResultJson stop(@Valid @RequestBody ServeTaskReq request) { service.intent(request, "stopped"); return accepted(); }
    @PostMapping("/delete")
    public ResultJson delete(@Valid @RequestBody ServeTaskReq request) { service.intent(request, "deleted"); return accepted(); }
    private ResultJson accepted() { return ResultJson.ok("操作已受理，可在列表查看执行状态"); }
}
