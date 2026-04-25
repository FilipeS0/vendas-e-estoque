package com.filipe.api.controller;

import com.filipe.api.dto.cliente.ClienteRequest;
import com.filipe.api.dto.cliente.ClienteResponse;
import com.filipe.api.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<Page<ClienteResponse>> list(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(clienteService.listarClientes(nome, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<ClienteResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<ClienteResponse> create(@RequestBody @Valid ClienteRequest request) {
        ClienteResponse response = clienteService.registrarCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<ClienteResponse> update(@PathVariable UUID id, @RequestBody @Valid ClienteRequest request) {
        return ResponseEntity.ok(clienteService.atualizarCliente(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clienteService.inativarCliente(id);
        return ResponseEntity.noContent().build();
    }
}
