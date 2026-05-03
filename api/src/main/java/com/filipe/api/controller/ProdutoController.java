package com.filipe.api.controller;

import com.filipe.api.dto.produto.ProdutoDetalheResponse;
import com.filipe.api.dto.produto.ProdutoPdvResponse;
import com.filipe.api.dto.produto.ProdutoRequest;
import com.filipe.api.dto.produto.ProdutoResponse;
import com.filipe.api.dto.produto.ProdutoUpdateRequest;
import com.filipe.api.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.filipe.api.domain.produto.HistoricoPreco;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProdutoResponse>> list(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<ProdutoResponse> response = produtoService.listarProdutos(nome, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pdv")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<Page<ProdutoPdvResponse>> listPdv(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(produtoService.listarPdv(query, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProdutoDetalheResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @GetMapping("/codigo-barras/{codigoBarras}")
    public ResponseEntity<ProdutoDetalheResponse> findByCodigoBarras(@PathVariable String codigoBarras) {
        return ResponseEntity.ok(produtoService.buscarPorCodigoBarras(codigoBarras));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProdutoResponse> create(@RequestBody @Valid ProdutoRequest request) {
        ProdutoResponse response = produtoService.registrarProduto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProdutoResponse> update(@PathVariable UUID id, @RequestBody @Valid ProdutoUpdateRequest request) {
        return ResponseEntity.ok(produtoService.atualizarProduto(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        produtoService.inativarProduto(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/imagem")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> uploadImagem(@PathVariable UUID id, @RequestParam("imagem") MultipartFile file) {
        produtoService.salvarImagem(id, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/imagem")
    public ResponseEntity<byte[]> getImagem(@PathVariable UUID id) {
        byte[] imagem = produtoService.buscarImagem(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Standardizing to JPEG or detection
                .body(imagem);
    }

    @GetMapping("/{id}/historico-precos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HistoricoPreco>> getHistoricoPrecos(@PathVariable UUID id) {
        return ResponseEntity.ok(produtoService.buscarHistoricoPrecos(id));
    }
}
