package com.filipe.api.service;

import com.filipe.api.domain.fiscal.NotaFiscal;
import com.filipe.api.domain.fiscal.NotaFiscalRepository;
import com.filipe.api.domain.fiscal.StatusNfe;
import com.filipe.api.domain.venda.Venda;
import com.filipe.api.domain.venda.VendaRepository;
import com.filipe.api.dto.fiscal.NotaFiscalResponse;
import com.filipe.api.exception.BusinessException;
import com.filipe.api.mapper.fiscal.NotaFiscalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotaFiscalService {

    private static final int SERIE_PADRAO = 1;
    private static final String AMBIENTE_MOCK = "HOMOLOGACAO";

    private final VendaRepository vendaRepository;
    private final NotaFiscalRepository notaFiscalRepository;
    private final NotaFiscalMapper notaFiscalMapper;

    public NotaFiscalResponse buscarPorVenda(UUID vendaId) {
        return notaFiscalRepository.findByVendaId(vendaId)
                .map(notaFiscalMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Nota fiscal nao encontrada para esta venda."));
    }

    @Transactional
    public NotaFiscalResponse gerarNotaFiscalMock(UUID vendaId) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new BusinessException("Venda nao encontrada."));

        if (notaFiscalRepository.findByVendaId(vendaId).isPresent()) {
            throw new BusinessException("Ja existe nota fiscal vinculada a esta venda.");
        }

        if (venda.getStatus() == null || !"CONFIRMADA".equals(venda.getStatus().name())) {
            throw new BusinessException("A nota fiscal mock so pode ser gerada para vendas confirmadas.");
        }

        LocalDateTime dataEmissao = LocalDateTime.now();
        String chaveAcesso = gerarChaveAcessoMock(venda.getId(), dataEmissao);
        Long numero = venda.getNumero() != null ? venda.getNumero() : Math.abs(venda.getId().getMostSignificantBits());
        String xmlAutorizado = montarXmlMock(venda, numero, chaveAcesso, dataEmissao);

        NotaFiscal notaFiscal = NotaFiscal.builder()
                .venda(venda)
                .numero(numero)
                .serie(SERIE_PADRAO)
                .chaveAcesso(chaveAcesso)
                .dataEmissao(dataEmissao)
                .xmlAutorizado(xmlAutorizado)
                .urlDanfe("/mock/nfce/" + venda.getId())
                .status(StatusNfe.AUTORIZADA)
                .mensagemRetorno("NFC-e mock gerada com sucesso.")
                .protocolo("MOCK-" + chaveAcesso.substring(chaveAcesso.length() - 8))
                .ambiente(AMBIENTE_MOCK)
                .build();

        NotaFiscal savedNotaFiscal = notaFiscalRepository.save(notaFiscal);
        return notaFiscalMapper.toResponse(savedNotaFiscal);
    }

    @Transactional
    public void cancelarNotaFiscalMock(UUID vendaId, String motivo) {
        notaFiscalRepository.findByVendaId(vendaId).ifPresent(notaFiscal -> {
            notaFiscal.setStatus(StatusNfe.CANCELADA);
            notaFiscal.setMensagemRetorno("NFC-e mock cancelada. Motivo: " + motivo);
            notaFiscal.setXmlAutorizado(notaFiscal.getXmlAutorizado() + "\n<!-- CANCELADA: " + motivo + " -->");
            notaFiscalRepository.save(notaFiscal);
        });
    }

    private String gerarChaveAcessoMock(UUID vendaId, LocalDateTime dataEmissao) {
        String base = dataEmissao.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + vendaId.toString().replace("-", "");
        return base.substring(0, Math.min(base.length(), 44));
    }

    private String montarXmlMock(Venda venda, Long numero, String chaveAcesso, LocalDateTime dataEmissao) {
        return """
                <nfceMock>
                  <vendaId>%s</vendaId>
                  <numero>%s</numero>
                  <serie>%s</serie>
                  <chaveAcesso>%s</chaveAcesso>
                  <dataEmissao>%s</dataEmissao>
                  <valorTotal>%s</valorTotal>
                  <status>%s</status>
                </nfceMock>
                """.formatted(
                venda.getId(),
                numero,
                SERIE_PADRAO,
                chaveAcesso,
                dataEmissao,
                venda.getValorTotal(),
                StatusNfe.AUTORIZADA
        ).trim();
    }
}
