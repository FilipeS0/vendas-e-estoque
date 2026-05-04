package com.filipe.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.dto.caixa.*;
import com.filipe.api.service.CaixaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CaixaController.class)
public class CaixaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CaixaService caixaService;

    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        mockUsuario = Usuario.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .nome("Test User")
                .build();
    }

    @Test
    public void deveAbrirCaixaComSucesso() throws Exception {
        AbrirCaixaRequest request = new AbrirCaixaRequest(new BigDecimal("100.00"));
        CaixaResponse response = CaixaResponse.builder()
                .id(UUID.randomUUID())
                .status("ABERTO")
                .valorAbertura(new BigDecimal("100.00"))
                .build();

        when(caixaService.abrirCaixa(any(AbrirCaixaRequest.class), any(Usuario.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/caixas/abrir")
                        .with(csrf())
                        .with(user(mockUsuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.valorAbertura").value(100.00));
    }

    @Test
    public void deveFecharCaixaComSucesso() throws Exception {
        UUID caixaId = UUID.randomUUID();
        FecharCaixaRequest request = new FecharCaixaRequest(new BigDecimal("150.00"));
        CaixaResponse response = CaixaResponse.builder()
                .id(caixaId)
                .status("FECHADO")
                .valorFechamentoFisico(new BigDecimal("150.00"))
                .build();

        when(caixaService.fecharCaixa(eq(caixaId), any(FecharCaixaRequest.class), any(Usuario.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/caixas/{id}/fechar", caixaId)
                        .with(csrf())
                        .with(user(mockUsuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FECHADO"));
    }

    @Test
    public void deveRegistrarSangriaComSucesso() throws Exception {
        UUID caixaId = UUID.randomUUID();
        LancamentoManualCaixaRequest request = new LancamentoManualCaixaRequest(new BigDecimal("50.00"), "Sangria para troco", null);
        LancamentoCaixaResponse response = LancamentoCaixaResponse.builder()
                .id(UUID.randomUUID())
                .valor(new BigDecimal("50.00"))
                .descricao("Sangria para troco")
                .tipo("SAIDA")
                .build();

        when(caixaService.registrarSaidaManual(eq(caixaId), any(LancamentoManualCaixaRequest.class), any(Usuario.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/caixas/{id}/sangria", caixaId)
                        .with(csrf())
                        .with(user(mockUsuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("SAIDA"))
                .andExpect(jsonPath("$.valor").value(50.00));
    }
}
