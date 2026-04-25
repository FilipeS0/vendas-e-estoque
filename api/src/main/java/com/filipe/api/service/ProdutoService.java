package com.filipe.api.service;

import com.filipe.api.domain.estoque.EstoqueAtual;
import com.filipe.api.domain.estoque.EstoqueAtualRepository;
import com.filipe.api.domain.estoque.MovimentacaoEstoque;
import com.filipe.api.domain.estoque.MovimentacaoEstoqueRepository;
import com.filipe.api.domain.estoque.TipoMovimentacaoEstoque;
import com.filipe.api.domain.produto.Categoria;
import com.filipe.api.domain.produto.CategoriaRepository;
import com.filipe.api.domain.produto.Fornecedor;
import com.filipe.api.domain.produto.FornecedorRepository;
import com.filipe.api.domain.produto.Produto;
import com.filipe.api.domain.produto.ProdutoRepository;
import com.filipe.api.dto.produto.ProdutoDetalheResponse;
import com.filipe.api.dto.produto.ProdutoRequest;
import com.filipe.api.dto.produto.ProdutoResponse;
import com.filipe.api.dto.produto.ProdutoPdvResponse;
import com.filipe.api.dto.produto.ProdutoUpdateRequest;
import com.filipe.api.exception.BusinessException;
import com.filipe.api.mapper.estoque.EstoqueMapper;
import com.filipe.api.mapper.produto.ProdutoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final EstoqueAtualRepository estoqueAtualRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final CategoriaRepository categoriaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoMapper produtoMapper;
    private final EstoqueMapper estoqueMapper;

    public ProdutoDetalheResponse buscarPorId(UUID id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Produto nao encontrado."));
        return produtoMapper.toDetalheResponse(produto, buscarQuantidadeEstoque(produto.getId()));
    }

    public ProdutoDetalheResponse buscarPorCodigoBarras(String codigoBarras) {
        Produto produto = produtoRepository.findByCodigoBarrasAndAtivoTrue(codigoBarras)
                .orElseThrow(() -> new BusinessException("Produto nao encontrado para o codigo de barras informado."));
        return produtoMapper.toDetalheResponse(produto, buscarQuantidadeEstoque(produto.getId()));
    }

    public Page<ProdutoResponse> listarProdutos(String nome, Pageable pageable) {
        Page<Produto> produtosPage;

        if (nome != null && !nome.trim().isEmpty()) {
            produtosPage = produtoRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome.trim(), pageable);
        } else {
            produtosPage = produtoRepository.findByAtivoTrue(pageable);
        }

        Map<UUID, BigDecimal> estoquePorProduto = estoqueAtualRepository.findByProdutoIdIn(
                        produtosPage.getContent().stream().map(Produto::getId).toList())
                .stream()
                .collect(Collectors.toMap(
                        estoque -> estoque.getProduto().getId(),
                        EstoqueAtual::getQuantidadeAtual
                ));

        return produtosPage.map(produto -> produtoMapper.toResponse(
                produto,
                estoquePorProduto.getOrDefault(produto.getId(), BigDecimal.ZERO)
        ));
    }

    public Page<ProdutoPdvResponse> listarPdv(String query, Pageable pageable) {
        Page<Produto> produtosPage;

        if (query != null && !query.trim().isEmpty()) {
            Optional<Produto> porCodigo = produtoRepository.findByCodigoBarrasAndAtivoTrue(query.trim());
            if (porCodigo.isPresent()) {
                produtosPage = new PageImpl<>(List.of(porCodigo.get()), pageable, 1);
            } else {
                produtosPage = produtoRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(query.trim(), pageable);
            }
        } else {
            produtosPage = produtoRepository.findByAtivoTrue(pageable);
        }

        Map<UUID, BigDecimal> estoquePorProduto = estoqueAtualRepository.findByProdutoIdIn(
                        produtosPage.getContent().stream().map(Produto::getId).toList())
                .stream()
                .collect(Collectors.toMap(
                        estoque -> estoque.getProduto().getId(),
                        EstoqueAtual::getQuantidadeAtual
                ));

        return produtosPage.map(produto -> produtoMapper.toPdvResponse(
                produto,
                estoquePorProduto.getOrDefault(produto.getId(), BigDecimal.ZERO)
        ));
    }

    @Transactional
    public ProdutoResponse registrarProduto(ProdutoRequest request) {
        if (produtoRepository.existsByCodigoBarras(request.codigoBarras())) {
            throw new BusinessException("Ja existe um produto com o codigo de barras informado.");
        }

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new BusinessException("Categoria nao encontrada."));

        Fornecedor fornecedor = fornecedorRepository.findById(request.fornecedorId())
                .orElseThrow(() -> new BusinessException("Fornecedor nao encontrado."));

        Produto produto = produtoMapper.toEntity(request, categoria, fornecedor);
        Produto savedProduto = produtoRepository.save(produto);

        BigDecimal quantidadeInicial = request.quantidadeInicial() != null
                ? request.quantidadeInicial()
                : BigDecimal.ZERO;

        EstoqueAtual estoqueAtual = EstoqueAtual.builder()
                .produto(savedProduto)
                .quantidadeAtual(quantidadeInicial)
                .quantidadeMinima(BigDecimal.ZERO)
                .build();
        estoqueAtualRepository.save(estoqueAtual);

        // Fix 14 — record the opening balance as a movement so the full
        // history is auditable from day one.  Without this entry the movement
        // log has no record of how the initial stock arrived.
        if (quantidadeInicial.compareTo(BigDecimal.ZERO) > 0) {
            MovimentacaoEstoque movimentacaoInicial = estoqueMapper.toEntity(
                    savedProduto,
                    TipoMovimentacaoEstoque.ENTRADA_COMPRA,
                    quantidadeInicial,
                    BigDecimal.ZERO,
                    quantidadeInicial,
                    "Saldo inicial de estoque no cadastro do produto",
                    "CADASTRO",
                    null  // no operator context at registration time; extend if you add auth here
            );
            movimentacaoEstoqueRepository.save(movimentacaoInicial);
        }

        return produtoMapper.toResponse(savedProduto, estoqueAtual.getQuantidadeAtual());
    }

    @Transactional
    public ProdutoResponse atualizarProduto(UUID id, ProdutoUpdateRequest request) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Produto nao encontrado."));

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new BusinessException("Categoria nao encontrada."));

        Fornecedor fornecedor = fornecedorRepository.findById(request.fornecedorId())
                .orElseThrow(() -> new BusinessException("Fornecedor nao encontrado."));

        produtoMapper.updateEntity(produto, request, categoria, fornecedor);
        Produto savedProduto = produtoRepository.save(produto);

        return produtoMapper.toResponse(savedProduto, buscarQuantidadeEstoque(savedProduto.getId()));
    }

    @Transactional
    public void inativarProduto(UUID id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Produto nao encontrado."));
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    private BigDecimal buscarQuantidadeEstoque(UUID produtoId) {
        return estoqueAtualRepository.findByProdutoId(produtoId)
                .map(EstoqueAtual::getQuantidadeAtual)
                .orElse(BigDecimal.ZERO);
    }
}