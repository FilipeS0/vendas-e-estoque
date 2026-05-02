package com.filipe.api.controller;

import com.filipe.api.dto.configuracao.ConfiguracaoRequest;
import com.filipe.api.dto.configuracao.ConfiguracaoResponse;
import com.filipe.api.service.ConfiguracaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/configuracoes")
@RequiredArgsConstructor
public class ConfiguracaoController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    public ResponseEntity<ConfiguracaoResponse> obterConfiguracao() {
        ConfiguracaoResponse response = configuracaoService.obterConfiguracao();
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ConfiguracaoResponse> salvarConfiguracao(@Valid @RequestBody ConfiguracaoRequest request) {
        return ResponseEntity.ok(configuracaoService.salvarConfiguracao(request));
    }
}
