package com.filipe.api.service;

import com.filipe.api.domain.produto.Categoria;
import com.filipe.api.domain.produto.CategoriaRepository;
import com.filipe.api.domain.produto.Fornecedor;
import com.filipe.api.domain.produto.FornecedorRepository;
import com.filipe.api.domain.produto.Produto;
import com.filipe.api.domain.produto.ProdutoRepository;
import com.filipe.api.domain.produto.dto.ProdutoDetalheResponse;
import com.filipe.api.domain.produto.dto.ProdutoRequest;
import com.filipe.api.domain.produto.dto.ProdutoUpdateRequest;
import com.filipe.api.domain.produto.dto.ProdutoResponse;
import com.filipe.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FornecedorRepository fornecedorRepository;

    public ProdutoDetalheResponse buscarPorId(UUID id) {
        Produto p = produtoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Produto não encontrado."));
        return new ProdutoDetalheResponse(
                p.getId(), p.getCodigoInterno(), p.getCodigoBarras(), p.getNome(),
                p.getDescricao(), p.getCategoria().getId(), p.getFornecedor().getId(),
                p.getPrecoCusto(), p.getPrecoVenda(), p.getNcm(), p.getCest(),
                p.getCfop(), p.getSituacaoTributaria(), p.getAliquotaIcms(),
                p.getAliquotaPis(), p.getAliquotaCofins(), p.getAtivo()
        );
    }

    public Page<ProdutoResponse> listarProdutos(String nome, Pageable pageable) {
        Page<Produto> produtosPage;
        
        if (nome != null && !nome.trim().isEmpty()) {
            produtosPage = produtoRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome.trim(), pageable);
        } else {
            produtosPage = produtoRepository.findByAtivoTrue(pageable);
        }

        return produtosPage.map(p -> new ProdutoResponse(
                p.getId(),
                p.getCodigoInterno(),
                p.getCodigoBarras(),
                p.getNome(),
                p.getCategoria() != null ? p.getCategoria().getNome() : null,
                p.getFornecedor() != null ? p.getFornecedor().getNome() : null,
                p.getPrecoVenda(),
                p.getAtivo()
        ));
    }

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

    @Transactional
    public ProdutoResponse atualizarProduto(UUID id, ProdutoUpdateRequest request) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Produto não encontrado."));

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new BusinessException("Categoria não encontrada."));

        Fornecedor fornecedor = fornecedorRepository.findById(request.fornecedorId())
                .orElseThrow(() -> new BusinessException("Fornecedor não encontrado."));

        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setCategoria(categoria);
        produto.setFornecedor(fornecedor);
        produto.setPrecoCusto(request.precoCusto());
        produto.setPrecoVenda(request.precoVenda());
        produto.setNcm(request.ncm());
        produto.setCest(request.cest());
        produto.setCfop(request.cfop());
        produto.setSituacaoTributaria(request.situacaoTributaria());
        produto.setAliquotaIcms(request.aliquotaIcms());
        produto.setAliquotaPis(request.aliquotaPis());
        produto.setAliquotaCofins(request.aliquotaCofins());

        Produto savedProduto = produtoRepository.save(produto);

        return new ProdutoResponse(
                savedProduto.getId(), savedProduto.getCodigoInterno(), savedProduto.getCodigoBarras(),
                savedProduto.getNome(), categoria.getNome(), fornecedor.getNome(),
                savedProduto.getPrecoVenda(), savedProduto.getAtivo()
        );
    }

    @Transactional
    public void inativarProduto(UUID id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Produto não encontrado."));
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }
}
