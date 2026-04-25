package com.filipe.api.service;

import com.filipe.api.domain.caixa.LancamentoCaixa;
import com.filipe.api.domain.caixa.LancamentoCaixaRepository;
import com.filipe.api.domain.caixa.TipoLancamentoCaixa;
import com.filipe.api.domain.estoque.EstoqueAtualRepository;
import com.filipe.api.domain.venda.FormaPagamento;
import com.filipe.api.domain.venda.StatusVenda;
import com.filipe.api.domain.venda.Venda;
import com.filipe.api.domain.venda.VendaRepository;
import com.filipe.api.dto.estoque.EstoqueAtualResponse;
import com.filipe.api.mapper.estoque.EstoqueMapper;
import com.filipe.api.dto.dashboard.DashboardStatsResponse;
import com.filipe.api.mapper.venda.VendaMapper;
import com.filipe.api.dto.venda.VendaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final VendaRepository vendaRepository;
    private final EstoqueAtualRepository estoqueAtualRepository;
    private final LancamentoCaixaRepository lancamentoCaixaRepository;
    private final VendaMapper vendaMapper;
    private final EstoqueMapper estoqueMapper;

    public List<VendaResponse> relatorioVendasPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return vendaRepository.findByDataHoraBetween(inicio, fim).stream()
                .map(vendaMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EstoqueAtualResponse> relatorioPosicaoEstoque() {
        return estoqueAtualRepository.findAll().stream()
                .map(estoqueMapper::toEstoqueAtualResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Object> relatorioBalancoCaixa(UUID caixaId) {
        List<LancamentoCaixa> lancamentos = lancamentoCaixaRepository.findByCaixaId(caixaId);
        
        Map<FormaPagamento, BigDecimal> totaisPorForma = new HashMap<>();
        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSaidas = BigDecimal.ZERO;

        for (LancamentoCaixa l : lancamentos) {
            if (l.getTipo() == TipoLancamentoCaixa.ENTRADA) {
                totalEntradas = totalEntradas.add(l.getValor());
                if (l.getFormaPagamento() != null) {
                    totaisPorForma.merge(l.getFormaPagamento(), l.getValor(), BigDecimal::add);
                }
            } else {
                totalSaidas = totalSaidas.add(l.getValor());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("caixaId", caixaId);
        response.put("totalEntradas", totalEntradas);
        response.put("totalSaidas", totalSaidas);
        response.put("saldoLiquido", totalEntradas.subtract(totalSaidas));
        response.put("totaisPorFormaPagamento", totaisPorForma);
        
        return response;
    }

    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime trintaDiasAtras = LocalDateTime.now().minusDays(30);
        List<Venda> vendasRecentes = vendaRepository.findByDataHoraBetween(trintaDiasAtras, LocalDateTime.now())
                .stream()
                .filter(v -> v.getStatus() == StatusVenda.CONFIRMADA)
                .toList();

        BigDecimal faturamentoTotal = vendasRecentes.stream()
                .map(Venda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalVendas = vendasRecentes.size();
        BigDecimal ticketMedio = totalVendas > 0 
                ? faturamentoTotal.divide(BigDecimal.valueOf(totalVendas), 2, java.math.RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;

        long produtosAbaixoMinimo = estoqueAtualRepository.findAbaixoMinimo().size();

        // Vendas por dia (últimos 7 dias)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        Map<String, BigDecimal> vendasPorDiaMap = new TreeMap<>();
        for (int i = 6; i >= 0; i--) {
            vendasPorDiaMap.put(LocalDate.now().minusDays(i).format(formatter), BigDecimal.ZERO);
        }

        vendasRecentes.stream()
                .filter(v -> v.getDataHora().isAfter(LocalDateTime.now().minusDays(7)))
                .forEach(v -> {
                    String dia = v.getDataHora().format(formatter);
                    vendasPorDiaMap.merge(dia, v.getValorTotal(), BigDecimal::add);
                });

        List<DashboardStatsResponse.VendasPorDia> vendasRecentemente = vendasPorDiaMap.entrySet().stream()
                .map(e -> new DashboardStatsResponse.VendasPorDia(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // Faturamento por forma de pagamento
        Map<String, BigDecimal> faturamentoPorForma = new HashMap<>();
        vendasRecentes.forEach(v -> {
            v.getPagamentos().forEach(p -> {
                faturamentoPorForma.merge(p.getFormaPagamento().name(), p.getValor().subtract(p.getTroco()), BigDecimal::add);
            });
        });

        return new DashboardStatsResponse(
                faturamentoTotal,
                totalVendas,
                ticketMedio,
                produtosAbaixoMinimo,
                vendasRecentemente,
                List.of(), // TODO: Implement Top Products if needed
                faturamentoPorForma
        );
    }
}
