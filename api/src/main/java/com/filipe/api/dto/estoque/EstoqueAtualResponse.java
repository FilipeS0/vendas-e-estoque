package com.filipe.api.dto.estoque;

import java.math.BigDecimal;
import java.util.UUID;

public record EstoqueAtualResponse(
    UUID produtoId,
    String produtoNome,
    String codigoBarras,
    String categoriaNome,
    BigDecimal quantidadeAtual,
    BigDecimal quantidadeMinima
) {}
