package com.filipe.api.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardStatsResponse(
    BigDecimal faturamentoTotal,
    Long totalVendas,
    BigDecimal ticketMedio,
    Long produtosAbaixoMinimo,
    List<VendasPorDia> vendasRecentemente,
    List<TopProduto> topProdutos,
    Map<String, BigDecimal> faturamentoPorFormaPagamento
) {
    public record VendasPorDia(String data, BigDecimal total) {}
    public record TopProduto(String nome, Long quantidade, BigDecimal total) {}
}
