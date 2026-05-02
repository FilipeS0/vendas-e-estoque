package com.filipe.api.mapper.usuario;

import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.dto.usuario.UsuarioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "perfilNome", source = "perfil.nome")
    @Mapping(target = "perfilId", source = "perfil.id")
    UsuarioResponse toResponse(Usuario usuario);
}
