package com.filipe.api.dto.cliente;

import java.util.UUID;

public record ClienteResumoResponse(
    UUID id,
    String nome,
    String cpf
) {}
