package com.filipe.api.dto.estoque;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record SaidaManualEstoqueRequest(
        @NotNull UUID produtoId,
        @NotNull @Positive BigDecimal quantidade,
        @NotBlank String motivo,
        String referencia
) {
}
