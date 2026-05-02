package com.filipe.api.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UsuarioUpdateRequest(
    @NotBlank String nome,
    @NotBlank @Email String email,
    String senha,
    @NotNull UUID perfilId,
    Boolean ativo
) {}
