package com.filipe.api.domain.caixa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LancamentoCaixaRepository extends JpaRepository<LancamentoCaixa, UUID> {
    List<LancamentoCaixa> findByCaixaId(UUID caixaId);
    List<LancamentoCaixa> findByCaixaIdAndReferenciaId(UUID caixaId, UUID referenciaId);
}
