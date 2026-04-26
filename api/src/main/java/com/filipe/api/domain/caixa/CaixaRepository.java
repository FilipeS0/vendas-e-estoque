package com.filipe.api.domain.caixa;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaixaRepository extends JpaRepository<Caixa, UUID> {
    Optional<Caixa> findByIdAndStatus(UUID id, StatusCaixa status);
    boolean existsByOperadorIdAndStatus(UUID operadorId, StatusCaixa status);
    List<Caixa> findByOperadorId(UUID operadorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Caixa> findByIdWithLock(UUID id);
}
