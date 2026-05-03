package com.filipe.api.shared.fiscal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filipe.api.domain.fiscal.StatusNfe;
import com.filipe.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FocusNfeClient implements SefazClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private static final String URL_HOMOLOGACAO = "https://homologacao.focusnfe.com.br/v2/nfce";
    private static final String URL_PRODUCAO = "https://api.focusnfe.com.br/v2/nfce";

    @Override
    public NfceResponse emitirNfce(NfcePayload payload, String token, String ambiente) {
        String url = "PRODUCAO".equalsIgnoreCase(ambiente) ? URL_PRODUCAO : URL_HOMOLOGACAO;
        
        try {
            Map<String, Object> focusBody = Map.of(
                "natureza_operacao", "Venda de mercadoria",
                "data_emissao", java.time.OffsetDateTime.now().toString(),
                "tipo_operacao", 1, // Saída
                "presenca_comprador", 1, // Operação presencial
                "cnpj_emitente", "FIXME", // Deveria vir da config
                "nome_destinatario", payload.getNomeDestinatario() != null ? payload.getNomeDestinatario() : "",
                "cpf_destinatario", payload.getCpfDestinatario() != null ? payload.getCpfDestinatario() : "",
                "items", payload.getItems().stream().map(item -> Map.<String, Object>ofEntries(
                    Map.entry("numero_item", payload.getItems().indexOf(item) + 1),
                    Map.entry("codigo_produto", item.getCodigo()),
                    Map.entry("descricao", item.getDescricao()),
                    Map.entry("ncm", item.getNcm()),
                    Map.entry("cfop", item.getCfop()),
                    Map.entry("unidade_comercial", "UN"),
                    Map.entry("quantidade_comercial", item.getQuantidade()),
                    Map.entry("valor_unitario_comercial", item.getValorUnitario()),
                    Map.entry("valor_bruto", item.getValorTotal()),
                    Map.entry("icms_situacao_tributaria", "102"), // Simples Nacional
                    Map.entry("icms_origem", 0)
                )).toList(),
                "formas_pagamento", payload.getPagamentos().stream().map(p -> Map.of(
                    "forma_pagamento", "99", // Outros (ou mapear correto)
                    "valor_pagamento", p.getValor()
                )).toList()
            );

            String jsonBody = objectMapper.writeValueAsString(focusBody);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((token + ":").getBytes()))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            log.info("Sending NFC-e to Focus NF-e ({}): {}", ambiente, url);
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 400 && response.statusCode() != 422) {
                throw new BusinessException("Erro na comunicação com Focus NF-e: HTTP " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String status = root.path("status").asText();
            
            if ("autorizado".equalsIgnoreCase(status)) {
                return NfceResponse.builder()
                        .status(StatusNfe.AUTORIZADA)
                        .chaveAcesso(root.path("chave_nfe").asText())
                        .protocolo(root.path("protocolo").asText())
                        .xmlAutorizado(root.path("caminho_xml_nota_fiscal").asText()) // Geralmente é uma URL ou o próprio XML
                        .urlDanfe(root.path("caminho_danfe").asText())
                        .mensagemRetorno("Autorizada com sucesso")
                        .build();
            } else if ("processando_autorizacao".equalsIgnoreCase(status)) {
                return NfceResponse.builder()
                        .status(StatusNfe.AGUARDANDO_SEFAZ)
                        .mensagemRetorno("Processando na SEFAZ")
                        .build();
            } else {
                return NfceResponse.builder()
                        .status(StatusNfe.REJEITADA)
                        .mensagemRetorno(root.path("mensagem_sefaz").asText("Rejeitada"))
                        .build();
            }

        } catch (Exception e) {
            log.error("Failed to emit NFC-e via Focus NF-e", e);
            throw new BusinessException("Falha na emissão da NFC-e: " + e.getMessage());
        }
    }

    @Override
    public void cancelarNfce(String chaveAcesso, String motivo, String token, String ambiente) {
        // Implementação similar usando DELETE ou POST /cancelar
        log.warn("Cancelamento real via Focus NF-e não implementado no esqueleto.");
    }
}
