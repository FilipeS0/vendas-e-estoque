package com.filipe.api.dto.produto;

import com.filipe.api.domain.produto.Csosn;
import com.filipe.api.domain.produto.CstPisCofins;
import com.filipe.api.domain.produto.OrigemProduto;

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
        OrigemProduto origem,
        Csosn csosn,
        CstPisCofins cstPisCofins,
        String situacaoTributaria,
        BigDecimal aliquotaIcms,
        BigDecimal aliquotaPis,
        BigDecimal aliquotaCofins,
        BigDecimal quantidadeEstoque,
        String unidadeMedida,
        String imagemUrl,
        Boolean ativo
) {}
