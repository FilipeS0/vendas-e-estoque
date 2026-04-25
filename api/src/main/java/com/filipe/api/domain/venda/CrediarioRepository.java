package com.filipe.api.domain.venda;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CrediarioRepository extends JpaRepository<Crediario, UUID> {
}
