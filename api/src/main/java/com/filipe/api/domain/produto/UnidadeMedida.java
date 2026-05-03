package com.filipe.api.domain.produto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnidadeMedida {
    UN("Unidade"),
    KG("Quilograma"),
    LT("Litro"),
    CX("Caixa"),
    FD("Fardo"),
    PC("Peça"),
    MT("Metro"),
    M2("Metro Quadrado"),
    M3("Metro Cúbico");

    private final String descricao;
}
