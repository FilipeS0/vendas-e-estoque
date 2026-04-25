package com.filipe.api.dto.venda;

import com.filipe.api.domain.venda.FormaPagamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PagamentoRequest(
    @NotNull(message = "A forma de pagamento é obrigatória")
    FormaPagamento formaPagamento,

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser maior que zero")
    BigDecimal valor,

    // Optional card fields
    String nsu,
    String autorizacao
) {}
