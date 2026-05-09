package com.filipe.api.controller;

import com.filipe.api.domain.configuracao.Configuracao;
import com.filipe.api.domain.configuracao.ConfiguracaoRepository;
import com.filipe.api.exception.BusinessException;
import com.filipe.api.service.PixService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pix")
@RequiredArgsConstructor
public class PixController {

    private final PixService pixService;
    private final ConfiguracaoRepository configuracaoRepository;

    @GetMapping("/generate")
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE', 'ADMIN')")
    public ResponseEntity<Map<String, String>> generate(@RequestParam BigDecimal valor) throws Exception {
        Configuracao config = configuracaoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new BusinessException("Configuração do sistema não encontrada"));
        
        if (config.getPixChave() == null || config.getPixChave().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Chave PIX não configurada no sistema"));
        }

        String payload = pixService.generatePayload(
                config.getPixChave(),
                config.getPixBeneficiario(),
                config.getPixCidade() != null ? config.getPixCidade() : "SAO PAULO",
                valor
        );
        
        String qrCode = pixService.generateQrCodeBase64(payload);
        
        return ResponseEntity.ok(Map.of(
                "payload", payload,
                "qrCode", qrCode
        ));
    }
}
