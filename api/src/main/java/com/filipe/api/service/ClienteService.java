package com.filipe.api.service;

import com.filipe.api.domain.cliente.Cliente;
import com.filipe.api.domain.cliente.ClienteRepository;
import com.filipe.api.dto.cliente.ClienteRequest;
import com.filipe.api.dto.cliente.ClienteResponse;
import com.filipe.api.exception.BusinessException;
import com.filipe.api.mapper.cliente.ClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteResponse buscarPorId(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente nao encontrado."));
        return clienteMapper.toResponse(cliente);
    }

    public Page<ClienteResponse> listarClientes(String nome, Pageable pageable) {
        if (nome != null && !nome.trim().isEmpty()) {
            return clienteRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome.trim(), pageable)
                    .map(clienteMapper::toResponse);
        }
        return clienteRepository.findByAtivoTrue(pageable)
                .map(clienteMapper::toResponse);
    }

    @Transactional
    public ClienteResponse registrarCliente(ClienteRequest request) {
        if (request.cpf() != null && !request.cpf().trim().isEmpty() 
            && clienteRepository.findByCpf(request.cpf()).isPresent()) {
            throw new BusinessException("Ja existe um cliente cadastrado com este CPF.");
        }

        Cliente cliente = clienteMapper.toEntity(request);
        Cliente savedCliente = clienteRepository.save(cliente);
        return clienteMapper.toResponse(savedCliente);
    }

    @Transactional
    public ClienteResponse atualizarCliente(UUID id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente nao encontrado."));

        if (request.cpf() != null && !request.cpf().trim().isEmpty()) {
            clienteRepository.findByCpf(request.cpf())
                    .ifPresent(c -> {
                        if (!c.getId().equals(id)) {
                            throw new BusinessException("Ja existe outro cliente cadastrado com este CPF.");
                        }
                    });
        }

        clienteMapper.updateEntity(cliente, request);
        Cliente savedCliente = clienteRepository.save(cliente);
        return clienteMapper.toResponse(savedCliente);
    }

    @Transactional
    public void inativarCliente(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente nao encontrado."));
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }
}
