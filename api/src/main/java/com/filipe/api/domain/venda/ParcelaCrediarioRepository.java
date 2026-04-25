package com.filipe.api.domain.venda;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
