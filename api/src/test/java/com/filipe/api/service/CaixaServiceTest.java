package com.filipe.api.service;

import com.filipe.api.domain.caixa.Caixa;
import com.filipe.api.domain.caixa.CaixaRepository;
import com.filipe.api.domain.caixa.LancamentoCaixa;
import com.filipe.api.domain.caixa.LancamentoCaixaRepository;
import com.filipe.api.domain.caixa.StatusCaixa;
import com.filipe.api.domain.caixa.TipoLancamentoCaixa;
import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.domain.venda.FormaPagamento;
import com.filipe.api.domain.venda.Pagamento;
import com.filipe.api.domain.venda.Venda;
import com.filipe.api.dto.caixa.AbrirCaixaRequest;
import com.filipe.api.dto.caixa.FecharCaixaRequest;
import com.filipe.api.dto.caixa.CaixaResponse;
import com.filipe.api.dto.caixa.LancamentoManualCaixaRequest;
import com.filipe.api.exception.BusinessException;
import com.filipe.api.mapper.caixa.CaixaMapper;
import com.filipe.api.shared.audit.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaixaServiceTest {

    @Mock
    private CaixaRepository caixaRepository;

    @Mock
    private LancamentoCaixaRepository lancamentoCaixaRepository;

    @Mock
    private CaixaMapper caixaMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CaixaService caixaService;

    private UUID caixaId;
    private Usuario operador;

    @BeforeEach
    void setUp() {
        caixaId = UUID.randomUUID();
        operador = Usuario.builder().id(UUID.randomUUID()).nome("Operador").build();

        when(caixaRepository.save(any(Caixa.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(lancamentoCaixaRepository.save(any(LancamentoCaixa.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(caixaMapper.toResponse(any(Caixa.class))).thenAnswer(invocation -> {
            Caixa caixa = invocation.getArgument(0);
            return new CaixaResponse(
                    caixa.getId(),
                    caixa.getOperador().getId(),
                    caixa.getOperador().getNome(),
                    caixa.getDataAbertura(),
                    caixa.getDataFechamento(),
                    caixa.getValorAbertura(),
                    caixa.getValorFechamentoSistema(),
                    caixa.getValorFechamentoFisico(),
                    caixa.getDiferenca(),
                    caixa.getStatus()
            );
        });
    }

    @Test
    void shouldThrowWhenOpeningCaixaAlreadyOpen() {
        when(caixaRepository.existsByOperadorIdAndStatus(operador.getId(), StatusCaixa.ABERTO)).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> caixaService.abrirCaixa(new AbrirCaixaRequest(new BigDecimal("100.00")), operador));
    }

    @Test
    void shouldCalculateDifferenceIgnoringNonCashInFecharCaixa() {
        Caixa caixa = Caixa.builder()
                .id(caixaId)
                .operador(operador)
                .status(StatusCaixa.ABERTO)
                .valorAbertura(new BigDecimal("100.00"))
                .valorFechamentoSistema(new BigDecimal("100.00"))
                .build();

        List<LancamentoCaixa> lancamentos = List.of(
                LancamentoCaixa.builder()
                        .caixa(caixa)
                        .tipo(TipoLancamentoCaixa.ENTRADA)
                        .formaPagamento(FormaPagamento.DINHEIRO)
                        .valor(new BigDecimal("50.00"))
                        .build(),
                LancamentoCaixa.builder()
                        .caixa(caixa)
                        .tipo(TipoLancamentoCaixa.ENTRADA)
                        .formaPagamento(FormaPagamento.PIX)
                        .valor(new BigDecimal("40.00"))
                        .build(),
                LancamentoCaixa.builder()
                        .caixa(caixa)
                        .tipo(TipoLancamentoCaixa.SAIDA)
                        .formaPagamento(FormaPagamento.DINHEIRO)
                        .valor(new BigDecimal("20.00"))
                        .build()
        );

        when(caixaRepository.findByIdAndStatus(caixaId, StatusCaixa.ABERTO)).thenReturn(Optional.of(caixa));
        when(lancamentoCaixaRepository.findByCaixaId(caixaId)).thenReturn(lancamentos);

        CaixaResponse response = caixaService.fecharCaixa(caixaId, new FecharCaixaRequest(new BigDecimal("135.00")), operador);

        assertEquals(new BigDecimal("130.00"), caixa.getValorFechamentoSistema());
        assertEquals(new BigDecimal("5.00"), caixa.getDiferenca());
        assertEquals(StatusCaixa.FECHADO, caixa.getStatus());
        assertEquals(new BigDecimal("135.00"), response.valorFechamentoFisico());
    }

    @Test
    void shouldThrowWhenRegistrarSaidaManualAmountExceedsSaldo() {
        Caixa caixa = Caixa.builder()
                .id(caixaId)
                .operador(operador)
                .status(StatusCaixa.ABERTO)
                .valorAbertura(new BigDecimal("100.00"))
                .valorFechamentoSistema(new BigDecimal("50.00"))
                .build();

        when(caixaRepository.findByIdWithLock(caixaId)).thenReturn(Optional.of(caixa));

        LancamentoManualCaixaRequest request = new LancamentoManualCaixaRequest(
                new BigDecimal("60.00"),
                "Saida acima do saldo",
                null
        );

        assertThrows(BusinessException.class,
                () -> caixaService.registrarSaidaManual(caixaId, request, operador));
    }

    @Test
    void shouldIncrementValorFechamentoSistemaWhenRegisteringManualEntry() {
        Caixa caixa = Caixa.builder()
                .id(caixaId)
                .operador(operador)
                .status(StatusCaixa.ABERTO)
                .valorAbertura(new BigDecimal("100.00"))
                .valorFechamentoSistema(new BigDecimal("100.00"))
                .build();

        when(caixaRepository.findByIdWithLock(caixaId)).thenReturn(Optional.of(caixa));

        LancamentoManualCaixaRequest request = new LancamentoManualCaixaRequest(
                new BigDecimal("25.00"),
                "Deposito de teste",
                null
        );

        caixaService.registrarEntradaManual(caixaId, request, operador);

        assertEquals(new BigDecimal("125.00"), caixa.getValorFechamentoSistema());
    }

    @Test
    void shouldDecrementValorFechamentoSistemaWhenRegisteringManualExit() {
        Caixa caixa = Caixa.builder()
                .id(caixaId)
                .operador(operador)
                .status(StatusCaixa.ABERTO)
                .valorAbertura(new BigDecimal("100.00"))
                .valorFechamentoSistema(new BigDecimal("100.00"))
                .build();

        when(caixaRepository.findByIdWithLock(caixaId)).thenReturn(Optional.of(caixa));

        LancamentoManualCaixaRequest request = new LancamentoManualCaixaRequest(
                new BigDecimal("30.00"),
                "Retirada de teste",
                null
        );

        caixaService.registrarSaidaManual(caixaId, request, operador);

        assertEquals(new BigDecimal("70.00"), caixa.getValorFechamentoSistema());
    }

    @Test
    void shouldApplyCashPaymentTrocoAndIgnoreNonCashPaymentInAutomaticEntry() {
        Caixa caixa = Caixa.builder()
                .id(caixaId)
                .status(StatusCaixa.ABERTO)
                .valorAbertura(new BigDecimal("100.00"))
                .valorFechamentoSistema(new BigDecimal("100.00"))
                .build();

        when(caixaRepository.findByIdWithLock(caixaId)).thenReturn(Optional.of(caixa));

        Venda venda = Venda.builder().id(UUID.randomUUID()).numero(123L).caixa(caixa).build();
        Pagamento cashPayment = Pagamento.builder()
                .formaPagamento(FormaPagamento.DINHEIRO)
                .valor(new BigDecimal("100.00"))
                .troco(new BigDecimal("10.00"))
                .build();
        Pagamento pixPayment = Pagamento.builder()
                .formaPagamento(FormaPagamento.PIX)
                .valor(new BigDecimal("50.00"))
                .build();

        caixaService.registrarEntradaAutomaticaVenda(venda, List.of(cashPayment, pixPayment), operador);

        assertEquals(new BigDecimal("190.00"), caixa.getValorFechamentoSistema());
    }
}