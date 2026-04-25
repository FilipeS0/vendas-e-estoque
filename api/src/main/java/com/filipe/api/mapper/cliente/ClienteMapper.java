package com.filipe.api.mapper.cliente;

import com.filipe.api.domain.cliente.Cliente;
import com.filipe.api.dto.cliente.ClienteRequest;
import com.filipe.api.dto.cliente.ClienteResponse;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequest request) {
        return Cliente.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .email(request.email())
                .telefone(request.telefone())
                .endereco(request.endereco())
                .ativo(true)
                .build();
    }

    public void updateEntity(Cliente cliente, ClienteRequest request) {
        cliente.setNome(request.nome());
        cliente.setCpf(request.cpf());
        cliente.setEmail(request.email());
        cliente.setTelefone(request.telefone());
        cliente.setEndereco(request.endereco());
    }

    public ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getEndereco(),
                cliente.getLimiteCredito(),
                cliente.getSaldoDevedor(),
                cliente.getAtivo(),
                cliente.getCreatedAt()
        );
    }
}
