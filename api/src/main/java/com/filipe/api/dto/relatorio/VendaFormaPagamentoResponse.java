package com.filipe.api.dto.relatorio;

import java.math.BigDecimal;

public record VendaFormaPagamentoResponse(
    String formaPagamento,
    Long quantidade,
    BigDecimal total
) {}
