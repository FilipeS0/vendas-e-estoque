package com.filipe.api.dto.caixa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record LancamentoManualCaixaRequest(
        @NotNull @Positive BigDecimal valor,
        @NotBlank String descricao,
        UUID referenciaId
) {
}
