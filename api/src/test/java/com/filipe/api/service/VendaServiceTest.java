package com.filipe.api.service;

import com.filipe.api.domain.caixa.Caixa;
import com.filipe.api.domain.caixa.CaixaRepository;
import com.filipe.api.domain.caixa.StatusCaixa;
import com.filipe.api.domain.produto.Produto;
import com.filipe.api.domain.produto.ProdutoRepository;
import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.domain.usuario.UsuarioRepository;
import com.filipe.api.domain.venda.*;
import com.filipe.api.dto.venda.VendaResponse;
import com.filipe.api.dto.venda.VendaStartRequest;
import com.filipe.api.mapper.venda.VendaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private CaixaRepository caixaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private VendaMapper vendaMapper;

    @InjectMocks
    private VendaService vendaService;

    private UUID operatorId;
    private UUID caixaId;

    @BeforeEach
    void setUp() {
        operatorId = UUID.randomUUID();
        caixaId = UUID.randomUUID();
    }

    @Test
    void shouldIniciarVendaWithSuccess() {
        // Arrange
        VendaStartRequest request = new VendaStartRequest(operatorId, caixaId, null);
        Usuario operator = Usuario.builder().id(operatorId).nome("Operator").build();
        Caixa caixa = Caixa.builder().id(caixaId).status(StatusCaixa.ABERTO).build();
        
        when(usuarioRepository.findById(operatorId)).thenReturn(Optional.of(operator));
        when(caixaRepository.findByIdAndStatus(caixaId, StatusCaixa.ABERTO)).thenReturn(Optional.of(caixa));
        when(vendaRepository.save(any(Venda.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        vendaService.iniciarVenda(request);

        // Assert
        verify(vendaRepository, times(1)).save(any(Venda.class));
    }

    @Test
    void shouldFailToFinalizarVendaWithoutItems() {
        // Arrange
        UUID vendaId = UUID.randomUUID();
        Venda venda = Venda.builder().id(vendaId).status(StatusVenda.EM_ANDAMENTO).build();
        when(vendaRepository.findById(vendaId)).thenReturn(Optional.of(venda));

        // Act & Assert
        assertThrows(com.filipe.api.exception.BusinessException.class, () -> 
            vendaService.finalizarVenda(vendaId, null));
    }
}
