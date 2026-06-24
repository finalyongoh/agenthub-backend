package com.yongoh.agenthub_backend.repository.controller;

import com.yongoh.agenthub_backend.repository.service.AnalysisService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import java.util.UUID;
import tools.jackson.databind.annotation.JsonNaming;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/internal/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService service;

    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@RequestBody CallbackRequest request) {
        String resultJsonStr = request.getResultJson() != null ? request.getResultJson().toString() : null;
        service.updateStatus(request.getAnalysisId(), request.getStatus(), resultJsonStr, request.getErrorMessage());
        return ResponseEntity.ok().build();
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CallbackRequest {
        private UUID analysisId;
        private String status;
        private JsonNode resultJson;
        private String errorMessage;
    }
}
