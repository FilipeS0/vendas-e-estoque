package com.filipe.api.dto.venda;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemVendaResponse(
    UUID id,
    UUID produtoId,
    String produtoNome,
    BigDecimal quantidade,
    BigDecimal precoUnitario,
    BigDecimal desconto,
    BigDecimal valorTotal
) {}
