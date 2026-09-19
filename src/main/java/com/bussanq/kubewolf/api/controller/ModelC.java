package com.bussanq.kubewolf.api.controller;

import com.bussanq.kubewolf.api.service.ModelService;
import com.bussanq.kubewolf.web.model.vo.*;
import com.bussanq.kubewolf.web.res.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/models")
public class ModelC {
    private final ModelService service;
    public ModelC(ModelService service) { this.service = service; }
    @GetMapping("/list")
    public ResultJson list(@Valid PageQuery page, @RequestParam(required = false) String name,
                           @RequestParam(required = false) String modelPath) {
        var result = service.list(page, name, modelPath);
        return ResultTable.ok(result.getTotalRow(), result.getList());
    }
    @GetMapping("/get")
    public ResultJson get(@RequestParam String id) { return ResultJson.ok(service.findById(id)); }
    @PostMapping("/create")
    public ResultJson create(@Valid @RequestBody ModelRequest request) { return ResultJson.ok(service.save(request)); }
    @PostMapping("/update")
    public ResultJson update(@Valid @RequestBody ModelRequest request) { return ResultJson.ok(service.update(request)); }
    @PostMapping("/delete")
    public ResultJson delete(@Valid @RequestBody IdRequest request) { service.delete(request.getId()); return ResultJson.ok(); }
}
