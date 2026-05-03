package com.filipe.api.controller;

import com.filipe.api.domain.fiscal.Ncm;
import com.filipe.api.domain.fiscal.NcmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ncm")
@RequiredArgsConstructor
public class NcmController {

    private final NcmRepository ncmRepository;

    @GetMapping("/search")
    public ResponseEntity<List<Ncm>> search(@RequestParam String query) {
        if (query.matches("\\d+")) {
            return ResponseEntity.ok(ncmRepository.findByCodigoContaining(query));
        } else {
            return ResponseEntity.ok(ncmRepository.findByDescricaoContainingIgnoreCase(query));
        }
    }
}
