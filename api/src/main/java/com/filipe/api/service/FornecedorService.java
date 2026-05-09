package com.filipe.api.service;

import com.filipe.api.domain.produto.Fornecedor;
import com.filipe.api.domain.produto.FornecedorRepository;
import com.filipe.api.dto.fornecedor.FornecedorRequest;
import com.filipe.api.dto.fornecedor.FornecedorResponse;
import com.filipe.api.exception.BusinessException;
import com.filipe.api.mapper.fornecedor.FornecedorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository repository;
    private final FornecedorMapper mapper;

    public Page<FornecedorResponse> listar(Pageable pageable) {
        return repository.findByAtivoTrue(pageable)
                .map(mapper::toResponse);
    }

    @Transactional
    public FornecedorResponse criar(FornecedorRequest request) {
        Fornecedor fornecedor = mapper.toEntity(request);
        return mapper.toResponse(repository.save(fornecedor));
    }

    @Transactional
    public FornecedorResponse atualizar(UUID id, FornecedorRequest request) {
        Fornecedor fornecedor = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Fornecedor não encontrado"));
        mapper.updateEntity(fornecedor, request);
        return mapper.toResponse(repository.save(fornecedor));
    }

    @Transactional
    public void deletar(UUID id) {
        Fornecedor fornecedor = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Fornecedor não encontrado"));
        fornecedor.setAtivo(false);
        repository.save(fornecedor);
    }
}
