package com.filipe.api.shared;

import com.filipe.api.domain.caixa.Caixa;
import com.filipe.api.domain.caixa.StatusCaixa;
import com.filipe.api.domain.cliente.Cliente;
import com.filipe.api.domain.produto.Categoria;
import com.filipe.api.domain.produto.CstPisCofins;
import com.filipe.api.domain.produto.Csosn;
import com.filipe.api.domain.produto.Fornecedor;
import com.filipe.api.domain.produto.OrigemProduto;
import com.filipe.api.domain.produto.Produto;
import com.filipe.api.domain.produto.UnidadeMedida;
import com.filipe.api.domain.usuario.Perfil;
import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.domain.venda.ItemVenda;
import com.filipe.api.domain.venda.StatusVenda;
import com.filipe.api.domain.venda.Venda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TestDataBuilder {

    public static Categoria createCategoria() {
        return Categoria.builder()
                .id(UUID.randomUUID())
                .nome("Categoria Teste")
                .ativo(true)
                .build();
    }

    public static Fornecedor createFornecedor() {
        return Fornecedor.builder()
                .id(UUID.randomUUID())
                .nome("Fornecedor Teste")
                .cnpj("12345678000100")
                .ativo(true)
                .build();
    }

    public static Produto createProduto(Categoria categoria, Fornecedor fornecedor) {
        return Produto.builder()
                .id(UUID.randomUUID())
                .nome("Produto Teste")
                .codigoInterno("PROD001")
                .codigoBarras("7891234567890")
                .unidadeMedida(UnidadeMedida.UN)
                .precoCusto(new BigDecimal("10.00"))
                .precoVenda(new BigDecimal("20.00"))
                .ncm("12345678")
                .cfop("5102")
                .origem(OrigemProduto.NACIONAL)
                .csosn(Csosn.TRIBUTADA_SEM_PERMISSAO_CREDITO)
                .cstPisCofins(CstPisCofins.OPERACAO_TRIBUTAVEL_AL_NORMAL)
                .aliquotaIcms(BigDecimal.ZERO)
                .aliquotaPis(BigDecimal.ZERO)
                .aliquotaCofins(BigDecimal.ZERO)
                .categoria(categoria)
                .fornecedor(fornecedor)
                .ativo(true)
                .build();
    }

    public static Usuario createUsuario() {
        return createUsuario("usuario.teste@example.com", "OPERADOR");
    }

    public static Usuario createUsuario(String email, String perfilNome) {
        return Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Usuario Teste")
                .email(email)
                .senhaHash("$2a$12$hashdeteste")
                .perfil(createPerfil(perfilNome))
                .ativo(true)
                .build();
    }

    public static Cliente createCliente() {
        return Cliente.builder()
                .id(UUID.randomUUID())
                .nome("Cliente Teste")
                .cpf("12345678901")
                .email("cliente.teste@example.com")
                .telefone("11999999999")
                .limiteCredito(new BigDecimal("500.00"))
                .saldoDevedor(BigDecimal.ZERO)
                .ativo(true)
                .build();
    }

    public static Caixa createCaixa(Usuario operador) {
        return Caixa.builder()
                .id(UUID.randomUUID())
                .operador(operador)
                .dataAbertura(LocalDateTime.now())
                .valorAbertura(new BigDecimal("100.00"))
                .valorFechamentoSistema(new BigDecimal("100.00"))
                .status(StatusCaixa.ABERTO)
                .diferenca(BigDecimal.ZERO)
                .build();
    }

    public static Venda createVenda(Usuario operador, Caixa caixa, Cliente cliente) {
        return Venda.builder()
                .id(UUID.randomUUID())
                .operador(operador)
                .caixa(caixa)
                .cliente(cliente)
                .dataHora(LocalDateTime.now())
                .valorBruto(BigDecimal.ZERO)
                .valorDesconto(BigDecimal.ZERO)
                .valorDescontoVenda(BigDecimal.ZERO)
                .valorTotal(BigDecimal.ZERO)
                .status(StatusVenda.EM_ANDAMENTO)
                .build();
    }

    public static Venda createVenda(Usuario operador, Caixa caixa, Cliente cliente, Produto produto) {
        Venda venda = createVenda(operador, caixa, cliente);
        ItemVenda item = ItemVenda.builder()
                .id(UUID.randomUUID())
                .produto(produto)
                .quantidade(BigDecimal.ONE)
                .precoUnitario(produto.getPrecoVenda())
                .desconto(BigDecimal.ZERO)
                .valorTotal(produto.getPrecoVenda())
                .build();

        venda.adicionarItem(item);
        venda.recalcularTotais();
        return venda;
    }

    private static Perfil createPerfil(String nome) {
        return Perfil.builder()
                .id(UUID.randomUUID())
                .nome(nome)
                .ativo(true)
                .build();
    }
}
