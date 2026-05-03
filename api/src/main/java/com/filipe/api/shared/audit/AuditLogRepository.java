package com.filipe.api.shared.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("SELECT a FROM AuditLog a " +
           "WHERE (:entidade IS NULL OR a.entidade = :entidade) " +
           "AND (:entidadeId IS NULL OR a.entidadeId = :entidadeId)")
    Page<AuditLog> findByFilters(
            @Param("entidade") String entidade,
            @Param("entidadeId") UUID entidadeId,
            Pageable pageable
    );
}
