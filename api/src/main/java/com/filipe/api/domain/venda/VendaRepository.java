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
           "WHERE v.status = COALESCE(:status, v.status) " +
           "AND v.dataHora >= COALESCE(:dataInicio, v.dataHora) " +
           "AND v.dataHora <= COALESCE(:dataFim, v.dataHora) " +
           "AND v.numero = COALESCE(:numero, v.numero) " +
           "AND v.caixa.id = COALESCE(:caixaId, v.caixa.id)")
    Page<Venda> findComFiltros(
            @Param("status") StatusVenda status,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("numero") Long numero,
            @Param("caixaId") UUID caixaId,
            Pageable pageable);
}
