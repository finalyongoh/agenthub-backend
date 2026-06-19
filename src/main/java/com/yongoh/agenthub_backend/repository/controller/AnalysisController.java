package com.yongoh.agenthub_backend.repository.controller;

import com.yongoh.agenthub_backend.repository.service.AnalysisService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService service;

    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@RequestBody CallbackRequest request) {
        service.updateStatus(request.getAnalysisId(), request.getStatus(), request.getResultJson(), request.getErrorMessage());
        return ResponseEntity.ok().build();
    }

    @Data
    public static class CallbackRequest {
        private UUID analysisId;
        private String status;
        private String resultJson;
        private String errorMessage;
    }
}
