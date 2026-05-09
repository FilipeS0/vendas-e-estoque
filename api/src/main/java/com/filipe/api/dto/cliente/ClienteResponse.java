package com.filipe.api.dto.cliente;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClienteResponse(
    UUID id,
    String nome,
    String cpf,
    String email,
    String telefone,
    String cep,
    String logradouro,
    String numero,
    String bairro,
    String cidade,
    String uf,
    String complemento,
    java.math.BigDecimal limiteCredito,
    java.math.BigDecimal saldoDevedor,
    Boolean ativo,
    LocalDateTime createdAt
) {}
