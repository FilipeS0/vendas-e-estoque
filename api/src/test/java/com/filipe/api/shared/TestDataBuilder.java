package com.filipe.api.shared;

import com.filipe.api.domain.produto.Categoria;
import com.filipe.api.domain.produto.Fornecedor;
import com.filipe.api.domain.produto.Produto;

import java.math.BigDecimal;
import java.util.UUID;

public class TestDataBuilder {

    public static Categoria createCategoria() {
        return Categoria.builder()
                .id(UUID.randomUUID())
                .nome("Categoria Teste")
                .ativo(true)
                .build();
    }

    public static Fornecedor createFornecedor() {
        return Fornecedor.builder()
                .id(UUID.randomUUID())
                .nome("Fornecedor Teste")
                .cnpj("12345678000100")
                .ativo(true)
                .build();
    }

    public static Produto createProduto(Categoria categoria, Fornecedor fornecedor) {
        return Produto.builder()
                .id(UUID.randomUUID())
                .nome("Produto Teste")
                .codigoInterno("PROD001")
                .codigoBarras("7891234567890")
                .precoCusto(new BigDecimal("10.00"))
                .precoVenda(new BigDecimal("20.00"))
                .categoria(categoria)
                .fornecedor(fornecedor)
                .ativo(true)
                .build();
    }
}
