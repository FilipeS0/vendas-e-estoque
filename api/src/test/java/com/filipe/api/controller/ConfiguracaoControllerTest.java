package com.filipe.api.controller;

import com.filipe.api.dto.configuracao.ConfiguracaoResponse;
import com.filipe.api.service.ConfiguracaoService;
import com.filipe.api.domain.usuario.UsuarioRepository;
import com.filipe.api.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfiguracaoController.class)
@Import(SecurityConfig.class)
public class ConfiguracaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConfiguracaoService configuracaoService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void devePermitirUploadCertificadoParaAdmin() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cert.pfx", "application/x-pkcs12", "test content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/configuracoes/certificado")
                        .file(file)
                        .param("senha", "123456")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    public void deveNegarUploadCertificadoParaGerente() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cert.pfx", "application/x-pkcs12", "test content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/configuracoes/certificado")
                        .file(file)
                        .param("senha", "123456")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    public void deveNegarBuscaConfiguracaoParaOperador() throws Exception {
        mockMvc.perform(get("/api/v1/configuracoes"))
                .andExpect(status().isForbidden());
    }
}
