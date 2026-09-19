package com.bussanq.kubewolf.web;

import com.bussanq.kubewolf.api.controller.TrainAPIC;
import com.bussanq.kubewolf.web.controller.TrainPageC;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrainPageTest {
    @Test
    void addButtonTargetResolvesToTheTrainingForm() throws Exception {
        MockMvcBuilders.standaloneSetup(new TrainPageC()).build()
                .perform(get("/train/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("aiplatform/train/add"));
    }

    @Test
    void placeholderListUsesTheTableResponseContract() throws Exception {
        MockMvcBuilders.standaloneSetup(new TrainAPIC()).build()
                .perform(get("/api/v1/train/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
