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
}
