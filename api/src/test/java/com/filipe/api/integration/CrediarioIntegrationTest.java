package com.filipe.api.integration;

import com.filipe.api.AbstractIntegrationTest;
import com.filipe.api.domain.caixa.Caixa;
import com.filipe.api.domain.caixa.CaixaRepository;
import com.filipe.api.domain.caixa.LancamentoCaixaRepository;
import com.filipe.api.domain.caixa.StatusCaixa;
import com.filipe.api.domain.cliente.Cliente;
import com.filipe.api.domain.cliente.ClienteRepository;
import com.filipe.api.domain.configuracao.Configuracao;
import com.filipe.api.domain.configuracao.ConfiguracaoRepository;
import com.filipe.api.domain.estoque.EstoqueAtual;
import com.filipe.api.domain.estoque.EstoqueAtualRepository;
import com.filipe.api.domain.produto.Categoria;
import com.filipe.api.domain.produto.CategoriaRepository;
import com.filipe.api.domain.produto.Fornecedor;
import com.filipe.api.domain.produto.FornecedorRepository;
import com.filipe.api.domain.produto.Produto;
import com.filipe.api.domain.produto.ProdutoRepository;
import com.filipe.api.domain.usuario.Perfil;
import com.filipe.api.domain.usuario.PerfilRepository;
import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.domain.usuario.UsuarioRepository;
import com.filipe.api.domain.venda.CrediarioRepository;
import com.filipe.api.domain.venda.FormaPagamento;
import com.filipe.api.domain.venda.ParcelaCrediario;
import com.filipe.api.domain.venda.ParcelaCrediarioRepository;
import com.filipe.api.domain.venda.StatusCrediario;
import com.filipe.api.domain.venda.StatusParcela;
import com.filipe.api.dto.crediario.LiquidarParcelaRequest;
import com.filipe.api.dto.venda.FinalizarVendaRequest;
import com.filipe.api.dto.venda.ItemVendaRequest;
import com.filipe.api.dto.venda.PagamentoRequest;
import com.filipe.api.dto.venda.VendaResponse;
import com.filipe.api.dto.venda.VendaStartRequest;
import com.filipe.api.service.CrediarioService;
import com.filipe.api.service.VendaService;
import com.filipe.api.shared.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CrediarioIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private VendaService vendaService;
    @Autowired
    private CrediarioService crediarioService;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private EstoqueAtualRepository estoqueAtualRepository;
    @Autowired
    private PerfilRepository perfilRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CaixaRepository caixaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ConfiguracaoRepository configuracaoRepository;
    @Autowired
    private CrediarioRepository crediarioRepository;
    @Autowired
    private ParcelaCrediarioRepository parcelaRepository;
    @Autowired
    private LancamentoCaixaRepository lancamentoCaixaRepository;

    @Test
    void deveCriarCrediarioNaVendaFiadoELiquidarParcela() {
        Usuario operador = criarOperador("operador-crediario@test.local");
        Caixa caixa = criarCaixaAberto(operador, "25.00");
        Cliente cliente = criarCliente();
        Produto produto = criarProdutoComEstoque("PROD-CRED-IT", "7891234567002", "5.000");
        criarConfiguracaoFiscalMock();

        VendaResponse venda = vendaService.iniciarVenda(new VendaStartRequest(caixa.getId(), cliente.getId()), operador);
        vendaService.adicionarItem(venda.id(), new ItemVendaRequest(produto.getId(), BigDecimal.ONE, BigDecimal.ZERO));
        vendaService.finalizarVenda(
                venda.id(),
                new FinalizarVendaRequest(List.of(
                        new PagamentoRequest(FormaPagamento.CREDIARIO, new BigDecimal("20.00"), null, null, 2)
                ))
        );

        var crediario = crediarioRepository.findByClienteId(cliente.getId()).getFirst();
        assertThat(crediario.getStatus()).isEqualTo(StatusCrediario.ABERTO);
        assertThat(crediario.getValorTotal()).isEqualByComparingTo("20.00");
        assertThat(crediario.getParcelas())
                .hasSize(2)
                .extracting(ParcelaCrediario::getValor)
                .allSatisfy(valor -> assertThat(valor).isEqualByComparingTo("10.00"));
        assertThat(clienteRepository.findById(cliente.getId())).get()
                .extracting(Cliente::getSaldoDevedor)
                .satisfies(saldo -> assertThat(saldo).isEqualByComparingTo("20.00"));

        ParcelaCrediario primeiraParcela = crediario.getParcelas().stream()
                .filter(parcela -> parcela.getNumeroParcela() == 1)
                .findFirst()
                .orElseThrow();

        crediarioService.liquidarParcela(
                primeiraParcela.getId(),
                new LiquidarParcelaRequest(new BigDecimal("10.00"), caixa.getId()),
                operador
        );

        assertThat(parcelaRepository.findById(primeiraParcela.getId())).get()
                .satisfies(parcela -> {
                    assertThat(parcela.getStatus()).isEqualTo(StatusParcela.PAGO);
                    assertThat(parcela.getValorPago()).isEqualByComparingTo("10.00");
                    assertThat(parcela.getDataPagamento()).isNotNull();
                });
        assertThat(crediarioRepository.findById(crediario.getId())).get()
                .satisfies(atualizado -> {
                    assertThat(atualizado.getStatus()).isEqualTo(StatusCrediario.PAGO_PARCIAL);
                    assertThat(atualizado.getValorPago()).isEqualByComparingTo("10.00");
                });
        assertThat(clienteRepository.findById(cliente.getId())).get()
                .extracting(Cliente::getSaldoDevedor)
                .satisfies(saldo -> assertThat(saldo).isEqualByComparingTo("10.00"));
        assertThat(lancamentoCaixaRepository.findByCaixaId(caixa.getId()))
                .anySatisfy(lancamento -> {
                    assertThat(lancamento.getReferenciaId()).isEqualTo(primeiraParcela.getId());
                    assertThat(lancamento.getValor()).isEqualByComparingTo("10.00");
                });
        assertThat(caixaRepository.findById(caixa.getId())).get()
                .extracting(Caixa::getValorFechamentoSistema)
                .satisfies(saldo -> assertThat(saldo).isEqualByComparingTo("35.00"));
    }

    private Produto criarProdutoComEstoque(String codigoInterno, String codigoBarras, String quantidade) {
        Categoria categoria = categoriaRepository.save(TestDataBuilder.createCategoria());
        Fornecedor fornecedor = fornecedorRepository.save(TestDataBuilder.createFornecedor());
        Produto produto = TestDataBuilder.createProduto(categoria, fornecedor);
        produto.setCodigoInterno(codigoInterno);
        produto.setCodigoBarras(codigoBarras);
        produto = produtoRepository.save(produto);

        estoqueAtualRepository.save(EstoqueAtual.builder()
                .produto(produto)
                .quantidadeAtual(new BigDecimal(quantidade))
                .quantidadeMinima(BigDecimal.ZERO)
                .build());

        return produto;
    }

    private Usuario criarOperador(String email) {
        Perfil perfil = perfilRepository.save(Perfil.builder().nome("OPERADOR").build());
        return usuarioRepository.save(Usuario.builder()
                .nome("Operador Crediario")
                .email(email)
                .senhaHash("hash")
                .perfil(perfil)
                .ativo(true)
                .build());
    }

    private Caixa criarCaixaAberto(Usuario operador, String valorAbertura) {
        BigDecimal valor = new BigDecimal(valorAbertura);
        return caixaRepository.save(Caixa.builder()
                .operador(operador)
                .valorAbertura(valor)
                .valorFechamentoSistema(valor)
                .status(StatusCaixa.ABERTO)
                .diferenca(BigDecimal.ZERO)
                .build());
    }

    private Cliente criarCliente() {
        return clienteRepository.save(Cliente.builder()
                .nome("Cliente Crediario")
                .cpf("12345678901")
                .limiteCredito(new BigDecimal("100.00"))
                .saldoDevedor(BigDecimal.ZERO)
                .ativo(true)
                .build());
    }

    private void criarConfiguracaoFiscalMock() {
        configuracaoRepository.save(Configuracao.builder()
                .razaoSocial("Empresa Teste")
                .cnpj("12345678000198")
                .ambienteSefaz("HOMOLOGACAO")
                .serieNfce(1)
                .numeroSequencialNfce(1L)
                .apiTokenFiscal("")
                .build());
    }
}
