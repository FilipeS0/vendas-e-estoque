package com.filipe.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSenhaRequest(
        @NotBlank String senhaAtual,
        @NotBlank @Size(min = 6) String novaSenha
) {}
