package com.filipe.api.dto.relatorio;

import java.math.BigDecimal;

public record ContasAReceberResumoResponse(
    BigDecimal totalVencido,
    BigDecimal totalAVencer,
    BigDecimal totalGeral
) {}
