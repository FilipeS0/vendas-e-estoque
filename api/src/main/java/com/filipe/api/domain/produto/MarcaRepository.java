package com.filipe.api.domain.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MarcaRepository extends JpaRepository<Marca, UUID> {
    List<Marca> findByAtivoTrue();
}
