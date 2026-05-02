package com.filipe.api.controller;

import com.filipe.api.dto.configuracao.ConfiguracaoRequest;
import com.filipe.api.dto.configuracao.ConfiguracaoResponse;
import com.filipe.api.service.ConfiguracaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

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

    @PostMapping("/certificado")
    public ResponseEntity<Void> uploadCertificado(
            @RequestParam("file") MultipartFile file,
            @RequestParam("senha") String senha
    ) {
        configuracaoService.salvarCertificado(file, senha);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/certificado/status")
    public ResponseEntity<Map<String, Object>> statusCertificado() {
        return ResponseEntity.ok(configuracaoService.getStatusCertificado());
    }
}
