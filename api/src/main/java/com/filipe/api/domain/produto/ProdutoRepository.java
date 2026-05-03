package com.filipe.api.domain.produto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
    boolean existsByCodigoBarras(String codigoBarras);
    Optional<Produto> findByCodigoBarras(String codigoBarras);
    Optional<Produto> findByCodigoBarrasAndAtivoTrue(String codigoBarras);
    Page<Produto> findByAtivoTrue(Pageable pageable);
    Page<Produto> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("""
        SELECT p FROM Produto p
        WHERE p.ativo = true
        AND (LOWER(p.nome) LIKE LOWER(CONCAT('%', :query, '%'))
             OR p.codigoBarras LIKE CONCAT('%', :query, '%')
             OR p.codigoInterno LIKE LOWER(CONCAT('%', :query, '%')))
    """)
    Page<Produto> findByMultiCriteria(String query, Pageable pageable);
}
