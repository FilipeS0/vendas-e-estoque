package com.filipe.api.controller;

import com.filipe.api.dto.produto.ProdutoResponse;
import com.filipe.api.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
public class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoService produtoService;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deveListarProdutosComSucesso() throws Exception {
        ProdutoResponse response = ProdutoResponse.builder()
                .id(UUID.randomUUID())
                .nome("Cerveja Teste")
                .codigoInterno("001")
                .precoVenda(new BigDecimal("10.00"))
                .ativo(true)
                .build();

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
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    public void deveNegarInativacaoParaOperador() throws Exception {
        UUID id = UUID.randomUUID();
        
        mockMvc.perform(delete("/api/v1/produtos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
