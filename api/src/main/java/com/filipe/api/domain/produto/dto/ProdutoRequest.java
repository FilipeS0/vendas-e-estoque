package com.filipe.api.domain.produto.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoRequest(
        @NotBlank String codigoInterno,
        @NotBlank @Size(min = 8, max = 13) String codigoBarras,
        @NotBlank @Size(max = 200) String nome,
        String descricao,
        @NotNull UUID categoriaId,
        @NotNull UUID fornecedorId,
        @NotNull @PositiveOrZero BigDecimal precoCusto,
        @NotNull @Positive BigDecimal precoVenda,
        @NotBlank @Size(min = 8, max = 8) String ncm,
        @Size(max = 10) String cest,
        @NotBlank @Size(min = 4, max = 4) String cfop,
        String situacaoTributaria,
        @PositiveOrZero BigDecimal aliquotaIcms,
        @PositiveOrZero BigDecimal aliquotaPis,
        @PositiveOrZero BigDecimal aliquotaCofins
) {}
