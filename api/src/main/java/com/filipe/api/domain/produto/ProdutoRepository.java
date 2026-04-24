package com.filipe.api.domain.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
    boolean existsByCodigoBarras(String codigoBarras);
}
