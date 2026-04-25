package com.filipe.api.domain.caixa;

import com.filipe.api.domain.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "caixas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caixa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operador_id")
    private Usuario operador;

    @CreationTimestamp
    @Column(name = "data_abertura", updatable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @Column(name = "valor_abertura", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAbertura;

    @Column(name = "valor_fechamento_sis", precision = 15, scale = 2)
    private BigDecimal valorFechamentoSistema;

    @Column(name = "valor_fechamento_fis", precision = 15, scale = 2)
    private BigDecimal valorFechamentoFisico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusCaixa status;

    @Column(precision = 15, scale = 2)
    private BigDecimal diferenca;
}
