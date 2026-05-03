package com.filipe.api.domain.produto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoPrecoRepository extends JpaRepository<HistoricoPreco, UUID> {
    List<HistoricoPreco> findByProdutoIdOrderByDataAlteracaoDesc(UUID produtoId);
}
