package com.filipe.api.shared.audit;

import com.filipe.api.domain.usuario.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    public void log(String acao, String entidade, UUID entidadeId, Usuario usuario, String detalhe) {
        String usuarioId = usuario != null && usuario.getId() != null
                ? usuario.getId().toString()
                : "unknown";

        log.info("[AUDIT] acao={} entidade={} id={} usuario={} detalhe={}",
                acao, entidade, entidadeId, usuarioId, detalhe);
    }
}
