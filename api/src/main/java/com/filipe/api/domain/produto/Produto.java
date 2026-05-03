package com.filipe.api.domain.produto;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo_interno", unique = true, length = 50)
    private String codigoInterno;

    @Column(name = "codigo_barras", unique = true, length = 13)
    private String codigoBarras;

    @Column(nullable = false, length = 200)
    private String nome;

    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_medida", nullable = false, length = 2)
    private UnidadeMedida unidadeMedida = UnidadeMedida.UN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Column(name = "preco_custo", nullable = false, precision = 15, scale = 2)
    private BigDecimal precoCusto;

    @Column(name = "preco_venda", nullable = false, precision = 15, scale = 2)
    private BigDecimal precoVenda;

    @Column(nullable = false, length = 8)
    private String ncm;

    @Column(length = 10)
    private String cest;

    @Column(nullable = false, length = 4)
    private String cfop;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private OrigemProduto origem;

    @Enumerated(EnumType.STRING)
    @Column(length = 3)
    private Csosn csosn;

    @Enumerated(EnumType.STRING)
    @Column(name = "cst_pis_cofins", length = 2)
    private CstPisCofins cstPisCofins;

    @Column(name = "situacao_tributaria", length = 50)
    private String situacaoTributaria;

    @Column(name = "aliquota_icms", precision = 5, scale = 2)
    private BigDecimal aliquotaIcms;

    @Column(name = "aliquota_pis", precision = 5, scale = 2)
    private BigDecimal aliquotaPis;

    @Column(name = "aliquota_cofins", precision = 5, scale = 2)
    private BigDecimal aliquotaCofins;

    @Column(name = "imagem_url")
    private String imagemUrl;

    @Builder.Default
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
