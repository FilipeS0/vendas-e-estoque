package com.filipe.api.domain.fiscal;

import com.filipe.api.domain.venda.Venda;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notas_fiscais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", nullable = false, unique = true)
    private Venda venda;

    private Long numero;

    private Integer serie;

    @Column(name = "chave_acesso", length = 44)
    private String chaveAcesso;

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    @Column(name = "xml_autorizado")
    private String xmlAutorizado;

    @Column(name = "url_danfe", length = 255)
    private String urlDanfe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusNfe status;

    @Column(name = "mensagem_retorno")
    private String mensagemRetorno;

    @Column(length = 50)
    private String protocolo;

    @Column(length = 20)
    private String ambiente;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
