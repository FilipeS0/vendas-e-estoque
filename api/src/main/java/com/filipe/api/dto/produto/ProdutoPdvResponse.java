package com.filipe.api.dto.produto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoPdvResponse(
    UUID id,
    String nome,
    String codigoBarras,
    BigDecimal precoVenda,
    BigDecimal quantidadeEstoque
) {}
