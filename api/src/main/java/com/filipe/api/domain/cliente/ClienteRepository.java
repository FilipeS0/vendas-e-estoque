package com.filipe.api.domain.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByCpf(String cpf);
    Page<Cliente> findByAtivoTrue(Pageable pageable);
    Page<Cliente> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome, Pageable pageable);
}
