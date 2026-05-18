package com.filipe.api.controller;

import com.filipe.api.domain.fiscal.Ncm;
import com.filipe.api.domain.fiscal.NcmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ncm")
@RequiredArgsConstructor
public class NcmController {

    private final NcmRepository ncmRepository;

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERADOR')")
    public ResponseEntity<List<Ncm>> search(@RequestParam String query) {
        String cleanQuery = query.trim();
        
        if (cleanQuery.matches("\\d+")) {
            List<Ncm> locals = ncmRepository.findByCodigoContaining(cleanQuery);
            
            // On-demand integration with BrasilAPI for exact 8-digit NCM queries
            if (locals.isEmpty() && cleanQuery.length() == 8) {
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    String url = "https://brasilapi.com.br/api/ncm/v1/" + cleanQuery;
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                    if (response != null && response.containsKey("codigo") && response.containsKey("descricao")) {
                        String rawCodigo = (String) response.get("codigo");
                        String cleanedCodigo = rawCodigo.replace(".", "");
                        String descricao = (String) response.get("descricao");
                        
                        Ncm newNcm = new Ncm(cleanedCodigo, descricao);
                        ncmRepository.save(newNcm);
                        
                        return ResponseEntity.ok(List.of(newNcm));
                    }
                } catch (Exception e) {
                    // Fail silently and return empty list
                }
            }
            return ResponseEntity.ok(locals);
        } else {
            return ResponseEntity.ok(ncmRepository.findByDescricaoContainingIgnoreCase(cleanQuery));
        }
    }
}
