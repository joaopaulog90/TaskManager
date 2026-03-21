package com.taskmanager.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Health", description = "Status da API")
public class ControladorSaude {

    @GetMapping("/api/health")
    @Operation(summary = "Verifica se a API está no ar")
    public Map<String, String> saude() {
        return Map.of("status", "UP");
    }
}
