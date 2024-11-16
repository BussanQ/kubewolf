package com.bussanq.kubewolf.ai.service;

import com.bussanq.kubewolf.api.model.TaskInfo;
import com.bussanq.kubewolf.api.model.dto.ServingTask;

/**
 * @author bussanq
 * @date 2024/11/14
 */
public interface AIService {

    TaskInfo startServingTask(ServingTask task);
}
