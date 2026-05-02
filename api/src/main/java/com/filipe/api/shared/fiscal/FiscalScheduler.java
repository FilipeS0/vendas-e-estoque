package com.filipe.api.shared.fiscal;

import com.filipe.api.domain.fiscal.NotaFiscal;
import com.filipe.api.domain.fiscal.NotaFiscalRepository;
import com.filipe.api.domain.fiscal.StatusNfe;
import com.filipe.api.service.NotaFiscalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FiscalScheduler {

    private final NotaFiscalRepository notaFiscalRepository;
    private final NotaFiscalService notaFiscalService;

    /**
     * Tenta reenviar notas em contingência a cada 5 minutos.
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void processarContingencias() {
        List<NotaFiscal> notasEmContingencia = notaFiscalRepository.findByStatus(StatusNfe.CONTINGENCIA);
        
        if (notasEmContingencia.isEmpty()) return;
        
        log.info("Encontradas {} notas em contingência para reenvio.", notasEmContingencia.size());
        
        for (NotaFiscal nota : notasEmContingencia) {
            try {
                // Removemos a nota atual e tentamos emitir de novo
                // Nota: Em um sistema real, poderíamos ter um método 'transmitirContingencia'
                // para não gerar um novo ID de nota, mas aqui vamos simplificar.
                notaFiscalRepository.delete(nota);
                notaFiscalService.emitirNotaFiscal(nota.getVenda().getId());
                log.info("Nota da venda {} reenviada com sucesso.", nota.getVenda().getId());
            } catch (Exception e) {
                log.error("Falha ao reenviar nota da venda {}: {}", nota.getVenda().getId(), e.getMessage());
            }
        }
    }
}
