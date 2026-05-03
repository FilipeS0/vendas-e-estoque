package com.filipe.api.dto.crediario;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record LiquidarParcelaRequest(
    @NotNull BigDecimal valorPago,
    @NotNull UUID caixaId
) {}
