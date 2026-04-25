package com.filipe.api.dto.fiscal;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotaFiscalResponse(
        UUID id,
        UUID vendaId,
        Long numero,
        Integer serie,
        String chaveAcesso,
        LocalDateTime dataEmissao,
        String xmlAutorizado,
        String urlDanfe,
        String status,
        String mensagemRetorno,
        String protocolo,
        String ambiente
) {
}
