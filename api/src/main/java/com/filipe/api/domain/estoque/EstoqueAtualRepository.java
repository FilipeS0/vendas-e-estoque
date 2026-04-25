package com.filipe.api.domain.estoque;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstoqueAtualRepository extends JpaRepository<EstoqueAtual, UUID> {

    Optional<EstoqueAtual> findByProdutoId(UUID produtoId);

    /**
     * Fix 2 — pessimistic write lock prevents two concurrent transactions from
     * reading the same quantity, both passing the "sufficient stock" check and
     * both subtracting, which would silently push quantity below zero.
     *
     * Use this overload wherever stock is about to be mutated (sale
     * confirmation, manual entry, cancellation reversal).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EstoqueAtual e WHERE e.produto.id = :produtoId")
    Optional<EstoqueAtual> findByProdutoIdForUpdate(@Param("produtoId") UUID produtoId);

    List<EstoqueAtual> findByProdutoIdIn(Collection<UUID> produtoIds);

    @Query("SELECT e FROM EstoqueAtual e WHERE e.quantidadeAtual < e.quantidadeMinima")
    List<EstoqueAtual> findAbaixoMinimo();

    @Query("""
           SELECT e FROM EstoqueAtual e JOIN e.produto p LEFT JOIN p.categoria c
           WHERE (CASE WHEN :nome IS NULL THEN TRUE ELSE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')) END)
           AND   p.codigoBarras = COALESCE(:codigoBarras, p.codigoBarras)
           AND   (c.id IS NULL OR c.id = COALESCE(:categoriaId, c.id))
           """)
    org.springframework.data.domain.Page<EstoqueAtual> findComFiltros(
            @Param("nome")          String nome,
            @Param("codigoBarras")  String codigoBarras,
            @Param("categoriaId")   UUID   categoriaId,
            org.springframework.data.domain.Pageable pageable);
}