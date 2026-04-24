package com.filipe.api.service;

import com.filipe.api.domain.produto.Categoria;
import com.filipe.api.domain.produto.CategoriaRepository;
import com.filipe.api.domain.produto.Fornecedor;
import com.filipe.api.domain.produto.FornecedorRepository;
import com.filipe.api.domain.produto.Produto;
import com.filipe.api.domain.produto.ProdutoRepository;
import com.filipe.api.domain.produto.dto.ProdutoRequest;
import com.filipe.api.domain.produto.dto.ProdutoResponse;
import com.filipe.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FornecedorRepository fornecedorRepository;

    @Transactional
    public ProdutoResponse registrarProduto(ProdutoRequest request) {
        if (produtoRepository.existsByCodigoBarras(request.codigoBarras())) {
            throw new BusinessException("Já existe um produto com o código de barras informado.");
        }

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new BusinessException("Categoria não encontrada."));

        Fornecedor fornecedor = fornecedorRepository.findById(request.fornecedorId())
                .orElseThrow(() -> new BusinessException("Fornecedor não encontrado."));

        Produto produto = Produto.builder()
                .codigoInterno(request.codigoInterno())
                .codigoBarras(request.codigoBarras())
                .nome(request.nome())
                .descricao(request.descricao())
                .categoria(categoria)
                .fornecedor(fornecedor)
                .precoCusto(request.precoCusto())
                .precoVenda(request.precoVenda())
                .ncm(request.ncm())
                .cest(request.cest())
                .cfop(request.cfop())
                .situacaoTributaria(request.situacaoTributaria())
                .aliquotaIcms(request.aliquotaIcms())
                .aliquotaPis(request.aliquotaPis())
                .aliquotaCofins(request.aliquotaCofins())
                .ativo(true)
                .build();

        Produto savedProduto = produtoRepository.save(produto);

        return new ProdutoResponse(
                savedProduto.getId(),
                savedProduto.getCodigoInterno(),
                savedProduto.getCodigoBarras(),
                savedProduto.getNome(),
                categoria.getNome(),
                fornecedor.getNome(),
                savedProduto.getPrecoVenda(),
                savedProduto.getAtivo()
        );
    }
}
