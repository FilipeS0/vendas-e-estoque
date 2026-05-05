package com.filipe.api.integration;

import com.filipe.api.AbstractIntegrationTest;
import com.filipe.api.domain.caixa.Caixa;
import com.filipe.api.domain.caixa.CaixaRepository;
import com.filipe.api.domain.caixa.LancamentoCaixaRepository;
import com.filipe.api.domain.caixa.StatusCaixa;
import com.filipe.api.domain.configuracao.Configuracao;
import com.filipe.api.domain.configuracao.ConfiguracaoRepository;
import com.filipe.api.domain.estoque.EstoqueAtual;
import com.filipe.api.domain.estoque.EstoqueAtualRepository;
import com.filipe.api.domain.estoque.MovimentacaoEstoqueRepository;
import com.filipe.api.domain.estoque.TipoMovimentacaoEstoque;
import com.filipe.api.domain.fiscal.NotaFiscalRepository;
import com.filipe.api.domain.fiscal.StatusNfe;
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
import com.filipe.api.domain.venda.FormaPagamento;
import com.filipe.api.domain.venda.StatusVenda;
import com.filipe.api.domain.venda.VendaRepository;
import com.filipe.api.dto.venda.FinalizarVendaRequest;
import com.filipe.api.dto.venda.ItemVendaRequest;
import com.filipe.api.dto.venda.PagamentoRequest;
import com.filipe.api.dto.venda.VendaResponse;
import com.filipe.api.dto.venda.VendaStartRequest;
import com.filipe.api.service.VendaService;
import com.filipe.api.shared.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class VendaFluxoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private VendaService vendaService;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private EstoqueAtualRepository estoqueAtualRepository;
    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    @Autowired
    private PerfilRepository perfilRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CaixaRepository caixaRepository;
    @Autowired
    private ConfiguracaoRepository configuracaoRepository;
    @Autowired
    private NotaFiscalRepository notaFiscalRepository;
    @Autowired
    private VendaRepository vendaRepository;
    @Autowired
    private LancamentoCaixaRepository lancamentoCaixaRepository;

    @Test
    void deveConfirmarVendaBaixarEstoqueEmitirNotaFiscalMockERegistrarCaixa() {
        Usuario operador = criarOperador("operador-venda-fluxo@test.local");
        Caixa caixa = criarCaixaAberto(operador, "100.00");
        Produto produto = criarProdutoComEstoque("PROD-VENDA-IT", "7891234567001", "10.000");
        criarConfiguracaoFiscalMock();

        VendaResponse vendaIniciada = vendaService.iniciarVenda(new VendaStartRequest(caixa.getId(), null), operador);
        vendaService.adicionarItem(
                vendaIniciada.id(),
                new ItemVendaRequest(produto.getId(), new BigDecimal("2.000"), BigDecimal.ZERO)
        );

        VendaResponse vendaFinalizada = vendaService.finalizarVenda(
                vendaIniciada.id(),
                new FinalizarVendaRequest(List.of(
                        new PagamentoRequest(FormaPagamento.DINHEIRO, new BigDecimal("50.00"), null, null, null)
                ))
        );

        assertThat(vendaFinalizada.status()).isEqualTo(StatusVenda.CONFIRMADA);
        assertThat(vendaFinalizada.valorTotal()).isEqualByComparingTo("40.00");

        assertThat(estoqueAtualRepository.findByProdutoId(produto.getId()))
                .get()
                .extracting(EstoqueAtual::getQuantidadeAtual)
                .satisfies(qtd -> assertThat(qtd).isEqualByComparingTo("8.000"));

        assertThat(movimentacaoEstoqueRepository.findComFiltros(
                produto.getId(), TipoMovimentacaoEstoque.SAIDA_VENDA, null, null, org.springframework.data.domain.Pageable.ofSize(1)
        )).hasSize(1);

        assertThat(notaFiscalRepository.findByVendaId(vendaIniciada.id()))
                .get()
                .satisfies(nota -> {
                    assertThat(nota.getStatus()).isEqualTo(StatusNfe.AUTORIZADA);
                    assertThat(nota.getXmlAutorizado()).contains(vendaIniciada.id().toString());
                });

        assertThat(vendaRepository.findById(vendaIniciada.id())).get()
                .satisfies(venda -> assertThat(venda.getStatus()).isEqualTo(StatusVenda.CONFIRMADA));
        assertThat(lancamentoCaixaRepository.findByCaixaIdAndReferenciaId(caixa.getId(), vendaIniciada.id()))
                .hasSize(1)
                .first()
                .satisfies(lancamento -> assertThat(lancamento.getValor()).isEqualByComparingTo("40.00"));
        assertThat(caixaRepository.findById(caixa.getId())).get()
                .extracting(Caixa::getValorFechamentoSistema)
                .satisfies(saldo -> assertThat(saldo).isEqualByComparingTo("140.00"));
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
                .nome("Operador Integracao")
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

    private void criarConfiguracaoFiscalMock() {
        configuracaoRepository.save(Configuracao.builder()
                .razaoSocial("Empresa Teste")
                .cnpj("12345678000199")
                .ambienteSefaz("HOMOLOGACAO")
                .serieNfce(1)
                .numeroSequencialNfce(1L)
                .apiTokenFiscal("")
                .build());
    }
}
