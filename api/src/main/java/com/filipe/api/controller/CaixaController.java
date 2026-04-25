package com.filipe.api.controller;

import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.dto.caixa.AbrirCaixaRequest;
import com.filipe.api.dto.caixa.CaixaResponse;
import com.filipe.api.dto.caixa.FecharCaixaRequest;
import com.filipe.api.dto.caixa.LancamentoManualCaixaRequest;
import com.filipe.api.dto.caixa.LancamentoCaixaResponse;
import com.filipe.api.domain.caixa.StatusCaixa;
import com.filipe.api.domain.caixa.TipoLancamentoCaixa;
import com.filipe.api.service.CaixaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/caixas")
@RequiredArgsConstructor
public class CaixaController {

    private final CaixaService caixaService;

    @PostMapping("/abrir")
    public ResponseEntity<CaixaResponse> abrir(
            @RequestBody @Valid AbrirCaixaRequest request,
            Authentication authentication
    ) {
        Usuario usuario = authentication != null ? (Usuario) authentication.getPrincipal() : null;
        CaixaResponse response = caixaService.abrirCaixa(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CaixaResponse>> listar(
            @RequestParam(required = false) StatusCaixa status,
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim,
            Authentication authentication
    ) {
        Usuario usuario = authentication != null ? (Usuario) authentication.getPrincipal() : null;
        return ResponseEntity.ok(caixaService.listarCaixas(usuario, status, dataInicio, dataFim));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaixaResponse> buscar(@PathVariable UUID id, Authentication authentication) {
        Usuario usuario = authentication != null ? (Usuario) authentication.getPrincipal() : null;
        return ResponseEntity.ok(caixaService.buscarCaixa(id, usuario));
    }

    @GetMapping("/{id}/lancamentos")
    public ResponseEntity<List<LancamentoCaixaResponse>> listarLancamentos(
            @PathVariable UUID id,
            @RequestParam(required = false) TipoLancamentoCaixa tipo,
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim,
            Authentication authentication
    ) {
        Usuario usuario = authentication != null ? (Usuario) authentication.getPrincipal() : null;
        return ResponseEntity.ok(caixaService.listarLancamentos(id, usuario, tipo, dataInicio, dataFim));
    }

    @PostMapping("/{id}/lancamentos/entrada")
    public ResponseEntity<LancamentoCaixaResponse> registrarEntradaManual(
            @PathVariable UUID id,
            @RequestBody @Valid LancamentoManualCaixaRequest request,
            Authentication authentication
    ) {
        Usuario usuario = authentication != null ? (Usuario) authentication.getPrincipal() : null;
        LancamentoCaixaResponse response = caixaService.registrarEntradaManual(id, request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/lancamentos/saida")
    public ResponseEntity<LancamentoCaixaResponse> registrarSaidaManual(
            @PathVariable UUID id,
            @RequestBody @Valid LancamentoManualCaixaRequest request,
            Authentication authentication
    ) {
        Usuario usuario = authentication != null ? (Usuario) authentication.getPrincipal() : null;
        LancamentoCaixaResponse response = caixaService.registrarSaidaManual(id, request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/suprimento")
    public ResponseEntity<LancamentoCaixaResponse> suprimento(
            @PathVariable UUID id,
            @RequestBody @Valid LancamentoManualCaixaRequest request,
            Authentication authentication
    ) {
        Usuario usuario = authentication != null ? (Usuario) authentication.getPrincipal() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(caixaService.registrarEntradaManual(id, request, usuario));
    }

    @PostMapping("/{id}/sangria")
    public ResponseEntity<LancamentoCaixaResponse> sangria(
            @PathVariable UUID id,
            @RequestBody @Valid LancamentoManualCaixaRequest request,
            Authentication authentication
    ) {
        Usuario usuario = authentication != null ? (Usuario) authentication.getPrincipal() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(caixaService.registrarSaidaManual(id, request, usuario));
    }

    @PostMapping("/{id}/fechar")
    public ResponseEntity<CaixaResponse> fechar(
            @PathVariable UUID id,
            @RequestBody @Valid FecharCaixaRequest request,
            Authentication authentication
    ) {
        Usuario usuario = authentication != null ? (Usuario) authentication.getPrincipal() : null;
        CaixaResponse response = caixaService.fecharCaixa(id, request, usuario);
        return ResponseEntity.ok(response);
    }
}
