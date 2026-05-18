package com.filipe.api.dto.produto;

import java.util.UUID;

public record MarcaResponse(
        UUID id,
        String nome,
        Boolean ativo
) {
}
