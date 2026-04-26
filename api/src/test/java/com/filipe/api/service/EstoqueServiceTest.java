package com.filipe.api.service;

import com.filipe.api.domain.estoque.EstoqueAtual;
import com.filipe.api.domain.estoque.EstoqueAtualRepository;
import com.filipe.api.domain.estoque.MovimentacaoEstoque;
import com.filipe.api.domain.estoque.MovimentacaoEstoqueRepository;
import com.filipe.api.domain.estoque.TipoMovimentacaoEstoque;
import com.filipe.api.domain.produto.Produto;
import com.filipe.api.domain.produto.ProdutoRepository;
import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.exception.BusinessException;
import com.filipe.api.mapper.estoque.EstoqueMapper;
import com.filipe.api.shared.audit.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EstoqueServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private EstoqueAtualRepository estoqueAtualRepository;

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Mock
    private EstoqueMapper estoqueMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private EstoqueService estoqueService;

    private Produto produto;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        produto = Produto.builder()
                .id(UUID.randomUUID())
                .nome("Produto Teste")
                .build();

        usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Usuario Teste")
                .build();

        when(movimentacaoEstoqueRepository.save(any(MovimentacaoEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldSaveMovementWhenSaleExitAndSufficientStock() {
        EstoqueAtual estoqueAtual = EstoqueAtual.builder()
                .id(UUID.randomUUID())
                .produto(produto)
                .quantidadeAtual(new BigDecimal("20.000"))
                .quantidadeMinima(new BigDecimal("0.000"))
                .build();

        when(estoqueAtualRepository.findByProdutoIdForUpdate(produto.getId()))
                .thenReturn(Optional.of(estoqueAtual));

        MovimentacaoEstoque movimento = MovimentacaoEstoque.builder()
                .id(UUID.randomUUID())
                .produto(produto)
                .tipo(TipoMovimentacaoEstoque.SAIDA_VENDA)
                .quantidade(new BigDecimal("5.000"))
                .build();
        when(estoqueMapper.toEntity(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(movimento);

        estoqueService.moverEstoque(produto, TipoMovimentacaoEstoque.SAIDA_VENDA,
                new BigDecimal("5.000"), "Venda confirmada", "ref-123", usuario);

        assertEquals(new BigDecimal("15.000"), estoqueAtual.getQuantidadeAtual());
        verify(estoqueAtualRepository).save(estoqueAtual);
        verify(movimentacaoEstoqueRepository).save(movimento);
    }

    @Test
    void shouldThrowWhenSaleExitAndInsufficientStock() {
        EstoqueAtual estoqueAtual = EstoqueAtual.builder()
                .id(UUID.randomUUID())
                .produto(produto)
                .quantidadeAtual(new BigDecimal("2.000"))
                .quantidadeMinima(new BigDecimal("0.000"))
                .build();

        when(estoqueAtualRepository.findByProdutoIdForUpdate(produto.getId()))
                .thenReturn(Optional.of(estoqueAtual));

        assertThrows(BusinessException.class,
                () -> estoqueService.moverEstoque(produto, TipoMovimentacaoEstoque.SAIDA_VENDA,
                        new BigDecimal("5.000"), "Venda confirmada", "ref-123", usuario));
    }

    @Test
    void shouldAddStockWhenEntradaCompra() {
        EstoqueAtual estoqueAtual = EstoqueAtual.builder()
                .id(UUID.randomUUID())
                .produto(produto)
                .quantidadeAtual(new BigDecimal("10.000"))
                .quantidadeMinima(new BigDecimal("1.000"))
                .build();

        when(estoqueAtualRepository.findByProdutoIdForUpdate(produto.getId()))
                .thenReturn(Optional.of(estoqueAtual));

        MovimentacaoEstoque movimento = MovimentacaoEstoque.builder()
                .id(UUID.randomUUID())
                .produto(produto)
                .tipo(TipoMovimentacaoEstoque.ENTRADA_COMPRA)
                .quantidade(new BigDecimal("6.000"))
                .build();
        when(estoqueMapper.toEntity(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(movimento);

        estoqueService.moverEstoque(produto, TipoMovimentacaoEstoque.ENTRADA_COMPRA,
                new BigDecimal("6.000"), "Compra recebida", "ref-456", usuario);

        assertEquals(new BigDecimal("16.000"), estoqueAtual.getQuantidadeAtual());
        verify(estoqueAtualRepository).save(estoqueAtual);
        verify(movimentacaoEstoqueRepository).save(movimento);
    }

    @Test
    void shouldThrowWhenUpdatingMinimumWithNegativeValue() {
        EstoqueAtual estoqueAtual = EstoqueAtual.builder()
                .id(UUID.randomUUID())
                .produto(produto)
                .quantidadeAtual(new BigDecimal("10.000"))
                .quantidadeMinima(new BigDecimal("1.000"))
                .build();

        when(estoqueAtualRepository.findByProdutoId(produto.getId()))
                .thenReturn(Optional.of(estoqueAtual));

        assertThrows(BusinessException.class,
                () -> estoqueService.atualizarEstoqueMinimo(produto.getId(), new BigDecimal("-1.000")));
    }
}