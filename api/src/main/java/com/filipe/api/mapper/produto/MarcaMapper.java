package com.filipe.api.mapper.produto;

import com.filipe.api.domain.produto.Marca;
import com.filipe.api.dto.produto.MarcaRequest;
import com.filipe.api.dto.produto.MarcaResponse;
import org.springframework.stereotype.Component;

@Component
public class MarcaMapper {

    public Marca toEntity(MarcaRequest request) {
        return Marca.builder()
                .nome(request.nome())
                .ativo(true)
                .build();
    }

    public MarcaResponse toResponse(Marca marca) {
        return new MarcaResponse(
                marca.getId(),
                marca.getNome(),
                marca.getAtivo()
        );
    }
}
