package com.filipe.api.controller;

import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.dto.estoque.EstoqueAtualResponse;
import com.filipe.api.dto.estoque.MovimentacaoEstoqueRequest;
import com.filipe.api.dto.estoque.MovimentacaoEstoqueResponse;
import com.filipe.api.dto.estoque.SaidaManualEstoqueRequest;
import com.filipe.api.service.EstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService estoqueService;

    @PostMapping("/saidas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovimentacaoEstoqueResponse> registrarSaidaManual(
            @RequestBody @Valid SaidaManualEstoqueRequest request,
            Authentication authentication
    ) {
        Usuario usuario = authentication != null ? (Usuario) authentication.getPrincipal() : null;
        MovimentacaoEstoqueResponse response = estoqueService.registrarSaidaManual(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/movimentacoes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovimentacaoEstoqueResponse> registrarMovimentacao(
            @RequestBody @Valid MovimentacaoEstoqueRequest request,
            Authentication authentication
    ) {
        Usuario usuario = authentication != null ? (Usuario) authentication.getPrincipal() : null;
        MovimentacaoEstoqueResponse response = estoqueService.registrarMovimentacao(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<Page<EstoqueAtualResponse>> listarEstoqueAtual(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String codigoBarras,
            @RequestParam(required = false) UUID categoriaId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(estoqueService.listarEstoqueAtual(nome, codigoBarras, categoriaId, pageable));
    }

    @GetMapping("/movimentacoes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MovimentacaoEstoqueResponse>> listarMovimentacoes(
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @PageableDefault(size = 20, sort = "dataHora") Pageable pageable
    ) {
        return ResponseEntity.ok(estoqueService.listarMovimentacoes(produtoId, tipo, dataInicio, dataFim, pageable));
    }

    @GetMapping("/abaixo-minimo")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<List<EstoqueAtualResponse>> listarAbaixoMinimo() {
        return ResponseEntity.ok(estoqueService.listarAbaixoMinimo());
    }

    @PutMapping("/{produtoId}/minimo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> atualizarEstoqueMinimo(
            @PathVariable UUID produtoId,
            @RequestParam BigDecimal quantidadeMinima
    ) {
        estoqueService.atualizarEstoqueMinimo(produtoId, quantidadeMinima);
        return ResponseEntity.noContent().build();
    }
}
