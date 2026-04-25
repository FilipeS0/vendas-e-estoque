package com.filipe.api.dto.venda;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record FinalizarVendaRequest(
    @NotEmpty(message = "Pelo menos uma forma de pagamento é obrigatória")
    @Valid
    List<PagamentoRequest> pagamentos
) {}
