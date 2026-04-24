package com.filipe.api.controller;

import com.filipe.api.domain.produto.dto.ProdutoRequest;
import com.filipe.api.domain.produto.dto.ProdutoResponse;
import com.filipe.api.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProdutoResponse> create(@RequestBody @Valid ProdutoRequest request) {
        ProdutoResponse response = produtoService.registrarProduto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
