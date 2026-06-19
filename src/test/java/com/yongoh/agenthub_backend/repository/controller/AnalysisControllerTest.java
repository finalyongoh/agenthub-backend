package com.yongoh.agenthub_backend.repository.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.yongoh.agenthub_backend.repository.service.AnalysisService;

@SpringBootTest
@AutoConfigureMockMvc
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    @Test
    void testCallbackEndpoint() throws Exception {
        UUID analysisId = UUID.randomUUID();
        String requestBody = """
            {
                "analysis_id": "%s",
                "status": "COMPLETED",
                "result_json": {"result":"ok"},
                "error_message": null
            }
            """.formatted(analysisId);

        doNothing().when(analysisService).updateStatus(eq(analysisId), eq("COMPLETED"), eq("{\"result\":\"ok\"}"), eq(null));

        mockMvc.perform(post("/api/v1/internal/analysis/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }
}
