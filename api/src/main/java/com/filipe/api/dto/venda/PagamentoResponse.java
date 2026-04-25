package com.filipe.api.dto.venda;

import com.filipe.api.domain.venda.FormaPagamento;

import java.math.BigDecimal;
import java.util.UUID;

public record PagamentoResponse(
    UUID id,
    FormaPagamento formaPagamento,
    BigDecimal valor,
    BigDecimal troco
) {}
