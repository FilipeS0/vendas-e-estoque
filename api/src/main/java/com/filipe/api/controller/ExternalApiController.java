package com.filipe.api.controller;

import com.filipe.api.service.BrasilApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/external")
@RequiredArgsConstructor
public class ExternalApiController {

    private final BrasilApiService brasilApiService;

    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<Map<String, Object>> consultarCnpj(@PathVariable String cnpj) {
        Map<String, Object> result = brasilApiService.consultarCnpj(cnpj);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cep/{cep}")
    public ResponseEntity<Map<String, Object>> consultarCep(@PathVariable String cep) {
        Map<String, Object> result = brasilApiService.consultarCep(cep);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }
}
