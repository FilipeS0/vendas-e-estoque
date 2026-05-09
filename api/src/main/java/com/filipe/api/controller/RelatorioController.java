package com.filipe.api.controller;

import com.filipe.api.dto.relatorio.*;
import com.filipe.api.dto.dashboard.DashboardStatsResponse;
import com.filipe.api.dto.venda.VendaResponse;
import com.filipe.api.dto.estoque.EstoqueAtualResponse;
import com.filipe.api.dto.estoque.MovimentacaoEstoqueResponse;
import com.filipe.api.service.RelatorioService;
import com.filipe.api.shared.report.PdfReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @GetMapping("/vendas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<VendaResponse>> getVendas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim
    ) {
        return ResponseEntity.ok(relatorioService.relatorioVendasPeriodo(inicio, fim));
    }

    @GetMapping("/vendas/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<byte[]> getVendasPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim
    ) {
        List<VendaResponse> data = relatorioService.relatorioVendasPeriodo(inicio, fim);
        String period = inicio.toString() + " a " + fim.toString();
        byte[] pdf = pdfReportGenerator.gerarRelatorioVendas(data, period);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vendas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/vendas/hoje")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<VendaResponse>> getVendasHoje() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fim = LocalDate.now().atTime(23, 59, 59);
        return ResponseEntity.ok(relatorioService.relatorioVendasPeriodo(inicio, fim));
    }

    @GetMapping("/vendas/hoje/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<byte[]> getVendasHojePdf() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fim = LocalDate.now().atTime(23, 59, 59);
        List<VendaResponse> data = relatorioService.relatorioVendasPeriodo(inicio, fim);
        String period = "Hoje (" + LocalDate.now().toString() + ")";
        byte[] pdf = pdfReportGenerator.gerarRelatorioVendas(data, period);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vendas_hoje.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/estoque/posicao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<EstoqueAtualResponse>> getEstoquePosicao() {
        return ResponseEntity.ok(relatorioService.relatorioPosicaoEstoque());
    }

    @GetMapping("/estoque/posicao/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<byte[]> getEstoquePosicaoPdf() {
        List<EstoqueAtualResponse> data = relatorioService.relatorioPosicaoEstoque();
        byte[] pdf = pdfReportGenerator.gerarRelatorioEstoque(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=estoque_posicao.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/estoque/abaixo-minimo/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<byte[]> getEstoqueAbaixoMinimoPdf() {
        List<EstoqueAtualResponse> data = relatorioService.relatorioEstoqueAbaixoMinimo();
        byte[] pdf = pdfReportGenerator.gerarRelatorioEstoque(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=estoque_abaixo_minimo.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/estoque/movimentacoes/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<byte[]> getMovimentacoesEstoquePdf(
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        List<MovimentacaoEstoqueResponse> data = relatorioService.relatorioMovimentacoesEstoque(produtoId, tipo, dataInicio, dataFim);
        String period = (dataInicio != null ? dataInicio.toLocalDate().toString() : "Tudo") + " a " + (dataFim != null ? dataFim.toLocalDate().toString() : "Tudo");
        byte[] pdf = pdfReportGenerator.gerarRelatorioMovimentacaoEstoque(data, period);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=movimentacoes_estoque.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/caixa/balanco/{caixaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Map<String, Object>> getBalancoCaixa(@PathVariable UUID caixaId) {
        return ResponseEntity.ok(relatorioService.relatorioBalancoCaixa(caixaId));
    }

    @GetMapping("/caixa/balanco/{caixaId}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<byte[]> getBalancoCaixaPdf(@PathVariable UUID caixaId) {
        Map<String, Object> data = relatorioService.relatorioBalancoCaixa(caixaId);
        String period = "Caixa ID: " + caixaId.toString();
        byte[] pdf = pdfReportGenerator.gerarRelatorioBalancoCaixa(data, period);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=balanco_caixa.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/contas-a-receber/resumo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ContasAReceberResumoResponse> getContasAReceberResumo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return ResponseEntity.ok(relatorioService.relatorioResumoContasAReceber(dataInicio, dataFim));
    }

    @GetMapping("/contas-a-receber/resumo/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<byte[]> getContasAReceberResumoPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        ContasAReceberResumoResponse data = relatorioService.relatorioResumoContasAReceber(dataInicio, dataFim);
        byte[] pdf = pdfReportGenerator.gerarRelatorioContasAReceber(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contas_a_receber.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/vendas/forma-pagamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<VendaFormaPagamentoResponse>> getVendasPorFormaPagamento(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim
    ) {
        return ResponseEntity.ok(relatorioService.relatorioVendasPorFormaPagamento(inicio, fim));
    }

    @GetMapping("/vendas/forma-pagamento/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<byte[]> getVendasPorFormaPagamentoPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim
    ) {
        List<VendaFormaPagamentoResponse> data = relatorioService.relatorioVendasPorFormaPagamento(inicio, fim);
        String period = inicio.toString() + " a " + fim.toString();
        byte[] pdf = pdfReportGenerator.gerarRelatorioVendasPorFormaPagamento(data, period);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vendas_forma_pagamento.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/produtos/ranking")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<VendaProdutoResponse>> getRankingProdutos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim
    ) {
        return ResponseEntity.ok(relatorioService.relatorioVendasPorProduto(inicio, fim));
    }

    @GetMapping("/produtos/ranking/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<byte[]> getRankingProdutosPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim
    ) {
        List<VendaProdutoResponse> data = relatorioService.relatorioVendasPorProduto(inicio, fim);
        String period = inicio.toString() + " a " + fim.toString();
        byte[] pdf = pdfReportGenerator.gerarRelatorioRankingProdutos(data, period);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ranking_produtos.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(
            @RequestParam(required = false) Integer dias
    ) {
        return ResponseEntity.ok(relatorioService.getDashboardStats(dias));
    }
}

