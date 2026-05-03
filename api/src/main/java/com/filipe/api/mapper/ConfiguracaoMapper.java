package com.filipe.api.mapper;

import com.filipe.api.domain.configuracao.Configuracao;
import com.filipe.api.dto.configuracao.ConfiguracaoRequest;
import com.filipe.api.dto.configuracao.ConfiguracaoResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConfiguracaoMapper {
    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "createdAt", ignore = true)
    @org.mapstruct.Mapping(target = "updatedAt", ignore = true)
    Configuracao toEntity(ConfiguracaoRequest request);
    ConfiguracaoResponse toResponse(Configuracao configuracao);
}
