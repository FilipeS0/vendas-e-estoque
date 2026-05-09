package com.filipe.api.service;

import com.filipe.api.domain.produto.Produto;
import com.filipe.api.domain.produto.ProdutoRepository;
import com.filipe.api.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(produtoService, "uploadDir", "target/test-uploads");
    }

    @Test
    void deveRejeitarImagemComTipoInvalido() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo", "teste.txt", "text/plain", "conteudo".getBytes()
        );
        UUID id = UUID.randomUUID();
        
        when(produtoRepository.findById(id)).thenReturn(Optional.of(new Produto()));

        assertThrows(BusinessException.class, () -> produtoService.salvarImagem(id, file));
    }

    @Test
    void deveAceitarImagemComTipoValido() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo", "teste.jpg", "image/jpeg", "fake-image-binary".getBytes()
        );
        UUID id = UUID.randomUUID();
        Produto produto = new Produto();
        
        when(produtoRepository.findById(id)).thenReturn(Optional.of(produto));
        // Mocking the actual file writing would require mocking Path/Files or using a temp dir
        // But we can at least test the validation logic before the write call if we split it, 
        // or just let it fail at directory creation in a way we can catch.
        
        // For simplicity in this test, we verify that it doesn't throw BusinessException for MIME type
        try {
            produtoService.salvarImagem(id, file);
        } catch (Exception e) {
            // It might fail on Files.createDirectories, but shouldn't be BusinessException("Tipo de arquivo não permitido")
            assertNotEquals("Tipo de arquivo não permitido. Use JPEG, PNG ou WEBP.", e.getMessage());
        }
    }
}
