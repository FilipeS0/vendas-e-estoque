package com.filipe.api.mapper.fiscal;

import com.filipe.api.domain.fiscal.NotaFiscal;
import com.filipe.api.dto.fiscal.NotaFiscalResponse;
import org.springframework.stereotype.Component;

@Component
public class NotaFiscalMapper {

    public NotaFiscalResponse toResponse(NotaFiscal notaFiscal) {
        return new NotaFiscalResponse(
                notaFiscal.getId(),
                notaFiscal.getVenda().getId(),
                notaFiscal.getNumero(),
                notaFiscal.getSerie(),
                notaFiscal.getChaveAcesso(),
                notaFiscal.getDataEmissao(),
                notaFiscal.getXmlAutorizado(),
                notaFiscal.getUrlDanfe(),
                notaFiscal.getStatus(),
                notaFiscal.getMensagemRetorno(),
                notaFiscal.getProtocolo(),
                notaFiscal.getAmbiente()
        );
    }
}
