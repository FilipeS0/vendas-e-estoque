package com.filipe.api.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UsuarioRequest(
    @NotBlank String nome,
    @NotBlank @Email String email,
    @NotBlank String senha,
    @NotNull UUID perfilId,
    Boolean ativo
) {}
