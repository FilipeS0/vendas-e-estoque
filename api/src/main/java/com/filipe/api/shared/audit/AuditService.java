package com.filipe.api.shared.audit;

import com.filipe.api.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository auditLogRepository;

    /**
     * Persists an audit log in the database and also logs it via SLF4J.
     * Uses Propagation.REQUIRES_NEW to ensure the audit log is saved even if the 
     * main transaction rolls back (crucial for auditing failed attempts or rollbacks).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String acao, String entidade, UUID entidadeId, Usuario usuario, String detalhe) {
        String usuarioId = usuario != null && usuario.getId() != null
                ? usuario.getId().toString()
                : "unknown";

        log.info("[AUDIT] acao={} entidade={} id={} usuario={} detalhe={}",
                acao, entidade, entidadeId, usuarioId, detalhe);

        try {
            AuditLog auditLog = AuditLog.builder()
                    .acao(acao)
                    .entidade(entidade)
                    .entidadeId(entidadeId)
                    .usuario(usuario)
                    .detalhe(detalhe)
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to persist audit log to database", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> listLogs(String entidade, UUID entidadeId, Pageable pageable) {
        return auditLogRepository.findByFilters(entidade, entidadeId, pageable);
    }
}
