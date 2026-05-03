package com.filipe.api.dto.relatorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FluxoCaixaResponse(
    List<FluxoDiario> dias,
    BigDecimal totalEntradas,
    BigDecimal totalSaidas,
    BigDecimal saldoFinal
) {
    public record FluxoDiario(
        LocalDate data,
        BigDecimal entradas,
        BigDecimal saidas,
        BigDecimal saldo
    ) {}
}
