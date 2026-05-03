package com.filipe.api.shared.fiscal;

import lombok.Builder;
import lombok.Data;
import com.filipe.api.domain.fiscal.StatusNfe;

@Data
@Builder
public class NfceResponse {
    private StatusNfe status;
    private String chaveAcesso;
    private String protocolo;
    private String xmlAutorizado;
    private String urlDanfe;
    private String mensagemRetorno;
}
