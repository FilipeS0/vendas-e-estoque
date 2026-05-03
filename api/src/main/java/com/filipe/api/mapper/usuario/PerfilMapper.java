package com.filipe.api.mapper.usuario;

import com.filipe.api.domain.usuario.Perfil;
import com.filipe.api.dto.usuario.PerfilResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PerfilMapper {
    PerfilResponse toResponse(Perfil perfil);
}
