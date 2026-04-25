package com.filipe.api.domain.estoque;

/**
 * Canonical set of stock movement types.
 *
 * Using an enum instead of a raw String removes the inconsistency where
 * VendaService wrote "SAIDA_VENDA" / "ENTRADA_CANCELAMENTO" while
 * EstoqueService accepted only "ENTRADA" / "SAIDA", breaking any filter
 * or report that tried to match both.
 *
 * + = increases stock, - = decreases stock
 */
public enum TipoMovimentacaoEstoque {

    // ── entries that ADD stock ──────────────────────────────────────────────
    ENTRADA_COMPRA,        // manual entry from a purchase
    ENTRADA_DEVOLUCAO,     // return from a sale cancellation
    AJUSTE_POSITIVO,       // inventory count — physical qty > system qty

    // ── entries that REMOVE stock ───────────────────────────────────────────
    SAIDA_VENDA,           // automatic deduction when a sale is confirmed
    SAIDA_PERDA,           // manual entry for loss / damage
    AJUSTE_NEGATIVO;       // inventory count — physical qty < system qty

    public boolean isEntrada() {
        return this == ENTRADA_COMPRA
            || this == ENTRADA_DEVOLUCAO
            || this == AJUSTE_POSITIVO;
    }

    public boolean isSaida() {
        return !isEntrada();
    }
}