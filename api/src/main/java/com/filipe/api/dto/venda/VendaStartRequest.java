package com.filipe.api.dto.venda;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VendaStartRequest(
    UUID operadorId,
    @NotNull UUID caixaId,
    UUID clienteId
) {}
