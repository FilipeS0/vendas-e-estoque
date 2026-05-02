package com.filipe.api.dto.usuario;

import java.util.UUID;

public record PerfilResponse(
    UUID id,
    String nome
) {}
