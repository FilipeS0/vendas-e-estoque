package com.filipe.api.domain.produto.dto;

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
        Boolean ativo
) {}
