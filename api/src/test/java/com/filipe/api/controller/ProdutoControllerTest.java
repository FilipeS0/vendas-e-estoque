package com.filipe.api.controller;

import com.filipe.api.dto.produto.ProdutoRequest;
import com.filipe.api.dto.produto.ProdutoResponse;
import com.filipe.api.domain.usuario.UsuarioRepository;
import com.filipe.api.security.SecurityConfig;
import com.filipe.api.service.ProdutoService;
import com.filipe.api.domain.produto.UnidadeMedida;
import com.filipe.api.domain.produto.OrigemProduto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(ProdutoController.class)
@Import(SecurityConfig.class)
public class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProdutoService produtoService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deveListarProdutosComSucesso() throws Exception {
        ProdutoResponse response = new ProdutoResponse(
                UUID.randomUUID(), "001", null, "Cerveja Teste", null, null,
                new BigDecimal("10.00"), null, null, null, true
        );

        when(produtoService.listarProdutos(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/produtos")
                        .param("nome", "Cerveja")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Cerveja Teste"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deveInativarProdutoComSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        
        mockMvc.perform(delete("/api/v1/produtos/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    public void deveNegarInativacaoParaOperador() throws Exception {
        UUID id = UUID.randomUUID();
        
        mockMvc.perform(delete("/api/v1/produtos/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deveCriarProdutoComSucesso() throws Exception {
        ProdutoRequest request = new ProdutoRequest(
                "P001", "7891234567890", "Produto Teste", "Desc",
                UnidadeMedida.UN, UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("10.00"), new BigDecimal("20.00"),
                "12345678", null, "5102", OrigemProduto.NACIONAL,
                null, null, null, null, null, null, BigDecimal.ZERO
        );

        ProdutoResponse response = new ProdutoResponse(
                UUID.randomUUID(), "P001", "7891234567890", "Produto Teste", null, null,
                new BigDecimal("20.00"), null, UnidadeMedida.UN.name(), null, true
        );

        when(produtoService.registrarProduto(any(ProdutoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/produtos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Produto Teste"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deveRetornarBadRequestAoCriarProdutoInvalido() throws Exception {
        ProdutoRequest request = new ProdutoRequest(
                "", "", "", null,
                null, null, null,
                null, null,
                "", null, "", null,
                null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/produtos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
