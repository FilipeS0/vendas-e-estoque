package com.filipe.api.dto.cliente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExtratoClienteItem(
    LocalDateTime data,
    String tipo, // "VENDA", "PAGAMENTO", "ESTORNO"
    String descricao,
    BigDecimal valor,
    BigDecimal saldoDevedorMomento,
    UUID referenciaId
) {}
