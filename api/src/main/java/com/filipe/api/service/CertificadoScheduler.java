package com.filipe.api.service;

import com.filipe.api.domain.configuracao.Configuracao;
import com.filipe.api.domain.configuracao.ConfiguracaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificadoScheduler {

    private final ConfiguracaoRepository configuracaoRepository;

    @Scheduled(cron = "0 0 8 * * *") // Runs every day at 8 AM
    public void monitorarVencimentoCertificado() {
        List<Configuracao> configuracoes = configuracaoRepository.findAll();
        if (configuracoes.isEmpty()) return;
        
        Configuracao config = configuracoes.get(0);
        if (config.getCertificadoValidade() == null) {
            log.warn("[ALERTA] Certificado digital não configurado no sistema!");
            return;
        }

        long diasRestantes = ChronoUnit.DAYS.between(LocalDateTime.now(), config.getCertificadoValidade());
        if (diasRestantes <= 0) {
            log.error("[URGENTE] O Certificado Digital A1 expirou!");
        } else if (diasRestantes <= 60) {
            log.warn("[ALERTA] O Certificado Digital A1 vence em {} dias. Por favor, providencie a renovação.", diasRestantes);
        } else {
            log.info("Certificado Digital OK. Vence em {} dias.", diasRestantes);
        }
    }
}
