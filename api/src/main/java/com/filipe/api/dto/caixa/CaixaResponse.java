package com.filipe.api.dto.caixa;

import com.filipe.api.domain.caixa.StatusCaixa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CaixaResponse(
        UUID id,
        UUID operadorId,
        String operadorNome,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        BigDecimal valorAbertura,
        BigDecimal valorFechamentoSistema,
        BigDecimal valorFechamentoFisico,
        BigDecimal diferenca,
        StatusCaixa status
) {
}
