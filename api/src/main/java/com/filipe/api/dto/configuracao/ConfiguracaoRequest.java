package com.filipe.api.dto.configuracao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfiguracaoRequest(
    @NotBlank String razaoSocial,
    @NotBlank String cnpj,
    String inscricaoEstadual,
    String endereco,
    @NotBlank String regimeTributario,
    @NotBlank String ambienteSefaz,
    String apiTokenFiscal,
    @NotNull Integer serieNfce,
    @NotNull Long numeroSequencialNfce,
    String impressoraTermicaIp,
    Integer impressoraTermicaPorta,
    @NotNull Integer alertaEstoqueMinimoGlobal
) {}
