package com.bussanq.kubewolf.ai.service;

import com.bussanq.kubewolf.api.model.TaskInfo;
import com.bussanq.kubewolf.api.model.dto.ServeTask;

/**
 * @author bussanq
 * @date 2024/11/14
 */
public interface AIService {

    TaskInfo startServing(ServeTask task);
    boolean stopServing(ServeTask task);
}
