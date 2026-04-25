package com.filipe.api.controller;

import com.filipe.api.dto.estoque.EstoqueAtualResponse;
import com.filipe.api.dto.venda.VendaResponse;
import com.filipe.api.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/vendas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VendaResponse>> relatorioVendas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(relatorioService.relatorioVendasPeriodo(inicio, fim));
    }

    @GetMapping("/estoque/posicao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EstoqueAtualResponse>> relatorioEstoque() {
        return ResponseEntity.ok(relatorioService.relatorioPosicaoEstoque());
    }

    @GetMapping("/caixa/balanco/{caixaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> relatorioCaixa(@PathVariable UUID caixaId) {
        return ResponseEntity.ok(relatorioService.relatorioBalancoCaixa(caixaId));
    }
}
