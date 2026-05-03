package com.filipe.api.service;

import com.filipe.api.domain.usuario.Perfil;
import com.filipe.api.domain.usuario.PerfilRepository;
import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.domain.usuario.UsuarioRepository;
import com.filipe.api.dto.usuario.PerfilResponse;
import com.filipe.api.dto.usuario.UsuarioRequest;
import com.filipe.api.dto.usuario.UsuarioResponse;
import com.filipe.api.dto.usuario.UsuarioUpdateRequest;
import com.filipe.api.exception.BusinessException;
import com.filipe.api.mapper.usuario.PerfilMapper;
import com.filipe.api.mapper.usuario.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final UsuarioMapper usuarioMapper;
    private final PerfilMapper perfilMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(usuarioMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado."));
        return usuarioMapper.toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("Ja existe um usuario com este e-mail.");
        }

        Perfil perfil = perfilRepository.findById(request.perfilId())
                .orElseThrow(() -> new BusinessException("Perfil nao encontrado."));

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .perfil(perfil)
                .ativo(request.ativo() != null ? request.ativo() : true)
                .build();

        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(UUID id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado."));

        if (!usuario.getEmail().equals(request.email()) && usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("Ja existe um usuario com este e-mail.");
        }

        Perfil perfil = perfilRepository.findById(request.perfilId())
                .orElseThrow(() -> new BusinessException("Perfil nao encontrado."));

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setPerfil(perfil);
        if (request.ativo() != null) {
            usuario.setAtivo(request.ativo());
        }

        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        }

        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void inativar(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado."));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<PerfilResponse> listarPerfis() {
        return perfilRepository.findAll().stream()
                .map(perfilMapper::toResponse)
                .collect(Collectors.toList());
    }
}
