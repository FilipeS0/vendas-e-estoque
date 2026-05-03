package com.filipe.api.dto.produto;

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
        BigDecimal quantidadeEstoque,
        String unidadeMedida,
        String imagemUrl,
        Boolean ativo
) {}
