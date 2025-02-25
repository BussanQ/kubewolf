package com.bussanq.kubewolf.api.controller;

import com.bussanq.kubewolf.api.model.TaskInfo;
import com.bussanq.kubewolf.api.model.dto.ModelTpl;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.api.service.ModelService;
import com.bussanq.kubewolf.web.model.vo.ServeTaskReq;
import com.bussanq.kubewolf.web.res.ResultJson;
import com.bussanq.kubewolf.web.res.ResultTable;
import com.jfinal.plugin.activerecord.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bussanq
 * @date 2024/11/09
 */
@RestController
@RequestMapping("/api/v1/models")
public class ModelC {

    @Autowired
    ModelService modelService;

    @GetMapping("/list")
    public ResultJson list() {
        List<ModelTpl> datas = modelService.listAll();
        return ResultTable.ok(datas);
    }

    @PostMapping("/delete")
    public ResultJson delete(String id) {
        return ResultJson.res(modelService.delete(id));
    }

    @PostMapping("/update")
    public ResultJson update(@RequestBody ModelTpl modelTpl) {
        return ResultJson.res(modelService.update(modelTpl));
    }

    @PostMapping("/create")
    public ResultJson save(@RequestBody ModelTpl modelTpl) {
        return ResultJson.res(modelService.save(modelTpl), modelTpl.getId());
    }
}
