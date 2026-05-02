package com.filipe.api.service;

import com.filipe.api.domain.configuracao.Configuracao;
import com.filipe.api.domain.configuracao.ConfiguracaoRepository;
import com.filipe.api.dto.configuracao.ConfiguracaoRequest;
import com.filipe.api.dto.configuracao.ConfiguracaoResponse;
import com.filipe.api.mapper.ConfiguracaoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ConfiguracaoService {

    private final ConfiguracaoRepository configuracaoRepository;
    private final ConfiguracaoMapper configuracaoMapper;

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
            configuracao.setSerieNfce(request.serieNfce());
            configuracao.setNumeroSequencialNfce(request.numeroSequencialNfce());
            configuracao.setImpressoraTermicaIp(request.impressoraTermicaIp());
            configuracao.setImpressoraTermicaPorta(request.impressoraTermicaPorta());
            configuracao.setAlertaEstoqueMinimoGlobal(request.alertaEstoqueMinimoGlobal());
        }
        
        return configuracaoMapper.toResponse(configuracaoRepository.save(configuracao));
    }
}
