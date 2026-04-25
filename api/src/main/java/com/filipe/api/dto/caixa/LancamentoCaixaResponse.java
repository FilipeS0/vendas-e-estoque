package com.filipe.api.dto.caixa;

import com.filipe.api.domain.caixa.TipoLancamentoCaixa;
import com.filipe.api.domain.venda.FormaPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LancamentoCaixaResponse(
        UUID id,
        UUID caixaId,
        TipoLancamentoCaixa tipo,
        FormaPagamento formaPagamento,
        BigDecimal valor,
        String descricao,
        UUID referenciaId,
        LocalDateTime dataHora,
        UUID usuarioId,
        String usuarioNome
) {
}
