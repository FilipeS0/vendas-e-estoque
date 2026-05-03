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
import com.filipe.api.dto.caixa.CaixaResponse;
import com.filipe.api.dto.caixa.FecharCaixaRequest;
import com.filipe.api.dto.caixa.LancamentoManualCaixaRequest;
import com.filipe.api.dto.caixa.LancamentoCaixaResponse;

import com.filipe.api.exception.BusinessException;
import com.filipe.api.mapper.caixa.CaixaMapper;
import com.filipe.api.shared.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaixaService {

    private final CaixaRepository caixaRepository;
    private final LancamentoCaixaRepository lancamentoCaixaRepository;
    private final CaixaMapper caixaMapper;
    private final AuditService auditService;

    @Transactional
    public CaixaResponse abrirCaixa(AbrirCaixaRequest request, Usuario usuario) {
        if (usuario == null) {
            throw new BusinessException("Usuario autenticado nao encontrado para abrir o caixa.");
        }

        if (caixaRepository.existsByOperadorIdAndStatus(usuario.getId(), StatusCaixa.ABERTO)) {
            throw new BusinessException("Ja existe um caixa aberto para este operador.");
        }

        Caixa caixa = Caixa.builder()
                .operador(usuario)
                .valorAbertura(request.valorAbertura())
                .valorFechamentoSistema(request.valorAbertura())
                .status(StatusCaixa.ABERTO)
                .diferenca(BigDecimal.ZERO)
                .build();

        Caixa savedCaixa = caixaRepository.save(caixa);
        return caixaMapper.toResponse(savedCaixa);
    }

    @Transactional(readOnly = true)
    public CaixaResponse buscarCaixa(UUID caixaId) {
        return buscarCaixa(caixaId, null);
    }

    @Transactional(readOnly = true)
    public CaixaResponse buscarCaixa(UUID caixaId, Usuario usuario) {
        Caixa caixa = caixaRepository.findById(caixaId)
                .orElseThrow(() -> new BusinessException("Caixa nao encontrado."));
        validarOperadorResponsavel(caixa, usuario);
        return caixaMapper.toResponse(caixa);
    }

    @Transactional(readOnly = true)
    public List<CaixaResponse> listarCaixas(Usuario usuario, StatusCaixa status, LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (usuario == null) {
            throw new BusinessException("Usuario autenticado nao encontrado.");
        }

        boolean isGerente = usuario.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_GERENTE"));

        List<Caixa> caixas = isGerente
                ? caixaRepository.findAll()
                : caixaRepository.findByOperadorId(usuario.getId());

        return caixas.stream()
                .filter(c -> status     == null || c.getStatus() == status)
                .filter(c -> dataInicio == null || !c.getDataAbertura().isBefore(dataInicio))
                .filter(c -> dataFim    == null || !c.getDataAbertura().isAfter(dataFim))
                .map(caixaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<LancamentoCaixaResponse> listarLancamentos(
            UUID caixaId,
            Usuario usuario,
            TipoLancamentoCaixa tipo,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Pageable pageable
    ) {
        Caixa caixa = caixaRepository.findById(caixaId)
                .orElseThrow(() -> new BusinessException("Caixa nao encontrado."));
        validarOperadorResponsavel(caixa, usuario);

        return lancamentoCaixaRepository.findComFiltros(caixaId, tipo, dataInicio, dataFim, pageable)
                .map(caixaMapper::toLancamentoResponse);
    }

    @Transactional
    public LancamentoCaixaResponse registrarEntradaManual(UUID caixaId, LancamentoManualCaixaRequest request, Usuario usuario) {
        return registrarLancamentoManual(caixaId, request, usuario, TipoLancamentoCaixa.ENTRADA);
    }

    @Transactional
    public LancamentoCaixaResponse registrarSaidaManual(UUID caixaId, LancamentoManualCaixaRequest request, Usuario usuario) {
        return registrarLancamentoManual(caixaId, request, usuario, TipoLancamentoCaixa.SAIDA);
    }

    @Transactional
    public CaixaResponse fecharCaixa(UUID caixaId, FecharCaixaRequest request, Usuario usuario) {
        Caixa caixa = caixaRepository.findByIdAndStatus(caixaId, StatusCaixa.ABERTO)
                .orElseThrow(() -> new BusinessException("Caixa aberto nao encontrado."));
        validarOperadorResponsavel(caixa, usuario);

        BigDecimal saldoEsperado = calcularSaldoEsperado(caixa.getId(), caixa.getValorAbertura());
        BigDecimal diferenca = request.valorFechamentoFisico().subtract(saldoEsperado);

        caixa.setValorFechamentoSistema(saldoEsperado);
        caixa.setValorFechamentoFisico(request.valorFechamentoFisico());
        caixa.setDiferenca(diferenca);
        caixa.setDataFechamento(LocalDateTime.now());
        caixa.setStatus(StatusCaixa.FECHADO);

        auditService.log(
                "FECHAMENTO_CAIXA",
                "Caixa",
                caixa.getId(),
                usuario,
                "diferenca=" + diferenca
        );

        Caixa savedCaixa = caixaRepository.save(caixa);
        return caixaMapper.toResponse(savedCaixa);
    }

    @Transactional
    public void registrarEntradaAutomaticaVenda(Venda venda, List<Pagamento> pagamentos, Usuario usuario) {
        if (venda.getCaixa() == null) {
            throw new BusinessException("Venda sem caixa vinculado. Nao e possivel registrar entrada automatica.");
        }

        Caixa caixa = caixaRepository.findByIdWithLock(venda.getCaixa().getId())
                .orElseThrow(() -> new BusinessException("Caixa aberto nao encontrado para the venda."));

        if (caixa.getStatus() != StatusCaixa.ABERTO) {
            throw new BusinessException("Caixa aberto nao encontrado para the venda.");
        }

        BigDecimal saldoAtual = caixa.getValorFechamentoSistema() != null
                ? caixa.getValorFechamentoSistema()
                : caixa.getValorAbertura();

        for (Pagamento pagamento : pagamentos) {
            BigDecimal valorLancamento = pagamento.getValor();
            if (pagamento.getFormaPagamento() == FormaPagamento.DINHEIRO && pagamento.getTroco() != null) {
                valorLancamento = valorLancamento.subtract(pagamento.getTroco());
            }

            if (valorLancamento.compareTo(BigDecimal.ZERO) <= 0 || pagamento.getFormaPagamento() == FormaPagamento.CREDIARIO) {
                continue;
            }

            LancamentoCaixa lancamento = LancamentoCaixa.builder()
                    .caixa(caixa)
                    .tipo(TipoLancamentoCaixa.ENTRADA)
                    .formaPagamento(pagamento.getFormaPagamento())
                    .valor(valorLancamento)
                    .descricao("Entrada automatica da venda " + (venda.getNumero() != null ? venda.getNumero() : venda.getId()))
                    .referenciaId(venda.getId())
                    .usuario(usuario)
                    .build();

            lancamentoCaixaRepository.save(lancamento);
            if (impactaSaldoFisico(pagamento.getFormaPagamento())) {
                saldoAtual = saldoAtual.add(valorLancamento);
            }
        }

        caixa.setValorFechamentoSistema(saldoAtual);
        caixaRepository.save(caixa);
    }

    @Transactional
    public void registrarEstornoVenda(Venda venda, List<Pagamento> pagamentos, Usuario usuario, String motivo) {
        if (venda.getCaixa() == null) {
            throw new BusinessException("Venda sem caixa vinculado. Nao e possivel registrar estorno no caixa.");
        }

        Caixa caixa = caixaRepository.findByIdWithLock(venda.getCaixa().getId())
                .orElseThrow(() -> new BusinessException("Caixa aberto nao encontrado para estornar a venda."));

        if (caixa.getStatus() != StatusCaixa.ABERTO) {
            throw new BusinessException("Caixa aberto nao encontrado para estornar a venda.");
        }

        if (!lancamentoCaixaRepository.findByCaixaIdAndReferenciaId(caixa.getId(), venda.getId()).isEmpty()) {
            BigDecimal saldoAtual = caixa.getValorFechamentoSistema() != null
                    ? caixa.getValorFechamentoSistema()
                    : caixa.getValorAbertura();

            for (Pagamento pagamento : pagamentos) {
                BigDecimal valorLancamento = pagamento.getValor();
                if (pagamento.getFormaPagamento() == FormaPagamento.DINHEIRO && pagamento.getTroco() != null) {
                    valorLancamento = valorLancamento.subtract(pagamento.getTroco());
                }

                if (valorLancamento.compareTo(BigDecimal.ZERO) <= 0 || pagamento.getFormaPagamento() == FormaPagamento.CREDIARIO) {
                    continue;
                }

                LancamentoCaixa lancamento = LancamentoCaixa.builder()
                        .caixa(caixa)
                        .tipo(TipoLancamentoCaixa.SAIDA)
                        .formaPagamento(pagamento.getFormaPagamento())
                        .valor(valorLancamento)
                        .descricao("Estorno da venda " + (venda.getNumero() != null ? venda.getNumero() : venda.getId()) + ": " + motivo)
                        .referenciaId(venda.getId())
                        .usuario(usuario)
                        .build();

                lancamentoCaixaRepository.save(lancamento);
                if (impactaSaldoFisico(pagamento.getFormaPagamento())) {
                    saldoAtual = saldoAtual.subtract(valorLancamento);
                }
            }

            caixa.setValorFechamentoSistema(saldoAtual);
            caixaRepository.save(caixa);
        }
    }

    private BigDecimal calcularSaldoEsperado(UUID caixaId, BigDecimal valorAbertura) {
        BigDecimal saldo = valorAbertura != null ? valorAbertura : BigDecimal.ZERO;

        for (LancamentoCaixa lancamento : lancamentoCaixaRepository.findByCaixaId(caixaId)) {
            boolean impactaSaldoFisico = lancamento.getFormaPagamento() == null
                    || lancamento.getFormaPagamento() == FormaPagamento.DINHEIRO;

            if (!impactaSaldoFisico) {
                continue;
            }

            if (lancamento.getTipo() == TipoLancamentoCaixa.ENTRADA) {
                saldo = saldo.add(lancamento.getValor());
            } else {
                saldo = saldo.subtract(lancamento.getValor());
            }
        }

        return saldo;
    }

    private boolean impactaSaldoFisico(FormaPagamento formaPagamento) {
        return formaPagamento == null || formaPagamento == FormaPagamento.DINHEIRO;
    }

    private LancamentoCaixaResponse registrarLancamentoManual(
            UUID caixaId,
            LancamentoManualCaixaRequest request,
            Usuario usuario,
            TipoLancamentoCaixa tipo
    ) {
        Caixa caixa = caixaRepository.findByIdWithLock(caixaId)
                .orElseThrow(() -> new BusinessException("Caixa aberto nao encontrado."));

        if (caixa.getStatus() != StatusCaixa.ABERTO) {
            throw new BusinessException("Caixa aberto nao encontrado.");
        }
        validarOperadorResponsavel(caixa, usuario);

        BigDecimal saldoAtual = caixa.getValorFechamentoSistema() != null
                ? caixa.getValorFechamentoSistema()
                : caixa.getValorAbertura();

        if (tipo == TipoLancamentoCaixa.SAIDA) {
            if (request.valor().compareTo(saldoAtual) > 0) {
                throw new BusinessException("Saldo insuficiente para registrar a saida manual no caixa.");
            }
        }

        LancamentoCaixa lancamento = LancamentoCaixa.builder()
                .caixa(caixa)
                .tipo(tipo)
                .valor(request.valor())
                .descricao(request.descricao().trim())
                .referenciaId(request.referenciaId())
                .usuario(usuario)
                .build();

        LancamentoCaixa savedLancamento = lancamentoCaixaRepository.save(lancamento);

        if (tipo == TipoLancamentoCaixa.ENTRADA) {
            saldoAtual = saldoAtual.add(request.valor());
        } else {
            saldoAtual = saldoAtual.subtract(request.valor());

            auditService.log(
                    "SANGRIA_CAIXA",
                    "Caixa",
                    caixa.getId(),
                    usuario,
                    "valor=" + request.valor() + " descricao=" + request.descricao().trim()
            );
        }

        caixa.setValorFechamentoSistema(saldoAtual);
        caixaRepository.save(caixa);

        return caixaMapper.toLancamentoResponse(savedLancamento);
    }

    private void validarOperadorResponsavel(Caixa caixa, Usuario usuario) {
        if (usuario == null) {
            throw new BusinessException("Usuario autenticado nao encontrado.");
        }

        if (caixa.getOperador() == null || !caixa.getOperador().getId().equals(usuario.getId())) {
            throw new BusinessException("Somente o operador dono do caixa pode executar esta operacao.");
        }
    }
}