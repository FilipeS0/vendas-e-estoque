package com.filipe.api.dto.fornecedor;

import java.time.LocalDateTime;
import java.util.UUID;

public record FornecedorResponse(
    UUID id,
    String nome,
    String cnpj,
    String email,
    String telefone,
    String cep,
    String logradouro,
    String numero,
    String bairro,
    String cidade,
    String uf,
    String complemento,
    Boolean ativo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
