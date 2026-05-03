package com.filipe.api.dto.estoque;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record AjusteInventarioRequest(
    @NotNull UUID produtoId,
    @NotNull BigDecimal novaQuantidade,
    String motivo
) {}
