package com.filipe.api.mapper.caixa;

import com.filipe.api.domain.caixa.Caixa;
import com.filipe.api.domain.caixa.LancamentoCaixa;
import com.filipe.api.dto.caixa.CaixaResponse;
import com.filipe.api.dto.caixa.LancamentoCaixaResponse;
import org.springframework.stereotype.Component;

@Component
public class CaixaMapper {

    public CaixaResponse toResponse(Caixa caixa) {
        return new CaixaResponse(
                caixa.getId(),
                caixa.getOperador() != null ? caixa.getOperador().getId() : null,
                caixa.getOperador() != null ? caixa.getOperador().getNome() : null,
                caixa.getDataAbertura(),
                caixa.getDataFechamento(),
                caixa.getValorAbertura(),
                caixa.getValorFechamentoSistema(),
                caixa.getValorFechamentoFisico(),
                caixa.getDiferenca(),
                caixa.getStatus()
        );
    }

    public LancamentoCaixaResponse toLancamentoResponse(LancamentoCaixa lancamento) {
        return new LancamentoCaixaResponse(
                lancamento.getId(),
                lancamento.getCaixa().getId(),
                lancamento.getTipo(),
                lancamento.getFormaPagamento(),
                lancamento.getValor(),
                lancamento.getDescricao(),
                lancamento.getReferenciaId(),
                lancamento.getDataHora(),
                lancamento.getUsuario() != null ? lancamento.getUsuario().getId() : null,
                lancamento.getUsuario() != null ? lancamento.getUsuario().getNome() : null
        );
    }
}
