package com.filipe.api.service;

import com.filipe.api.domain.estoque.EstoqueAtual;
import com.filipe.api.domain.estoque.EstoqueAtualRepository;
import com.filipe.api.domain.estoque.MovimentacaoEstoque;
import com.filipe.api.domain.estoque.MovimentacaoEstoqueRepository;
import com.filipe.api.domain.estoque.TipoMovimentacaoEstoque;
import com.filipe.api.domain.produto.Produto;
import com.filipe.api.domain.produto.ProdutoRepository;
import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.dto.estoque.AjusteInventarioRequest;
import com.filipe.api.dto.estoque.MovimentacaoEstoqueRequest;
import com.filipe.api.dto.estoque.MovimentacaoEstoqueResponse;
import com.filipe.api.dto.estoque.SaidaManualEstoqueRequest;
import com.filipe.api.dto.estoque.EstoqueAtualResponse;
import com.filipe.api.exception.BusinessException;
import com.filipe.api.mapper.estoque.EstoqueMapper;
import com.filipe.api.shared.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final ProdutoRepository produtoRepository;
    private final EstoqueAtualRepository estoqueAtualRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final EstoqueMapper estoqueMapper;
    private final AuditService auditService;

    // ── public API ────────────────────────────────────────────────────────────

    @Transactional
    public MovimentacaoEstoqueResponse registrarSaidaManual(SaidaManualEstoqueRequest request, Usuario usuario) {
        return registrarMovimentacaoInterna(
                request.produtoId(),
                TipoMovimentacaoEstoque.SAIDA_PERDA,
                request.quantidade(),
                request.motivo(),
                request.referencia(),
                usuario
        );
    }

    /**
     * Fix 3 — the request DTO carries a String "ENTRADA" / "SAIDA" from the
     * HTTP layer; we map it to the canonical enum here so the entity and DB
     * always store the full, specific type name.
     */
    @Transactional
    public MovimentacaoEstoqueResponse registrarMovimentacao(MovimentacaoEstoqueRequest request, Usuario usuario) {
        TipoMovimentacaoEstoque tipo = resolverTipoGenerico(request.tipo());
        return registrarMovimentacaoInterna(
                request.produtoId(),
                tipo,
                request.quantidade(),
                request.motivo(),
                request.referencia(),
                usuario
        );
    }

    @Transactional(readOnly = true)
    public Page<EstoqueAtualResponse> listarEstoqueAtual(
            String nome, String codigoBarras, UUID categoriaId, Pageable pageable) {
        return estoqueAtualRepository
                .findComFiltros(nome, codigoBarras, categoriaId, pageable)
                .map(estoqueMapper::toEstoqueAtualResponse);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoEstoqueResponse> listarMovimentacoes(
            UUID produtoId, String tipoStr,
            LocalDateTime dataInicio, LocalDateTime dataFim,
            Pageable pageable) {

        TipoMovimentacaoEstoque tipo = tipoStr != null
                ? parseTipoOrThrow(tipoStr)
                : null;

        return movimentacaoEstoqueRepository
                .findComFiltros(produtoId, tipo, dataInicio, dataFim, pageable)
                .map(m -> estoqueMapper.toResponse(m, m.getUsuario()));
    }

    @Transactional(readOnly = true)
    public List<EstoqueAtualResponse> listarAbaixoMinimo() {
        return estoqueAtualRepository.findAbaixoMinimo().stream()
                .map(estoqueMapper::toEstoqueAtualResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void atualizarEstoqueMinimo(UUID produtoId, BigDecimal quantidadeMinima) {
        if (quantidadeMinima.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("A quantidade minima nao pode ser negativa.");
        }
        EstoqueAtual estoqueAtual = estoqueAtualRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new BusinessException("Estoque do produto nao encontrado."));
        estoqueAtual.setQuantidadeMinima(quantidadeMinima);
        estoqueAtualRepository.save(estoqueAtual);
    }

    @Transactional
    public MovimentacaoEstoqueResponse ajustarInventario(AjusteInventarioRequest request, Usuario usuario) {
        EstoqueAtual estoque = estoqueAtualRepository.findByProdutoId(request.produtoId())
                .orElseThrow(() -> new BusinessException("Estoque nao encontrado."));
        
        BigDecimal atual = estoque.getQuantidadeAtual();
        BigDecimal diff = request.novaQuantidade().subtract(atual);
        
        if (diff.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("A nova quantidade e igual a atual. Nenhum ajuste necessario.");
        }
        
        TipoMovimentacaoEstoque tipo = diff.compareTo(BigDecimal.ZERO) > 0 
                ? TipoMovimentacaoEstoque.AJUSTE_POSITIVO 
                : TipoMovimentacaoEstoque.AJUSTE_NEGATIVO;
        
        return registrarMovimentacaoInterna(
                request.produtoId(),
                tipo,
                diff.abs(),
                request.motivo() != null ? request.motivo() : "Ajuste de inventario",
                "INVENTARIO",
                usuario
        );
    }

    // ── package-private: called directly by VendaService ─────────────────────

    /**
     * Fix 2 — uses findByProdutoIdForUpdate (PESSIMISTIC_WRITE) so concurrent
     * sale confirmations for the same product are serialised at the DB level.
     * Fix 3 — accepts TipoMovimentacaoEstoque directly, no String conversion.
     */
    @Transactional
    public void moverEstoque(
            Produto produto,
            TipoMovimentacaoEstoque tipo,
            BigDecimal quantidade,
            String motivo,
            String referencia,
            Usuario usuario) {

        EstoqueAtual estoque = estoqueAtualRepository
                .findByProdutoIdForUpdate(produto.getId())
                .orElseThrow(() -> new BusinessException(
                        "Estoque nao encontrado para o produto: " + produto.getNome()));

        BigDecimal anterior = estoque.getQuantidadeAtual();
        BigDecimal resultante = tipo.isEntrada()
                ? anterior.add(quantidade)
                : anterior.subtract(quantidade);

        if (resultante.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(
                    "Estoque insuficiente para o produto: " + produto.getNome()
                    + ". Disponível: " + anterior + ", solicitado: " + quantidade);
        }

        estoque.setQuantidadeAtual(resultante);
        estoqueAtualRepository.save(estoque);

        MovimentacaoEstoque mov = estoqueMapper.toEntity(
                produto, tipo, quantidade, anterior, resultante, motivo, referencia, usuario);
        movimentacaoEstoqueRepository.save(mov);

        if (tipo == TipoMovimentacaoEstoque.AJUSTE_POSITIVO
                || tipo == TipoMovimentacaoEstoque.AJUSTE_NEGATIVO
                || tipo == TipoMovimentacaoEstoque.SAIDA_PERDA) {
            auditService.log(
                    "AJUSTE_ESTOQUE",
                    "Estoque",
                    produto.getId(),
                    usuario,
                    "tipo=" + tipo + " quantidade=" + quantidade + " motivo=" + motivo
            );
        }
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private MovimentacaoEstoqueResponse registrarMovimentacaoInterna(
            UUID produtoId,
            TipoMovimentacaoEstoque tipo,
            BigDecimal quantidade,
            String motivo,
            String referencia,
            Usuario usuario) {

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new BusinessException("Produto nao encontrado."));

        moverEstoque(produto, tipo, quantidade, motivo, referencia, usuario);

        // Re-fetch the movement we just saved so we can return it properly
        return movimentacaoEstoqueRepository
                .findComFiltros(produtoId, tipo, null, null, Pageable.ofSize(1))
                .getContent()
                .stream()
                .findFirst()
                .map(m -> estoqueMapper.toResponse(m, usuario))
                .orElseThrow();
    }

    /** Maps generic "ENTRADA" / "SAIDA" strings from the HTTP API to a default enum value. */
    private TipoMovimentacaoEstoque resolverTipoGenerico(String tipo) {
        return switch (tipo.trim().toUpperCase()) {
            case "ENTRADA" -> TipoMovimentacaoEstoque.ENTRADA_COMPRA;
            case "SAIDA"   -> TipoMovimentacaoEstoque.SAIDA_PERDA;
            default        -> parseTipoOrThrow(tipo);
        };
    }

    private TipoMovimentacaoEstoque parseTipoOrThrow(String tipo) {
        try {
            return TipoMovimentacaoEstoque.valueOf(tipo.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    "Tipo de movimentacao invalido: '" + tipo + "'. Valores aceitos: "
                    + java.util.Arrays.toString(TipoMovimentacaoEstoque.values()));
        }
    }
}