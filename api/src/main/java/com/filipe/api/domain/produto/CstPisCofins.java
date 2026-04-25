package com.filipe.api.domain.produto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * CST (Código de Situação Tributária) do PIS/COFINS
 */
@Getter
@RequiredArgsConstructor
public enum CstPisCofins {
    OPERACAO_TRIBUTAVEL_AL_NORMAL("01", "Operação Tributável (alíquota normal)"),
    OPERACAO_TRIBUTAVEL_AL_DIFERENCIADA("02", "Operação Tributável (alíquota diferenciada)"),
    OPERACAO_TRIBUTAVEL_AL_UNIDADE_MEDIDA("03", "Operação Tributável (alíquota por unidade de medida)"),
    OPERACAO_TRIBUTAVEL_MONOFASICA("04", "Operação Tributável (monofásica)"),
    OPERACAO_TRIBUTAVEL_ST("05", "Operação Tributável (Substituição Tributária)"),
    OPERACAO_TRIBUTAVEL_AL_ZERO("06", "Operação Tributável (alíquota zero)"),
    OPERACAO_ISENTA_CONTRIBUICAO("07", "Operação Isenta da Contribuição"),
    OPERACAO_SEM_INCIDENCIA_CONTRIBUICAO("08", "Operação sem Incidência da Contribuição"),
    OPERACAO_COM_SUSPENSAO_CONTRIBUICAO("09", "Operação com Suspensão da Contribuição"),
    OUTRAS_OPERACOES_SAIDA("49", "Outras Operações de Saída");

    private final String codigo;
    private final String descricao;
}
