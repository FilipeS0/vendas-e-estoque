package com.filipe.api.domain.cliente;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true)
    private String cpf;

    private String email;

    private String telefone;

    private String endereco;

    @Builder.Default
    @Column(name = "limite_credito", precision = 15, scale = 2)
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "saldo_devedor", precision = 15, scale = 2)
    private BigDecimal saldoDevedor = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
