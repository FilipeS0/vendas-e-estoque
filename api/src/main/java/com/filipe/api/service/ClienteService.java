package com.filipe.api.service;

import com.filipe.api.domain.cliente.Cliente;
import com.filipe.api.domain.cliente.ClienteRepository;
import com.filipe.api.domain.venda.Crediario;
import com.filipe.api.domain.venda.CrediarioRepository;
import com.filipe.api.domain.venda.ParcelaCrediario;
import com.filipe.api.domain.venda.Venda;
import com.filipe.api.domain.venda.VendaRepository;
import com.filipe.api.domain.venda.StatusVenda;
import com.filipe.api.dto.cliente.ClienteRequest;
import com.filipe.api.dto.cliente.ClienteResponse;
import com.filipe.api.dto.cliente.ExtratoClienteItem;
import com.filipe.api.exception.BusinessException;
import com.filipe.api.mapper.cliente.ClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;
    private final CrediarioRepository crediarioRepository;
    private final ClienteMapper clienteMapper;

    public ClienteResponse buscarPorId(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente nao encontrado."));
        return clienteMapper.toResponse(cliente);
    }

    public Page<ClienteResponse> listarClientes(String nome, Pageable pageable) {
        if (nome != null && !nome.trim().isEmpty()) {
            return clienteRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome.trim(), pageable)
                    .map(clienteMapper::toResponse);
        }
        return clienteRepository.findByAtivoTrue(pageable)
                .map(clienteMapper::toResponse);
    }

    @Transactional
    public ClienteResponse registrarCliente(ClienteRequest request) {
        if (request.cpf() != null && !request.cpf().trim().isEmpty() 
            && clienteRepository.findByCpf(request.cpf()).isPresent()) {
            throw new BusinessException("Ja existe um cliente cadastrado com este CPF.");
        }

        Cliente cliente = clienteMapper.toEntity(request);
        Cliente savedCliente = clienteRepository.save(cliente);
        return clienteMapper.toResponse(savedCliente);
    }

    @Transactional
    public ClienteResponse atualizarCliente(UUID id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente nao encontrado."));

        if (request.cpf() != null && !request.cpf().trim().isEmpty()) {
            clienteRepository.findByCpf(request.cpf())
                    .ifPresent(c -> {
                        if (!c.getId().equals(id)) {
                            throw new BusinessException("Ja existe outro cliente cadastrado com este CPF.");
                        }
                    });
        }

        clienteMapper.updateEntity(cliente, request);
        Cliente savedCliente = clienteRepository.save(cliente);
        return clienteMapper.toResponse(savedCliente);
    }

    @Transactional
    public void inativarCliente(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente nao encontrado."));
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<ExtratoClienteItem> gerarExtrato(UUID clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new BusinessException("Cliente nao encontrado."));

        List<ExtratoClienteItem> items = new ArrayList<>();
        BigDecimal saldoAcumulado = BigDecimal.ZERO;

        // 1. Get all Sales (Debit)
        List<Venda> vendas = vendaRepository.findByClienteId(clienteId);
        for (Venda v : vendas) {
            if (v.getStatus() == StatusVenda.CANCELADA) continue;
            
            // For the statement, we only show the portion that went to Crediario
            BigDecimal valorCrediario = v.getPagamentos().stream()
                    .filter(p -> p.getFormaPagamento().name().equals("CREDIARIO"))
                    .map(p -> p.getValor())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (valorCrediario.compareTo(BigDecimal.ZERO) > 0) {
                items.add(new ExtratoClienteItem(
                    v.getDataHora(),
                    "VENDA",
                    "Compra a Prazo - Venda #" + v.getNumero(),
                    valorCrediario.negate(),
                    null, // Will calculate below
                    v.getId()
                ));
            }
        }

        // 2. Get all Payments (Credit)
        List<Crediario> crediarios = crediarioRepository.findByClienteId(clienteId);
        for (Crediario c : crediarios) {
            for (ParcelaCrediario p : c.getParcelas()) {
                if (p.getValorPago().compareTo(BigDecimal.ZERO) > 0) {
                    items.add(new ExtratoClienteItem(
                        p.getDataPagamento() != null ? p.getDataPagamento().atStartOfDay() : c.getCreatedAt(),
                        "PAGAMENTO",
                        "Pagamento Parcela " + p.getNumeroParcela() + " - Venda #" + c.getVenda().getNumero(),
                        p.getValorPago(),
                        null,
                        p.getId()
                    ));
                }
            }
        }

        // 3. Sort by Date
        items.sort(Comparator.comparing(ExtratoClienteItem::data));

        // 4. Calculate Running Balance
        List<ExtratoClienteItem> extratoFinal = new ArrayList<>();
        BigDecimal runningBalance = BigDecimal.ZERO;
        for (ExtratoClienteItem item : items) {
            runningBalance = runningBalance.add(item.valor());
            extratoFinal.add(new ExtratoClienteItem(
                item.data(),
                item.tipo(),
                item.descricao(),
                item.valor(),
                runningBalance,
                item.referenciaId()
            ));
        }

        return extratoFinal;
    }
}
