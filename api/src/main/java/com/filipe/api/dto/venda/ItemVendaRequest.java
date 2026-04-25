package com.filipe.api.dto.venda;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemVendaRequest(
    @NotNull(message = "O ID do produto é obrigatório")
    UUID produtoId,

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    BigDecimal quantidade
) {}
