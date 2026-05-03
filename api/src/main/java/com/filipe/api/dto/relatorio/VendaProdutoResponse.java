package com.filipe.api.dto.relatorio;

import java.math.BigDecimal;

public record VendaProdutoResponse(
    String produtoNome,
    BigDecimal quantidade,
    BigDecimal total
) {}
