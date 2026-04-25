package com.filipe.api.dto.crediario;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LiquidarParcelaRequest(
    @NotNull BigDecimal valorPago,
    @NotNull UUID caixaId
) {}
