package com.filipe.api.service;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class BrasilApiService {

    private final RestClient restClient;
    private static final String BASE_URL = "https://brasilapi.com.br/api";

    public BrasilApiService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(BASE_URL)
                .build();
    }

    public Map<String, Object> consultarCnpj(String cnpj) {
        String url = "/cnpj/v1/" + cnpj.replaceAll("\\D", "");
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> consultarCep(String cep) {
        String url = "/cep/v1/" + cep.replaceAll("\\D", "");
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}
