package com.filipe.api.dto.configuracao;

import java.util.UUID;

public record ConfiguracaoResponse(
    UUID id,
    String razaoSocial,
    String cnpj,
    String inscricaoEstadual,
    String endereco,
    String regimeTributario,
    String ambienteSefaz,
    Integer serieNfce,
    Long numeroSequencialNfce,
    String impressoraTermicaIp,
    Integer impressoraTermicaPorta,
    Integer alertaEstoqueMinimoGlobal
) {}
