package com.filipe.api.shared.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoServiceTest {

    private CryptoService cryptoService;
    private static final String SECRET = "0123456789abcdef0123456789abcdef"; // 32 chars for 256 bits

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService(SECRET);
    }

    @Test
    void deveCriptografarEDescriptografarComSucesso() {
        String original = "SenhaSuperSecreta123!";
        
        String criptografado = cryptoService.encrypt(original);
        assertNotNull(criptografado);
        assertNotEquals(original, criptografado);
        
        String descriptografado = cryptoService.decrypt(criptografado);
        assertEquals(original, descriptografado);
    }

    @Test
    void deveGerarCifrasDiferentesParaMesmoTexto() {
        String original = "mesma_senha";
        
        String cifrado1 = cryptoService.encrypt(original);
        String cifrado2 = cryptoService.encrypt(original);
        
        assertNotEquals(cifrado1, cifrado2, "O IV randômico deve gerar cifras diferentes");
        assertEquals(cryptoService.decrypt(cifrado1), cryptoService.decrypt(cifrado2));
    }

    @Test
    void deveFalharAoDescriptografarComChaveErrada() {
        String original = "teste";
        String cifrado = cryptoService.encrypt(original);
        
        CryptoService serviceComOutraChave = new CryptoService("outra_chave_de_32_caracteres_total");
        
        assertThrows(RuntimeException.class, () -> serviceComOutraChave.decrypt(cifrado));
    }
}
