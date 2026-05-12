package com.filipe.api.mapper.usuario;

import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.dto.usuario.UsuarioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "perfilNome", source = "perfil.nome")
    @Mapping(target = "perfilId", source = "perfil.id")
    @Mapping(target = "authorities", expression = "java(mapAuthorities(usuario))")
    UsuarioResponse toResponse(Usuario usuario);

    default List<UsuarioResponse.AuthorityResponse> mapAuthorities(Usuario usuario) {
        return usuario.getAuthorities().stream()
                .map(a -> new UsuarioResponse.AuthorityResponse(a.getAuthority()))
                .toList();
    }
}