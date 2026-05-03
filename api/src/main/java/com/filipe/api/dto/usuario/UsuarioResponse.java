package com.filipe.api.dto.usuario;

import java.util.UUID;

public record UsuarioResponse(
    UUID id,
    String nome,
    String email,
    String perfilNome,
    UUID perfilId,
    Boolean ativo
) {}
