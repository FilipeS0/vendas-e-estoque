package com.filipe.api.domain.venda;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface VendaRepository extends JpaRepository<Venda, UUID> {
    List<Venda> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT v FROM Venda v " +
           "WHERE (:status IS NULL OR v.status = :status) " +
           "AND (:dataInicio IS NULL OR v.dataHora >= :dataInicio) " +
           "AND (:dataFim IS NULL OR v.dataHora <= :dataFim) " +
           "AND (:numero IS NULL OR v.numero = :numero) " +
           "AND (:caixaId IS NULL OR v.caixa.id = :caixaId)")
    Page<Venda> findComFiltros(
            @Param("status") StatusVenda status,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("numero") Long numero,
            @Param("caixaId") UUID caixaId,
            Pageable pageable);
}
