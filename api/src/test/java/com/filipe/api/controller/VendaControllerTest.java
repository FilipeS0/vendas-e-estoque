package com.filipe.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.domain.venda.FormaPagamento;
import com.filipe.api.dto.venda.*;
import com.filipe.api.service.NotaFiscalService;
import com.filipe.api.service.VendaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VendaController.class)
public class VendaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private VendaService vendaService;

    @MockitoBean
    private NotaFiscalService notaFiscalService;

    @MockitoBean
    private com.filipe.api.domain.usuario.UsuarioRepository usuarioRepository;

    private Usuario mockUsuario;
    private UUID vendaId;

    @BeforeEach
    void setUp() {
        mockUsuario = Usuario.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .nome("Test User")
                .build();
        vendaId = UUID.randomUUID();
    }

    @Test
    public void deveIniciarVendaComSucesso() throws Exception {
        VendaStartRequest request = new VendaStartRequest(UUID.randomUUID(), null);
        VendaResponse response = new VendaResponse(
                vendaId, null, null, null, null, null,
                com.filipe.api.domain.venda.StatusVenda.EM_ANDAMENTO,
                List.of(), List.of(), null, null, null
        );

        when(vendaService.iniciarVenda(any(VendaStartRequest.class), any(Usuario.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/vendas")
                        .with(csrf())
                        .with(user(mockUsuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(vendaId.toString()))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    public void deveAdicionarItemComSucesso() throws Exception {
        ItemVendaRequest request = new ItemVendaRequest(UUID.randomUUID(), new BigDecimal("2.00"), BigDecimal.ZERO);
        VendaResponse response = new VendaResponse(
                vendaId, null, null, null, null, new BigDecimal("40.00"),
                null, List.of(), List.of(), null, null, null
        );

        when(vendaService.adicionarItem(eq(vendaId), any(ItemVendaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/vendas/{id}/itens", vendaId)
                        .with(csrf())
                        .with(user(mockUsuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(40.00));
    }

    @Test
    public void deveFinalizarVendaComSucesso() throws Exception {
        FinalizarVendaRequest request = new FinalizarVendaRequest(
                List.of(new PagamentoRequest(FormaPagamento.DINHEIRO, new BigDecimal("40.00"), null, null, null))
        );
        VendaResponse response = new VendaResponse(
                vendaId, null, null, null, null, null,
                com.filipe.api.domain.venda.StatusVenda.CONFIRMADA,
                List.of(), List.of(), null, null, null
        );

        when(vendaService.finalizarVenda(eq(vendaId), any(FinalizarVendaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/vendas/{id}/finalizar", vendaId)
                        .with(csrf())
                        .with(user(mockUsuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }
}
