package com.filipe.api.domain.venda;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "parcelas_crediario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelaCrediario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crediario_id", nullable = false)
    private Crediario crediario;

    @Column(name = "numero_parcela", nullable = false)
    private Integer numeroParcela;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Builder.Default
    @Column(name = "valor_pago", precision = 15, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusParcela status;
}
