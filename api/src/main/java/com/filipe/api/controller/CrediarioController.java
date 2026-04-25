package com.filipe.api.controller;

import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.domain.venda.StatusParcela;
import com.filipe.api.dto.crediario.LiquidarParcelaRequest;
import com.filipe.api.dto.crediario.ParcelaResponse;
import com.filipe.api.service.CrediarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crediarios")
@RequiredArgsConstructor
public class CrediarioController {

    private final CrediarioService crediarioService;

    @GetMapping("/parcelas")
    public ResponseEntity<Page<ParcelaResponse>> listarParcelas(
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) StatusParcela status,
            Pageable pageable) {
        return ResponseEntity.ok(crediarioService.listarParcelas(clienteId, status, pageable));
    }

    @PostMapping("/parcelas/{id}/liquidar")
    public ResponseEntity<ParcelaResponse> liquidarParcela(
            @PathVariable UUID id,
            @RequestBody @Valid LiquidarParcelaRequest request,
            Authentication authentication) {
        Usuario operador = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(crediarioService.liquidarParcela(id, request, operador));
    }
}
