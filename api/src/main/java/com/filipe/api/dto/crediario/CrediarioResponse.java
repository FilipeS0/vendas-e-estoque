package com.filipe.api.dto.crediario;

import com.filipe.api.domain.venda.StatusCrediario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CrediarioResponse(
    UUID id,
    UUID clienteId,
    String clienteNome,
    UUID vendaId,
    Long vendaNumero,
    BigDecimal valorTotal,
    BigDecimal valorPago,
    StatusCrediario status,
    LocalDateTime createdAt,
    List<ParcelaResponse> parcelas
) {}
