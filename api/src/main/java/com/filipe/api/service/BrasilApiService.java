package com.filipe.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BrasilApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://brasilapi.com.br/api";

    public Map<String, Object> consultarCnpj(String cnpj) {
        String url = BASE_URL + "/cnpj/v1/" + cnpj.replaceAll("\\D", "");
        try {
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> consultarCep(String cep) {
        String url = BASE_URL + "/cep/v1/" + cep.replaceAll("\\D", "");
        try {
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}
