package com.filipe.api.dto.caixa;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record FecharCaixaRequest(
        @NotNull @PositiveOrZero BigDecimal valorFechamentoFisico
) {
}
