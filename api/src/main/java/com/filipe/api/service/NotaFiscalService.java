package com.filipe.api.service;

import com.filipe.api.domain.configuracao.Configuracao;
import com.filipe.api.domain.configuracao.ConfiguracaoRepository;
import com.filipe.api.domain.fiscal.NotaFiscal;
import com.filipe.api.domain.fiscal.NotaFiscalRepository;
import com.filipe.api.domain.fiscal.StatusNfe;
import com.filipe.api.domain.venda.Venda;
import com.filipe.api.domain.venda.VendaRepository;
import com.filipe.api.dto.fiscal.NotaFiscalResponse;
import com.filipe.api.exception.BusinessException;
import com.filipe.api.mapper.fiscal.NotaFiscalMapper;
import com.filipe.api.shared.fiscal.NfcePayload;
import com.filipe.api.shared.fiscal.NfceResponse;
import com.filipe.api.shared.fiscal.SefazClient;
import com.filipe.api.shared.report.PdfReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotaFiscalService {

    private static final int SERIE_PADRAO = 1;

    private final VendaRepository vendaRepository;
    private final NotaFiscalRepository notaFiscalRepository;
    private final NotaFiscalMapper notaFiscalMapper;
    private final ConfiguracaoRepository configuracaoRepository;
    private final SefazClient sefazClient;
    private final PdfReportGenerator pdfReportGenerator;

    public NotaFiscalResponse buscarPorVenda(UUID vendaId) {
        return notaFiscalRepository.findByVendaId(vendaId)
                .map(notaFiscalMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Nota fiscal nao encontrada para esta venda."));
    }

    @Transactional(readOnly = true)
    public byte[] gerarDanfe(UUID vendaId) {
        NotaFiscal notaFiscal = notaFiscalRepository.findByVendaId(vendaId)
                .orElseThrow(() -> new BusinessException("Nota fiscal nao encontrada para esta venda."));
        
        return pdfReportGenerator.gerarDanfeNfce(notaFiscal);
    }

    @Transactional
    public NotaFiscalResponse emitirNotaFiscal(UUID vendaId) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new BusinessException("Venda nao encontrada."));

        if (notaFiscalRepository.findByVendaId(vendaId).isPresent()) {
            throw new BusinessException("Ja existe nota fiscal vinculada a esta venda.");
        }

        if (venda.getStatus() == null || !"CONFIRMADA".equals(venda.getStatus().name())) {
            throw new BusinessException("A nota fiscal so pode ser gerada para vendas confirmadas.");
        }

        Configuracao config = configuracaoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new BusinessException("Configuracao da empresa nao encontrada."));

        // Decidir se usa Mock ou Real
        if (config.getApiTokenFiscal() == null || config.getApiTokenFiscal().isBlank()) {
            log.info("API Token Fiscal não configurado. Gerando Nota Fiscal MOCK.");
            return emitirNotaFiscalMock(venda, config);
        }

        return emitirNotaFiscalReal(venda, config);
    }

    private NotaFiscalResponse emitirNotaFiscalMock(Venda venda, Configuracao config) {
        LocalDateTime dataEmissao = LocalDateTime.now();
        String chaveAcesso = gerarChaveAcessoMock(venda.getId(), dataEmissao);
        Long numero = venda.getNumero() != null ? venda.getNumero() : Math.abs(venda.getId().getMostSignificantBits());
        String xmlAutorizado = montarXmlMock(venda, numero, chaveAcesso, dataEmissao);

        NotaFiscal notaFiscal = NotaFiscal.builder()
                .venda(venda)
                .numero(numero)
                .serie(config.getSerieNfce() != null ? config.getSerieNfce() : SERIE_PADRAO)
                .chaveAcesso(chaveAcesso)
                .dataEmissao(dataEmissao)
                .xmlAutorizado(xmlAutorizado)
                .urlDanfe("/mock/nfce/" + venda.getId())
                .status(StatusNfe.AUTORIZADA)
                .mensagemRetorno("NFC-e mock gerada com sucesso (Token não configurado).")
                .protocolo("MOCK-" + chaveAcesso.substring(chaveAcesso.length() - 8))
                .ambiente(config.getAmbienteSefaz())
                .build();

        return notaFiscalMapper.toResponse(notaFiscalRepository.save(notaFiscal));
    }

    private NotaFiscalResponse emitirNotaFiscalReal(Venda venda, Configuracao config) {
        NfcePayload payload = NfcePayload.builder()
                .nomeDestinatario(venda.getCliente() != null ? venda.getCliente().getNome() : "CONSUMIDOR")
                .cpfDestinatario(venda.getCliente() != null ? venda.getCliente().getCpf() : null)
                .items(venda.getItens().stream().map(item -> NfcePayload.Item.builder()
                        .codigo(item.getProduto().getCodigoBarras())
                        .descricao(item.getProduto().getNome())
                        .ncm("00000000") // Mapear corretamente no futuro
                        .cfop("5102")    // Mapear corretamente no futuro
                        .quantidade(item.getQuantidade())
                        .valorUnitario(item.getPrecoUnitario())
                        .valorTotal(item.getValorTotal())
                        .build()).toList())
                .pagamentos(venda.getPagamentos().stream().map(p -> NfcePayload.Pagamento.builder()
                        .formaPagamento(p.getFormaPagamento().name())
                        .valor(p.getValor())
                        .build()).toList())
                .build();

        NfceResponse response;
        try {
            response = sefazClient.emitirNfce(payload, config.getApiTokenFiscal(), config.getAmbienteSefaz());
        } catch (Exception e) {
            log.error("Erro na comunicação com SEFAZ. Entrando em CONTINGÊNCIA: {}", e.getMessage());
            response = NfceResponse.builder()
                    .status(StatusNfe.CONTINGENCIA)
                    .mensagemRetorno("Contingência off-line: " + e.getMessage())
                    .build();
        }

        NotaFiscal notaFiscal = NotaFiscal.builder()
                .venda(venda)
                .numero(config.getNumeroSequencialNfce())
                .serie(config.getSerieNfce())
                .chaveAcesso(response.getChaveAcesso())
                .dataEmissao(LocalDateTime.now())
                .xmlAutorizado(response.getXmlAutorizado())
                .urlDanfe(response.getUrlDanfe())
                .status(response.getStatus())
                .mensagemRetorno(response.getMensagemRetorno())
                .protocolo(response.getProtocolo())
                .ambiente(config.getAmbienteSefaz())
                .build();

        // Incrementar sequencial se sucesso
        if (response.getStatus() == StatusNfe.AUTORIZADA) {
            config.setNumeroSequencialNfce(config.getNumeroSequencialNfce() + 1);
            configuracaoRepository.save(config);
        }

        return notaFiscalMapper.toResponse(notaFiscalRepository.save(notaFiscal));
    }

    @Transactional
    public void cancelarNotaFiscal(UUID vendaId, String motivo) {
        notaFiscalRepository.findByVendaId(vendaId).ifPresent(notaFiscal -> {
            Configuracao config = configuracaoRepository.findAll().stream().findFirst().orElse(null);
            
            if (config != null && config.getApiTokenFiscal() != null && !config.getApiTokenFiscal().isBlank()) {
                try {
                    sefazClient.cancelarNfce(notaFiscal.getChaveAcesso(), motivo, config.getApiTokenFiscal(), config.getAmbienteSefaz());
                } catch (Exception e) {
                    log.error("Erro ao cancelar nota na SEFAZ: {}", e.getMessage());
                }
            }

            notaFiscal.setStatus(StatusNfe.CANCELADA);
            notaFiscal.setMensagemRetorno("NFC-e cancelada. Motivo: " + motivo);
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
