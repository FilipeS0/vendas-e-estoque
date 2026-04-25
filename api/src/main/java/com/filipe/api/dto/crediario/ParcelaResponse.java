package com.filipe.api.dto.crediario;

import com.filipe.api.domain.venda.StatusParcela;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ParcelaResponse(
    UUID id,
    Integer numeroParcela,
    BigDecimal valor,
    LocalDate dataVencimento,
    LocalDate dataPagamento,
    BigDecimal valorPago,
    StatusParcela status
) {}
