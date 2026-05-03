package com.filipe.api.domain.fiscal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ncm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ncm {
    @Id
    private String codigo;
    private String descricao;
}
