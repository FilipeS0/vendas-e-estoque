package com.filipe.api.domain.venda;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pagamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false)
    private FormaPagamento formaPagamento;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    // Troco only applies to DINHEIRO payments
    @Column(precision = 15, scale = 2)
    private BigDecimal troco;

    // For card payments (optional)
    private String nsu;
    private String autorizacao;
}
