package com.filipe.api.service;

import com.filipe.api.domain.configuracao.Configuracao;
import com.filipe.api.domain.configuracao.ConfiguracaoRepository;
import com.filipe.api.dto.configuracao.ConfiguracaoRequest;
import com.filipe.api.dto.configuracao.ConfiguracaoResponse;
import com.filipe.api.mapper.ConfiguracaoMapper;
import com.filipe.api.shared.crypto.CryptoService;
import com.filipe.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ConfiguracaoService {

    private final ConfiguracaoRepository configuracaoRepository;
    private final ConfiguracaoMapper configuracaoMapper;
    private final CryptoService cryptoService;

    @Value("${api.fiscal.certificado.path:./certificados}")
    private String certificadoDir;

    public ConfiguracaoResponse obterConfiguracao() {
        List<Configuracao> configuracoes = configuracaoRepository.findAll();
        if (configuracoes.isEmpty()) {
            return null;
        }
        return configuracaoMapper.toResponse(configuracoes.get(0));
    }

    public ConfiguracaoResponse salvarConfiguracao(ConfiguracaoRequest request) {
        List<Configuracao> configuracoes = configuracaoRepository.findAll();
        Configuracao configuracao;
        
        if (configuracoes.isEmpty()) {
            configuracao = configuracaoMapper.toEntity(request);
        } else {
            configuracao = configuracoes.get(0);
            configuracao.setRazaoSocial(request.razaoSocial());
            configuracao.setCnpj(request.cnpj());
            configuracao.setInscricaoEstadual(request.inscricaoEstadual());
            configuracao.setEndereco(request.endereco());
            configuracao.setRegimeTributario(request.regimeTributario());
            configuracao.setAmbienteSefaz(request.ambienteSefaz());
            configuracao.setApiTokenFiscal(request.apiTokenFiscal());
            configuracao.setSerieNfce(request.serieNfce());
            configuracao.setNumeroSequencialNfce(request.numeroSequencialNfce());
            configuracao.setImpressoraTermicaIp(request.impressoraTermicaIp());
            configuracao.setImpressoraTermicaPorta(request.impressoraTermicaPorta());
            configuracao.setAlertaEstoqueMinimoGlobal(request.alertaEstoqueMinimoGlobal());
        }
        
        return configuracaoMapper.toResponse(configuracaoRepository.save(configuracao));
    }

    @Transactional
    public void salvarCertificado(MultipartFile file, String senha) {
        try {
            // 1. Validar certificado e extrair validade
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream is = file.getInputStream()) {
                ks.load(is, senha.toCharArray());
            }
            
            LocalDateTime validade = null;
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (ks.isKeyEntry(alias)) {
                    X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
                    validade = cert.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    break;
                }
            }

            if (validade == null) {
                throw new BusinessException("Certificado inválido: não foi possível encontrar a chave privada.");
            }

            // 2. Criar diretório se não existir
            Path uploadDir = Paths.get(certificadoDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 3. Salvar arquivo
            String filename = "certificado_" + System.currentTimeMillis() + ".pfx";
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 4. Atualizar banco de dados
            Configuracao config = configuracaoRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new BusinessException("Configure a empresa antes de subir o certificado."));
            
            config.setCertificadoPath(filePath.toString());
            config.setCertificadoValidade(validade);
            config.setCertificadoSenha(cryptoService.encrypt(senha));
            configuracaoRepository.save(config);

        } catch (Exception e) {
            if (e instanceof BusinessException) throw (BusinessException) e;
            throw new BusinessException("Erro ao processar certificado: " + e.getMessage());
        }
    }

    public Map<String, Object> getStatusCertificado() {
        Configuracao config = configuracaoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new BusinessException("Configuração não encontrada."));

        if (config.getCertificadoValidade() == null) {
            return Map.of("status", "NOT_FOUND");
        }

        long diasRestantes = ChronoUnit.DAYS.between(LocalDateTime.now(), config.getCertificadoValidade());
        
        return Map.of(
            "validade", config.getCertificadoValidade(),
            "diasRestantes", diasRestantes,
            "alerta", diasRestantes < 60
        );
    }
}
