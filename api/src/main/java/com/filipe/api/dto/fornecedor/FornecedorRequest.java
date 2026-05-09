package com.filipe.api.dto.fornecedor;

import jakarta.validation.constraints.NotBlank;

public record FornecedorRequest(
    @NotBlank String nome,
    String cnpj,
    String email,
    String telefone,
    String cep,
    String logradouro,
    String numero,
    String bairro,
    String cidade,
    String uf,
    String complemento
) {}
