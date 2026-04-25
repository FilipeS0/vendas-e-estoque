package com.filipe.api.dto.cliente;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClienteResponse(
    UUID id,
    String nome,
    String cpf,
    String email,
    String telefone,
    String endereco,
    Boolean ativo,
    LocalDateTime createdAt
) {}
