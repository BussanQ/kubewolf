package com.bussanq.kubewolf.web;

import com.bussanq.kubewolf.api.controller.*;
import com.bussanq.kubewolf.api.service.*;
import com.bussanq.kubewolf.web.security.*;
import com.bussanq.kubewolf.web.config.ExceptHandler;
import com.bussanq.kubewolf.web.model.vo.PageQuery;
import com.jfinal.plugin.activerecord.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ModelC.class,ServingC.class,AuthController.class})
@Import({SecurityConfig.class,ExceptHandler.class})
@TestPropertySource(properties={"auth.admin-password=test-admin-password-123", "auth.viewer-password=test-viewer-password-123"})
class ApiSecurityTest {
    @Autowired MockMvc mvc;
    @MockBean ModelService models;
    @MockBean ServeService serving;

    @Test void requiresAuthentication() throws Exception {
        mvc.perform(get("/api/v1/models/list")).andExpect(status().isUnauthorized());
    }
    @Test void viewerCanReadButCannotWrite() throws Exception {
        when(models.list(any(),any(),any())).thenReturn(new Page<>(List.of(),1,10,0,0));
        mvc.perform(get("/api/v1/models/list").with(user("viewer").roles("VIEWER"))).andExpect(status().isOk());
        mvc.perform(post("/api/v1/models/delete").with(user("viewer").roles("VIEWER")).with(csrf())
                .contentType("application/json").content("{\"id\":\"id\"}")).andExpect(status().isForbidden());
        verify(models,never()).delete(any());
    }
    @Test void administratorStillNeedsCsrf() throws Exception {
        mvc.perform(post("/api/v1/models/delete").with(user("admin").roles("ADMIN"))
                .contentType("application/json").content("{\"id\":\"id\"}")).andExpect(status().isForbidden());
    }
    @Test void deleteAcceptsTheDocumentedJsonId() throws Exception {
        mvc.perform(post("/api/v1/models/delete").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"id\":\"model-id\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        verify(models).delete("model-id");
    }
    @Test void rejectsBadInputAndOversizedPages() throws Exception {
        mvc.perform(post("/api/v1/models/create").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"name\":\"x\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/models/list?pageSize=1000").with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }
    @Test void bindsTheRequestedPageNumber() throws Exception {
        when(models.list(any(),any(),any())).thenReturn(new Page<>(List.of(),2,3,3,8));
        mvc.perform(get("/api/v1/models/list?pageNum=2&pageSize=3").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        verify(models).list(argThat(q -> q.getPageNum()==2 && q.getPageSize()==3),isNull(),isNull());
    }
    @Test void missingIdsAndWrongMethodsKeepTheirHttpStatus() throws Exception {
        mvc.perform(get("/api/v1/models/get").with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/models/delete").with(user("admin").roles("ADMIN")))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/not-found").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }
    @Test void authenticatesWithTheLoginFormAndRejectsBadPassword() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("userName","admin").param("passWord","wrong"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/login").with(csrf()).param("userName","admin").param("passWord","test-admin-password-123"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }
}
