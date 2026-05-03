package com.filipe.api.domain.fiscal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NcmRepository extends JpaRepository<Ncm, String> {
    List<Ncm> findByCodigoContaining(String query);
    List<Ncm> findByDescricaoContainingIgnoreCase(String query);
}
