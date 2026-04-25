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
           WHERE (:produtoId  IS NULL OR m.produto.id = :produtoId)
           AND   (:tipo       IS NULL OR m.tipo = :tipo)
           AND   (:dataInicio IS NULL OR m.dataHora >= :dataInicio)
           AND   (:dataFim    IS NULL OR m.dataHora <= :dataFim)
           ORDER BY m.dataHora DESC
           """)
    Page<MovimentacaoEstoque> findComFiltros(
            @Param("produtoId")  UUID                   produtoId,
            @Param("tipo")       TipoMovimentacaoEstoque tipo,
            @Param("dataInicio") LocalDateTime           dataInicio,
            @Param("dataFim")    LocalDateTime           dataFim,
            Pageable pageable);
}