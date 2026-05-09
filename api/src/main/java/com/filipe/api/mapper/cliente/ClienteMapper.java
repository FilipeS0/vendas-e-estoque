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
                .cep(request.cep())
                .logradouro(request.logradouro())
                .numero(request.numero())
                .bairro(request.bairro())
                .cidade(request.cidade())
                .uf(request.uf())
                .complemento(request.complemento())
                .ativo(true)
                .build();
    }

    public void updateEntity(Cliente cliente, ClienteRequest request) {
        cliente.setNome(request.nome());
        cliente.setCpf(request.cpf());
        cliente.setEmail(request.email());
        cliente.setTelefone(request.telefone());
        cliente.setCep(request.cep());
        cliente.setLogradouro(request.logradouro());
        cliente.setNumero(request.numero());
        cliente.setBairro(request.bairro());
        cliente.setCidade(request.cidade());
        cliente.setUf(request.uf());
        cliente.setComplemento(request.complemento());
    }

    public ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getCep(),
                cliente.getLogradouro(),
                cliente.getNumero(),
                cliente.getBairro(),
                cliente.getCidade(),
                cliente.getUf(),
                cliente.getComplemento(),
                cliente.getLimiteCredito(),
                cliente.getSaldoDevedor(),
                cliente.getAtivo(),
                cliente.getCreatedAt()
        );
    }
}
