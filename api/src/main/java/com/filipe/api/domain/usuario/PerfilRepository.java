package com.filipe.api.domain.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PerfilRepository extends JpaRepository<Perfil, UUID> {
}
