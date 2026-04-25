package com.filipe.api.dto.cliente;

import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
    @NotBlank String nome,
    String cpf,
    String email,
    String telefone,
    String endereco
) {}
