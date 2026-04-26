package com.filipe.api.dto.venda;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AplicarDescontoVendaRequest(
        @NotNull(message = "O desconto da venda é obrigatório")
        @DecimalMin(value = "0.00", inclusive = true, message = "O desconto da venda não pode ser negativo")
        BigDecimal desconto
) {}
