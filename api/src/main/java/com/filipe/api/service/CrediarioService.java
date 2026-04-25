package com.filipe.api.service;

import com.filipe.api.domain.caixa.Caixa;
import com.filipe.api.domain.caixa.CaixaRepository;
import com.filipe.api.domain.caixa.StatusCaixa;
import com.filipe.api.domain.cliente.Cliente;
import com.filipe.api.domain.cliente.ClienteRepository;
import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.domain.venda.*;
import com.filipe.api.dto.crediario.CrediarioResponse;
import com.filipe.api.dto.crediario.LiquidarParcelaRequest;
import com.filipe.api.dto.crediario.ParcelaResponse;
import com.filipe.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrediarioService {

    private final CrediarioRepository crediarioRepository;
    private final ParcelaCrediarioRepository parcelaRepository;
    private final CaixaRepository caixaRepository;
    private final ClienteRepository clienteRepository;
    private final CaixaService caixaService;

    @Transactional(readOnly = true)
    public Page<ParcelaResponse> listarParcelas(UUID clienteId, StatusParcela status, Pageable pageable) {
        return parcelaRepository.findComFiltros(clienteId, status, pageable)
                .map(this::toParcelaResponse);
    }

    @Transactional
    public ParcelaResponse liquidarParcela(UUID parcelaId, LiquidarParcelaRequest request, Usuario operador) {
        ParcelaCrediario parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new BusinessException("Parcela nao encontrada."));

        if (parcela.getStatus() == StatusParcela.PAGO) {
            throw new BusinessException("Esta parcela ja esta paga.");
        }

        Caixa caixa = caixaRepository.findByIdAndStatus(request.caixaId(), StatusCaixa.ABERTO)
                .orElseThrow(() -> new BusinessException("Caixa aberto nao encontrado."));

        BigDecimal valorPago = request.valorPago();
        
        // Update Parcela
        parcela.setValorPago(parcela.getValorPago().add(valorPago));
        parcela.setDataPagamento(LocalDate.now());
        
        if (parcela.getValorPago().compareTo(parcela.getValor()) >= 0) {
            parcela.setStatus(StatusParcela.PAGO);
        } else {
            parcela.setStatus(StatusParcela.PAGO_PARCIAL);
        }
        
        parcelaRepository.save(parcela);

        // Update Crediario total status
        Crediario crediario = parcela.getCrediario();
        crediario.setValorPago(crediario.getValorPago().add(valorPago));
        
        boolean todasPagas = crediario.getParcelas().stream()
                .allMatch(p -> p.getStatus() == StatusParcela.PAGO);
        
        if (todasPagas) {
            crediario.setStatus(StatusCrediario.PAGO);
        } else {
            crediario.setStatus(StatusCrediario.PAGO_PARCIAL);
        }
        
        crediarioRepository.save(crediario);

        // Update Cliente balance
        Cliente cliente = crediario.getCliente();
        cliente.setSaldoDevedor(cliente.getSaldoDevedor().subtract(valorPago));
        if (cliente.getSaldoDevedor().compareTo(BigDecimal.ZERO) < 0) {
            cliente.setSaldoDevedor(BigDecimal.ZERO);
        }
        clienteRepository.save(cliente);

        // Register in Cashier
        caixaService.registrarEntradaManual(caixa.getId(), 
            new com.filipe.api.dto.caixa.LancamentoManualCaixaRequest(
                valorPago, 
                "Recebimento de Crediário - Cliente: " + cliente.getNome() + " (Parcela " + parcela.getNumeroParcela() + ")",
                parcela.getId()
            ), operador);

        return toParcelaResponse(parcela);
    }

    private ParcelaResponse toParcelaResponse(ParcelaCrediario p) {
        return new ParcelaResponse(
            p.getId(),
            p.getNumeroParcela(),
            p.getValor(),
            p.getDataVencimento(),
            p.getDataPagamento(),
            p.getValorPago(),
            p.getStatus()
        );
    }

    public CrediarioResponse toCrediarioResponse(Crediario c) {
        return new CrediarioResponse(
            c.getId(),
            c.getCliente().getId(),
            c.getCliente().getNome(),
            c.getVenda() != null ? c.getVenda().getId() : null,
            c.getVenda() != null ? c.getVenda().getNumero() : null,
            c.getValorTotal(),
            c.getValorPago(),
            c.getStatus(),
            c.getCreatedAt(),
            c.getParcelas().stream().map(this::toParcelaResponse).collect(Collectors.toList())
        );
    }
}
