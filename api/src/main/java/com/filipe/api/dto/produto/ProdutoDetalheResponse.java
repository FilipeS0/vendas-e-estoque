package com.filipe.api.dto.produto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoDetalheResponse(
        UUID id,
        String codigoInterno,
        String codigoBarras,
        String nome,
        String descricao,
        UUID categoriaId,
        UUID fornecedorId,
        BigDecimal precoCusto,
        BigDecimal precoVenda,
        String ncm,
        String cest,
        String cfop,
        String situacaoTributaria,
        BigDecimal aliquotaIcms,
        BigDecimal aliquotaPis,
        BigDecimal aliquotaCofins,
        BigDecimal quantidadeEstoque,
        Boolean ativo
) {}
