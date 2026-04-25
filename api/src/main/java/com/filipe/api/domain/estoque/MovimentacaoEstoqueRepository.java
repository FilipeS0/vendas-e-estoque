package com.filipe.api.domain.estoque;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, UUID> {

    /**
     * Fix 3 — the :tipo param is now TipoMovimentacaoEstoque (enum), matching
     * the entity field type.  Previously it was String, which broke any filter
     * that passed a typed enum value (JPQL compares via toString and the DB
     * stores the enum name, but the param type mismatch caused silent misses
     * when Hibernate bound it incorrectly).
     */
    @Query("""
           SELECT m FROM MovimentacaoEstoque m
           WHERE m.produto.id = COALESCE(:produtoId, m.produto.id)
           AND   m.tipo       = COALESCE(:tipo,      m.tipo)
           AND   m.dataHora  >= COALESCE(:dataInicio, m.dataHora)
           AND   m.dataHora  <= COALESCE(:dataFim,    m.dataHora)
           ORDER BY m.dataHora DESC
           """)
    Page<MovimentacaoEstoque> findComFiltros(
            @Param("produtoId")  UUID                   produtoId,
            @Param("tipo")       TipoMovimentacaoEstoque tipo,
            @Param("dataInicio") LocalDateTime           dataInicio,
            @Param("dataFim")    LocalDateTime           dataFim,
            Pageable pageable);
}