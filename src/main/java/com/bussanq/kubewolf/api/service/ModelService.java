package com.bussanq.kubewolf.api.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.bussanq.kubewolf.ai.service.AIService;
import com.bussanq.kubewolf.api.model.TaskInfo;
import com.bussanq.kubewolf.api.model.dto.ModelTpl;
import com.bussanq.kubewolf.api.model.dto.ServeTask;
import com.bussanq.kubewolf.common.utils.DbUtil;
import com.bussanq.kubewolf.web.model.vo.ServeTaskReq;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author bussanq
 * @date 2025/02/25
 */
@Slf4j
@Service
public class ModelService {

    public boolean save(ModelTpl serveTask) {
        serveTask.setId(IdUtil.fastSimpleUUID());
        Date now = new Date();
        serveTask.setCreateTime(now);
        serveTask.setUpdateTime(now);
        if(StrUtil.isBlank(serveTask.getType())) {
            serveTask.setType("run");
        }
        if (serveTask.getReplicas() == null || serveTask.getReplicas() == 0) {
            serveTask.setReplicas(1);
        }
        return serveTask.save();
    }

    public boolean update(ModelTpl modelTpl) {
        modelTpl.setUpdateTime(new Date());
        return modelTpl.update();
    }

    public boolean delete(String taskId){
        return ModelTpl.dao.deleteById(taskId);
    }

   /* public ServeTask findByIdOrName(ServeTaskReq taskReq) {
        return StrUtil.isNotBlank(taskReq.getTaskId()) ? findById(taskReq.getTaskId()):findByName(taskReq.getTaskName());
    }*/

    public ModelTpl findById(String taskId) {
        return ModelTpl.dao.findById(taskId);
    }
    public List<ModelTpl> listAll() {
        return ModelTpl.dao.findAll();
    }

   /* public ServeTask findByName(String taskName) {
        if (StrUtil.isNotBlank(taskName)) {
            return DbUtil.searchFirst(ServeTask.dao, Kv.by("task_name=", taskName));
        }
        return null;
    }*/
}
