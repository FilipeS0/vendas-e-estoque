package com.filipe.api.dto.estoque;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MovimentacaoEstoqueResponse(
        UUID id,
        UUID produtoId,
        String produtoNome,
        String tipo,
        BigDecimal quantidade,
        BigDecimal quantidadeAnterior,
        BigDecimal quantidadeResultante,
        String motivo,
        String referencia,
        UUID usuarioId,
        String usuarioNome,
        LocalDateTime dataHora
) {
}
