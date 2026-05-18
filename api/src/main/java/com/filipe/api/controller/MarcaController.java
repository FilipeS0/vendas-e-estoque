package com.filipe.api.controller;

import com.filipe.api.domain.produto.Marca;
import com.filipe.api.domain.produto.MarcaRepository;
import com.filipe.api.dto.produto.MarcaRequest;
import com.filipe.api.dto.produto.MarcaResponse;
import com.filipe.api.mapper.produto.MarcaMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/marcas")
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaRepository repository;
    private final MarcaMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERADOR')")
    public ResponseEntity<List<MarcaResponse>> listar() {
        List<MarcaResponse> marcas = repository.findByAtivoTrue().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(marcas);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<MarcaResponse> criar(@Valid @RequestBody MarcaRequest request) {
        Marca marca = Marca.builder()
                .nome(request.nome().trim())
                .ativo(true)
                .build();
        
        Marca salva = repository.save(marca);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(salva));
    }
}
