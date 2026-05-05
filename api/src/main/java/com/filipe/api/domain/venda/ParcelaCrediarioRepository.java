package com.filipe.api.domain.venda;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ParcelaCrediarioRepository extends JpaRepository<ParcelaCrediario, UUID> {
    
    @Query("""
           SELECT p FROM ParcelaCrediario p
           WHERE (:clienteId IS NULL OR p.crediario.cliente.id = :clienteId)
           AND   (:status    IS NULL OR p.status = :status)
           ORDER BY p.dataVencimento ASC
           """)
    Page<ParcelaCrediario> findComFiltros(
            @Param("clienteId") UUID clienteId,
            @Param("status")    StatusParcela status,
            Pageable pageable);

    @Modifying
    @Query("UPDATE ParcelaCrediario p SET p.status = :atrasado " +
           "WHERE p.status = :pendente AND p.dataVencimento < CURRENT_DATE")
    int marcarParcelasVencidas(@Param("atrasado") StatusParcela atrasado,
                           @Param("pendente") StatusParcela pendente);

    @Query("""
           SELECT 
             SUM(CASE WHEN p.status = :vencida THEN p.valor - p.valorPago ELSE 0 END),
             SUM(CASE WHEN p.status = :pendente THEN p.valor - p.valorPago ELSE 0 END),
             SUM(p.valor - p.valorPago)
           FROM ParcelaCrediario p
           WHERE p.status IN :statuses
           """)
    Object[] getResumoContasAReceber(@Param("vencida") StatusParcela vencida,
                                     @Param("pendente") StatusParcela pendente,
                                     @Param("statuses") java.util.List<StatusParcela> statuses);
}
