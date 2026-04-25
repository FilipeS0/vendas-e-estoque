package com.filipe.api.dto.venda;

import jakarta.validation.constraints.NotBlank;

public record CancelarVendaRequest(
        @NotBlank String motivo
) {
}
