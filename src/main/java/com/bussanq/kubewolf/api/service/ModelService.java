package com.bussanq.kubewolf.api.service;

import cn.hutool.core.util.IdUtil;
import com.bussanq.kubewolf.api.model.dto.ModelTpl;
import com.bussanq.kubewolf.api.validation.TaskValidation;
import com.bussanq.kubewolf.common.error.ApiException;
import com.bussanq.kubewolf.web.model.vo.*;
import com.jfinal.plugin.activerecord.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@DependsOn("activeRecordPlugin")
public class ModelService {
    public ModelTpl findById(String id) {
        ModelTpl model = ModelTpl.dao.findById(id);
        if (model == null) throw new ApiException(404, "未找到模型");
        return model;
    }
    public Page<ModelTpl> list(PageQuery page, String name, String modelPath) {
        List<Object> args = new ArrayList<>();
        String sql = "from model_tpl where 1=1";
        if (name != null && !name.isBlank()) { sql += " and name like ?"; args.add("%" + name + "%"); }
        if (modelPath != null && !modelPath.isBlank()) { sql += " and model_path=?"; args.add(modelPath); }
        return ModelTpl.dao.paginate(page.getPageNum(), page.getPageSize(), "select *", sql + " order by create_time desc, id", args.toArray());
    }
    public ModelTpl save(ModelRequest request) {
        ModelTpl model = fill(new ModelTpl(), request);
        model.setId(IdUtil.fastSimpleUUID());
        model.save();
        return findById(model.getId());
    }
    public ModelTpl update(ModelRequest request) {
        if (request.getId() == null || request.getId().isBlank()) throw new ApiException(400, "缺少模型 ID");
        ModelTpl[] result = new ModelTpl[1];
        Db.tx(() -> {
            ModelTpl model = ModelTpl.dao.findFirst("select * from model_tpl where id=? for update", request.getId());
            if (model == null) throw new ApiException(404, "未找到模型");
            if (!Objects.equals(model.getCode(), request.getCode()) && referenced(model))
                throw new ApiException(409, "模型已被服务引用，不能修改 PVC 名称");
            fill(model, request).update();
            result[0] = findById(model.getId());
            return true;
        });
        return result[0];
    }
    public void delete(String id) {
        Db.tx(() -> {
            ModelTpl model = ModelTpl.dao.findFirst("select * from model_tpl where id=? for update", id);
            if (model == null) return true;
            if (referenced(model)) throw new ApiException(409, "模型仍被服务引用，请先删除相关服务");
            return model.delete();
        });
    }
    private boolean referenced(ModelTpl model) {
        return Db.queryLong("select count(*) from serve_task where model_id=? or model_code=?", model.getId(), model.getCode()) > 0;
    }
    private ModelTpl fill(ModelTpl model, ModelRequest request) {
        TaskValidation.environment(request.getEnv());
        return model.setName(request.getName().trim()).setCode(request.getCode()).setModelPath(request.getModelPath())
                .setType(request.getType()).setEnv(request.getEnv()).setCustom(request.getCustom());
    }
}
