package com.filipe.api.domain.produto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * CSOSN (Código de Situação da Operação no Simples Nacional)
 * Utilizado por empresas optantes pelo Simples Nacional.
 */
@Getter
@RequiredArgsConstructor
public enum Csosn {
    TRIBUTADA_COM_PERMISSAO_CREDITO("101", "Tributada com permissão de crédito"),
    TRIBUTADA_SEM_PERMISSAO_CREDITO("102", "Tributada sem permissão de crédito"),
    ISENTA_OU_NAO_TRIBUTADA_COM_PERMISSAO_CREDITO("103", "Isenta ou não tributada com permissão de crédito"),
    TRIBUTADA_COM_ST("201", "Tributada com permissão de crédito e com cobrança de ICMS por ST"),
    TRIBUTADA_SEM_PERMISSAO_CREDITO_COM_ST("202", "Tributada sem permissão de crédito e com cobrança de ICMS por ST"),
    ISENTA_OU_NAO_TRIBUTADA_COM_ST("203", "Isenta ou não tributada e com cobrança de ICMS por ST"),
    ISENTA("300", "Isenta"),
    NAO_TRIBUTADA("400", "Não tributada"),
    SUSPENSAO("500", "Suspensão"),
    OUTROS("900", "Outros");

    private final String codigo;
    private final String descricao;
}
