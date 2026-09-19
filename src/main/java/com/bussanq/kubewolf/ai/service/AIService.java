package com.bussanq.kubewolf.ai.service;

import com.bussanq.kubewolf.api.model.dto.ServeTask;

public interface AIService {
    record ServingStatus(String state, String detail) { }
    void ensureServing(ServeTask task);
    boolean stopServing(ServeTask task);
    ServingStatus status(ServeTask task);
    boolean ready();
}
