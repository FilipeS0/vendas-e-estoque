package com.filipe.api.mapper.fornecedor;

import com.filipe.api.domain.produto.Fornecedor;
import com.filipe.api.dto.fornecedor.FornecedorRequest;
import com.filipe.api.dto.fornecedor.FornecedorResponse;
import org.springframework.stereotype.Component;

@Component
public class FornecedorMapper {

    public Fornecedor toEntity(FornecedorRequest request) {
        return Fornecedor.builder()
                .nome(request.nome())
                .cnpj(request.cnpj())
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

    public void updateEntity(Fornecedor fornecedor, FornecedorRequest request) {
        fornecedor.setNome(request.nome());
        fornecedor.setCnpj(request.cnpj());
        fornecedor.setEmail(request.email());
        fornecedor.setTelefone(request.telefone());
        fornecedor.setCep(request.cep());
        fornecedor.setLogradouro(request.logradouro());
        fornecedor.setNumero(request.numero());
        fornecedor.setBairro(request.bairro());
        fornecedor.setCidade(request.cidade());
        fornecedor.setUf(request.uf());
        fornecedor.setComplemento(request.complemento());
    }

    public FornecedorResponse toResponse(Fornecedor fornecedor) {
        return new FornecedorResponse(
                fornecedor.getId(),
                fornecedor.getNome(),
                fornecedor.getCnpj(),
                fornecedor.getEmail(),
                fornecedor.getTelefone(),
                fornecedor.getCep(),
                fornecedor.getLogradouro(),
                fornecedor.getNumero(),
                fornecedor.getBairro(),
                fornecedor.getCidade(),
                fornecedor.getUf(),
                fornecedor.getComplemento(),
                fornecedor.getAtivo(),
                fornecedor.getCreatedAt(),
                fornecedor.getUpdatedAt()
        );
    }
}
