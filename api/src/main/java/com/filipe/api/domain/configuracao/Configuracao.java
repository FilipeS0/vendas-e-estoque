package com.filipe.api.domain.configuracao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "configuracoes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Configuracao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String razaoSocial;
    private String cnpj;
    private String inscricaoEstadual;
    private String endereco;
    
    // Configuração Fiscal
    private String regimeTributario;
    private String ambienteSefaz;
    
    // NFC-e
    private Integer serieNfce;
    private Long numeroSequencialNfce;
    
    // Outros
    private String impressoraTermicaIp;
    private Integer impressoraTermicaPorta;
    private Integer alertaEstoqueMinimoGlobal;
    
    // Certificado Digital A1
    @Column(name = "certificado_path")
    private String certificadoPath;
    
    @Column(name = "certificado_validade")
    private LocalDateTime certificadoValidade;
    
    @Column(name = "certificado_senha")
    private String certificadoSenha; // Criptografada
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
