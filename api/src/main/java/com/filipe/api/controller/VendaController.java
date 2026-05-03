package com.filipe.api.controller;

import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.domain.venda.StatusVenda;
import com.filipe.api.dto.fiscal.NotaFiscalResponse;
import com.filipe.api.dto.venda.AplicarDescontoVendaRequest;
import com.filipe.api.dto.venda.CancelarVendaRequest;
import com.filipe.api.dto.venda.FinalizarVendaRequest;
import com.filipe.api.dto.venda.ItemVendaRequest;
import com.filipe.api.dto.venda.VendaResponse;
import com.filipe.api.dto.venda.VendaStartRequest;
import com.filipe.api.service.NotaFiscalService;
import com.filipe.api.service.VendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;
    private final NotaFiscalService notaFiscalService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR','GERENTE','ADMIN')")
    public ResponseEntity<Page<VendaResponse>> list(
            @RequestParam(required = false) StatusVenda status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) Long numero,
            @RequestParam(required = false) UUID caixaId,
            @PageableDefault(size = 20, sort = "dataHora") Pageable pageable
    ) {
        return ResponseEntity.ok(vendaService.listarVendas(status, dataInicio, dataFim, numero, caixaId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR','GERENTE','ADMIN')")
    public ResponseEntity<VendaResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(vendaService.buscarVenda(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR','GERENTE','ADMIN')")
    public ResponseEntity<VendaResponse> start(
            @RequestBody @Valid VendaStartRequest request,
            Authentication authentication) {
        Usuario operador = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vendaService.iniciarVenda(request, operador));
    }

    @PostMapping("/{id}/itens")
    @PreAuthorize("hasAnyRole('OPERADOR','GERENTE','ADMIN')")
    public ResponseEntity<VendaResponse> addItem(
            @PathVariable UUID id,
            @RequestBody @Valid ItemVendaRequest request) {
        return ResponseEntity.ok(vendaService.adicionarItem(id, request));
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    @PreAuthorize("hasAnyRole('OPERADOR','GERENTE','ADMIN')")
    public ResponseEntity<VendaResponse> removeItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(vendaService.removerItem(id, itemId));
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('OPERADOR','GERENTE','ADMIN')")
    public ResponseEntity<VendaResponse> finalizar(
            @PathVariable UUID id,
            @RequestBody @Valid FinalizarVendaRequest request) {
        return ResponseEntity.ok(vendaService.finalizarVenda(id, request));
    }

    @PatchMapping("/{id}/desconto")
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public ResponseEntity<VendaResponse> aplicarDesconto(
            @PathVariable UUID id,
            @RequestBody @Valid AplicarDescontoVendaRequest request) {
        return ResponseEntity.ok(vendaService.aplicarDescontoVenda(id, request.desconto()));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public ResponseEntity<VendaResponse> cancelar(
            @PathVariable UUID id,
            @RequestBody @Valid CancelarVendaRequest request) {
        return ResponseEntity.ok(vendaService.cancelarVenda(id, request));
    }

    @PostMapping("/{id}/nota-fiscal")
    @PreAuthorize("hasAnyRole('OPERADOR','GERENTE','ADMIN')")
    public ResponseEntity<NotaFiscalResponse> gerarNotaFiscal(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notaFiscalService.emitirNotaFiscal(id));
    }

    @GetMapping("/{id}/nota-fiscal")
    @PreAuthorize("hasAnyRole('OPERADOR','GERENTE','ADMIN')")
    public ResponseEntity<NotaFiscalResponse> consultarNotaFiscal(@PathVariable UUID id) {
        return ResponseEntity.ok(notaFiscalService.buscarPorVenda(id));
    }

    @GetMapping("/{id}/nota-fiscal/danfe")
    @PreAuthorize("hasAnyRole('OPERADOR','GERENTE','ADMIN')")
    public ResponseEntity<byte[]> downloadDanfe(@PathVariable UUID id) {
        byte[] pdf = notaFiscalService.gerarDanfe(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=danfe_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}