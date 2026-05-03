package com.filipe.api.domain.caixa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LancamentoCaixaRepository extends JpaRepository<LancamentoCaixa, UUID> {
    List<LancamentoCaixa> findByCaixaId(UUID caixaId);
    List<LancamentoCaixa> findByCaixaIdAndReferenciaId(UUID caixaId, UUID referenciaId);

    @Query("SELECT l FROM LancamentoCaixa l WHERE l.caixa.id = :caixaId " +
           "AND (:tipo IS NULL OR l.tipo = :tipo) " +
           "AND (:dataInicio IS NULL OR l.dataHora >= :dataInicio) " +
           "AND (:dataFim IS NULL OR l.dataHora <= :dataFim) " +
           "ORDER BY l.dataHora DESC")
    Page<LancamentoCaixa> findComFiltros(
            @Param("caixaId") UUID caixaId,
            @Param("tipo") TipoLancamentoCaixa tipo,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );

    List<LancamentoCaixa> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
}
