package com.filipe.api.domain.configuracao;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ConfiguracaoRepository extends JpaRepository<Configuracao, UUID> {
}
