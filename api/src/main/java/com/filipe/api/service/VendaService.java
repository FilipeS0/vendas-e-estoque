package com.filipe.api.service;

import com.filipe.api.domain.caixa.Caixa;
import com.filipe.api.domain.caixa.CaixaRepository;
import com.filipe.api.domain.caixa.StatusCaixa;
import com.filipe.api.domain.cliente.Cliente;
import com.filipe.api.domain.cliente.ClienteRepository;
import com.filipe.api.domain.estoque.TipoMovimentacaoEstoque;
import com.filipe.api.domain.produto.Produto;
import com.filipe.api.domain.produto.ProdutoRepository;
import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.domain.venda.FormaPagamento;
import com.filipe.api.domain.venda.ItemVenda;
import com.filipe.api.domain.venda.Pagamento;
import com.filipe.api.domain.venda.PagamentoRepository;
import com.filipe.api.domain.venda.StatusVenda;
import com.filipe.api.domain.venda.Venda;
import com.filipe.api.domain.venda.VendaRepository;
import com.filipe.api.domain.venda.Crediario;
import com.filipe.api.domain.venda.CrediarioRepository;
import com.filipe.api.domain.venda.ParcelaCrediario;
import com.filipe.api.domain.venda.StatusCrediario;
import com.filipe.api.domain.venda.StatusParcela;
import com.filipe.api.dto.venda.CancelarVendaRequest;
import com.filipe.api.dto.venda.FinalizarVendaRequest;
import com.filipe.api.dto.venda.ItemVendaRequest;
import com.filipe.api.dto.venda.PagamentoRequest;
import com.filipe.api.dto.venda.VendaResponse;
import com.filipe.api.dto.venda.VendaStartRequest;
import com.filipe.api.exception.BusinessException;
import com.filipe.api.mapper.venda.VendaMapper;
import com.filipe.api.shared.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final CaixaRepository caixaRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final CrediarioRepository crediarioRepository;
    private final VendaMapper vendaMapper;
    private final CaixaService caixaService;
    private final NotaFiscalService notaFiscalService;
    private final AuditService auditService;
    // Fix 2 & 3 — delegate stock mutations to EstoqueService which owns the
    // pessimistic lock and the canonical TipoMovimentacaoEstoque enum.
    private final EstoqueService estoqueService;

    @Transactional(readOnly = true)
    public VendaResponse buscarVenda(UUID vendaId) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new BusinessException("Venda nao encontrada."));
        return vendaMapper.toResponse(venda);
    }

    @Transactional(readOnly = true)
    public Page<VendaResponse> listarVendas(
            StatusVenda status,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Long numero,
            UUID caixaId,
            Pageable pageable) {
        return vendaRepository.findComFiltros(status, dataInicio, dataFim, numero, caixaId, pageable)
                .map(vendaMapper::toResponse);
    }

    /**
     * Fix 4 — the operator is now the authenticated user passed in by the
     * controller (extracted from the JWT).  The request body no longer accepts
     * operadorId; keeping caixaId in the body is fine because one user may
     * have several open caixas in multi-register setups (v2.0 prep).
     */
    @Transactional
    public VendaResponse iniciarVenda(VendaStartRequest request, Usuario operador) {
        if (request == null) {
            throw new BusinessException("Os dados de abertura da venda sao obrigatorios.");
        }
        if (operador == null) {
            throw new BusinessException("Usuario autenticado e obrigatorio para iniciar uma venda.");
        }

        Caixa caixa = caixaRepository.findByIdAndStatus(request.caixaId(), StatusCaixa.ABERTO)
                .orElseThrow(() -> new BusinessException("Caixa aberto nao encontrado."));

        Cliente cliente = null;
        if (request.clienteId() != null) {
            cliente = clienteRepository.findById(request.clienteId())
                    .orElseThrow(() -> new BusinessException("Cliente nao encontrado."));
        }

        Venda venda = Venda.builder()
                .operador(operador)
                .caixa(caixa)
                .cliente(cliente)
                .dataHora(LocalDateTime.now())
                .valorBruto(BigDecimal.ZERO)
                .valorDesconto(BigDecimal.ZERO)
                .valorTotal(BigDecimal.ZERO)
                .status(StatusVenda.EM_ANDAMENTO)
                .build();

        return vendaMapper.toResponse(vendaRepository.save(venda));
    }

    @Transactional
    public VendaResponse adicionarItem(UUID vendaId, ItemVendaRequest request) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new BusinessException("Venda nao encontrada."));

        if (venda.getStatus() != StatusVenda.EM_ANDAMENTO) {
            throw new BusinessException("Nao e possivel adicionar itens a uma venda que nao esta em andamento.");
        }

        Produto produto = produtoRepository.findById(request.produtoId())
                .orElseThrow(() -> new BusinessException("Produto nao encontrado."));

        if (produto.getAtivo() == null || !produto.getAtivo()) {
            throw new BusinessException("Nao e possivel adicionar um produto inativo.");
        }

        BigDecimal quantidade    = request.quantidade();
        BigDecimal precoUnitario = produto.getPrecoVenda();
        BigDecimal desconto      = request.desconto() != null ? request.desconto() : BigDecimal.ZERO;

        if (desconto.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("O desconto do item nao pode ser negativo.");
        }

        BigDecimal valorTotalItem = precoUnitario.multiply(quantidade).subtract(desconto);
        if (valorTotalItem.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("O desconto do item nao pode ser maior que o valor do item.");
        }

        ItemVenda itemVenda = ItemVenda.builder()
                .venda(venda)
                .produto(produto)
                .quantidade(quantidade)
                .precoUnitario(precoUnitario)
                .desconto(desconto)
                .valorTotal(valorTotalItem)
                .build();

        venda.adicionarItem(itemVenda);
        venda.recalcularTotais();
        vendaRepository.save(venda);
        return vendaMapper.toResponse(venda);
    }

    @Transactional
    public VendaResponse aplicarDescontoVenda(UUID vendaId, BigDecimal desconto) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new BusinessException("Venda nao encontrada."));

        if (venda.getStatus() != StatusVenda.EM_ANDAMENTO) {
            throw new BusinessException("Nao e possivel aplicar desconto a uma venda que nao esta em andamento.");
        }

        if (desconto == null) {
            throw new BusinessException("O desconto da venda e obrigatório.");
        }
        if (desconto.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("O desconto da venda nao pode ser negativo.");
        }
        if (desconto.compareTo(venda.getValorBruto()) > 0) {
            throw new BusinessException("O desconto da venda nao pode ser maior que o valor bruto da venda.");
        }

        venda.setValorDescontoVenda(desconto);
        venda.recalcularTotais();
        vendaRepository.save(venda);
        return vendaMapper.toResponse(venda);
    }

    @Transactional
    public VendaResponse removerItem(UUID vendaId, UUID itemId) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new BusinessException("Venda nao encontrada."));

        if (venda.getStatus() != StatusVenda.EM_ANDAMENTO) {
            throw new BusinessException("Nao e possivel remover itens de uma venda que nao esta em andamento.");
        }

        ItemVenda itemRemover = venda.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Item nao encontrado na venda."));

        venda.removerItem(itemRemover);
        venda.recalcularTotais();
        vendaRepository.save(venda);
        return vendaMapper.toResponse(venda);
    }

    @Transactional
    public VendaResponse finalizarVenda(UUID vendaId, FinalizarVendaRequest request) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new BusinessException("Venda nao encontrada."));

        if (venda.getStatus() != StatusVenda.EM_ANDAMENTO) {
            throw new BusinessException("Somente vendas em andamento podem ser finalizadas.");
        }
        if (venda.getItens().isEmpty()) {
            throw new BusinessException("Nao e possivel finalizar uma venda sem itens.");
        }
        if (venda.getCaixa() == null) {
            throw new BusinessException("Nao e possivel finalizar uma venda sem caixa vinculado.");
        }

        // 1. Validate total paid >= sale total
        BigDecimal totalPago = request.pagamentos().stream()
                .map(PagamentoRequest::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPago.compareTo(venda.getValorTotal()) < 0) {
            throw new BusinessException(
                    "Valor pago insuficiente. Total da venda: " + venda.getValorTotal()
                    + ", Total informado: " + totalPago);
        }

        BigDecimal trocoTotal = totalPago.subtract(venda.getValorTotal());
        if (trocoTotal.compareTo(BigDecimal.ZERO) > 0
                && request.pagamentos().stream().noneMatch(p -> p.formaPagamento() == FormaPagamento.DINHEIRO)) {
            throw new BusinessException("Troco so pode ser aplicado quando houver pagamento em dinheiro.");
        }

        BigDecimal totalPagoEmDinheiro = request.pagamentos().stream()
                .filter(p -> p.formaPagamento() == FormaPagamento.DINHEIRO)
                .map(PagamentoRequest::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (trocoTotal.compareTo(totalPagoEmDinheiro) > 0) {
            throw new BusinessException("O troco nao pode ser maior que o valor total recebido em dinheiro.");
        }

        // 2. Build and save Pagamento records
        List<BigDecimal> trocosPorPagamento = distribuirTroco(request.pagamentos(), trocoTotal);

        for (int i = 0; i < request.pagamentos().size(); i++) {
            PagamentoRequest pagReq  = request.pagamentos().get(i);
            BigDecimal       troco   = trocosPorPagamento.get(i);

            Pagamento pagamento = Pagamento.builder()
                    .venda(venda)
                    .formaPagamento(pagReq.formaPagamento())
                    .valor(pagReq.valor())
                    .troco(troco)
                    .nsu(pagReq.nsu())
                    .autorizacao(pagReq.autorizacao())
                    .build();

            pagamentoRepository.save(pagamento);
            venda.getPagamentos().add(pagamento);

            if (pagReq.formaPagamento() == FormaPagamento.CREDIARIO) {
                processarCrediario(venda, pagReq.valor(), pagReq.numeroParcelas());
            }
        }

        // 3. Deduct stock — Fix 2: lock acquired inside moverEstoque
        //                  Fix 3: uses SAIDA_VENDA enum value
        for (ItemVenda item : venda.getItens()) {
            estoqueService.moverEstoque(
                    item.getProduto(),
                    TipoMovimentacaoEstoque.SAIDA_VENDA,
                    item.getQuantidade(),
                    "Venda confirmada",
                    venda.getId().toString(),
                    venda.getOperador()
            );
        }

        // 4. Confirm sale and register automatic cash entry
        venda.setStatus(StatusVenda.CONFIRMADA);
        vendaRepository.save(venda);
        caixaService.registrarEntradaAutomaticaVenda(venda, venda.getPagamentos(), venda.getOperador());
        
        // Automate NFC-e emission
        try {
            notaFiscalService.emitirNotaFiscal(venda.getId());
        } catch (Exception e) {
            // Log but don't fail the sale finalization
            System.err.println("Erro ao gerar nota fiscal automatica: " + e.getMessage());
        }

        return vendaMapper.toResponse(venda);
    }

    @Transactional
    public VendaResponse cancelarVenda(UUID vendaId, CancelarVendaRequest request) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new BusinessException("Venda nao encontrada."));

        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new BusinessException("A venda ja esta cancelada.");
        }

        String motivo = request.motivo().trim();
        if (motivo.isEmpty()) {
            throw new BusinessException("O motivo do cancelamento e obrigatorio.");
        }

        if (venda.getStatus() == StatusVenda.CONFIRMADA) {
            reverterEstoque(venda, motivo);
            caixaService.registrarEstornoVenda(venda, venda.getPagamentos(), venda.getOperador(), motivo);
            notaFiscalService.cancelarNotaFiscal(venda.getId(), motivo);
        }

        auditService.log(
                "CANCELAMENTO_VENDA",
                "Venda",
                venda.getId(),
                venda.getOperador(),
                "motivo=" + motivo
        );

        venda.setStatus(StatusVenda.CANCELADA);
        return vendaMapper.toResponse(vendaRepository.save(venda));
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private void reverterEstoque(Venda venda, String motivo) {
        for (ItemVenda item : venda.getItens()) {
            estoqueService.moverEstoque(
                    item.getProduto(),
                    TipoMovimentacaoEstoque.ENTRADA_DEVOLUCAO,
                    item.getQuantidade(),
                    "Cancelamento de venda: " + motivo,
                    venda.getId().toString(),
                    venda.getOperador()
            );
        }
    }

    private List<BigDecimal> distribuirTroco(List<PagamentoRequest> pagamentos, BigDecimal trocoTotal) {
        List<BigDecimal> trocos = new ArrayList<>();
        for (int i = 0; i < pagamentos.size(); i++) {
            trocos.add(BigDecimal.ZERO);
        }

        BigDecimal trocoRestante = trocoTotal;
        for (int i = pagamentos.size() - 1; i >= 0 && trocoRestante.compareTo(BigDecimal.ZERO) > 0; i--) {
            PagamentoRequest pagamento = pagamentos.get(i);
            if (pagamento.formaPagamento() != FormaPagamento.DINHEIRO) continue;

            BigDecimal trocoPagamento = pagamento.valor().min(trocoRestante);
            trocos.set(i, trocoPagamento);
            trocoRestante = trocoRestante.subtract(trocoPagamento);
        }

        if (trocoRestante.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Nao foi possivel distribuir o troco entre os pagamentos em dinheiro.");
        }

        return trocos;
    }

    private void processarCrediario(Venda venda, BigDecimal valor, Integer numeroParcelas) {
        Cliente cliente = venda.getCliente();
        if (cliente == null) {
            throw new BusinessException("Pagamento em CREDIARIO requer um cliente identificado.");
        }

        BigDecimal novoSaldoDevedor = cliente.getSaldoDevedor().add(valor);
        if (cliente.getLimiteCredito().compareTo(BigDecimal.ZERO) > 0 
            && novoSaldoDevedor.compareTo(cliente.getLimiteCredito()) > 0) {
            throw new BusinessException("Limite de credito excedido para o cliente " + cliente.getNome());
        }

        cliente.setSaldoDevedor(novoSaldoDevedor);
        clienteRepository.save(cliente);

        Crediario crediario = Crediario.builder()
                .cliente(cliente)
                .venda(venda)
                .valorTotal(valor)
                .valorPago(BigDecimal.ZERO)
                .status(StatusCrediario.ABERTO)
                .parcelas(new ArrayList<>())
                .build();

        int numParcelas = (numeroParcelas != null && numeroParcelas > 0) ? numeroParcelas : 1;
        BigDecimal valorParcela = valor.divide(BigDecimal.valueOf(numParcelas), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal resto = valor.subtract(valorParcela.multiply(BigDecimal.valueOf(numParcelas)));

        for (int i = 1; i <= numParcelas; i++) {
            BigDecimal valorFinalParcela = (i == numParcelas) ? valorParcela.add(resto) : valorParcela;
            
            ParcelaCrediario parcela = ParcelaCrediario.builder()
                    .crediario(crediario)
                    .numeroParcela(i)
                    .valor(valorFinalParcela)
                    .dataVencimento(java.time.LocalDate.now().plusMonths(i))
                    .status(StatusParcela.PENDENTE)
                    .build();

            crediario.getParcelas().add(parcela);
        }
        
        crediarioRepository.save(crediario);
    }
}