package com.filipe.api.domain.produto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrigemProduto {
    NACIONAL(0, "Nacional"),
    ESTRANGEIRA_IMPORTACAO_DIRETA(1, "Estrangeira - Importação direta"),
    ESTRANGEIRA_ADQUIRIDA_MERCADO_INTERNO(2, "Estrangeira - Adquirida no mercado interno"),
    NACIONAL_CONTEUDO_IMPORTACAO_SUPERIOR_40(3, "Nacional - Conteúdo de importação superior a 40%"),
    NACIONAL_PROCESSOS_PRODUTIVOS_BASICOS(4, "Nacional - Processos produtivos básicos"),
    NACIONAL_CONTEUDO_IMPORTACAO_INFERIOR_40(5, "Nacional - Conteúdo de importação inferior a 40%"),
    ESTRANGEIRA_IMPORTACAO_DIRETA_SEM_SIMILAR_NACIONAL(6, "Estrangeira - Importação direta, sem similar nacional"),
    ESTRANGEIRA_ADQUIRIDA_MERCADO_INTERNO_SEM_SIMILAR_NACIONAL(7, "Estrangeira - Adquirida no mercado interno, sem similar nacional"),
    NACIONAL_CONTEUDO_IMPORTACAO_SUPERIOR_70(8, "Nacional - Conteúdo de importação superior a 70%");

    private final int codigo;
    private final String descricao;
}
