package com.filipe.api.controller;

import com.filipe.api.dto.relatorio.FluxoCaixaResponse;
import com.filipe.api.service.RelatorioService;
import com.filipe.api.shared.report.PdfReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final PdfReportGenerator pdfReportGenerator;

    @GetMapping("/fluxo-caixa")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<FluxoCaixaResponse> getFluxoCaixa(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(relatorioService.obterFluxoCaixa(inicio, fim));
    }

    @GetMapping("/fluxo-caixa/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<byte[]> getFluxoCaixaPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        FluxoCaixaResponse data = relatorioService.obterFluxoCaixa(inicio, fim);
        String period = inicio.toString() + " a " + fim.toString();
        byte[] pdf = pdfReportGenerator.gerarRelatorioFluxoCaixa(data, period);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fluxo_caixa.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
