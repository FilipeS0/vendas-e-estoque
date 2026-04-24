package com.filipe.api.domain.produto.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoResponse(
        UUID id,
        String codigoInterno,
        String codigoBarras,
        String nome,
        String categoriaNome,
        String fornecedorNome,
        BigDecimal precoVenda,
        Boolean ativo
) {}
