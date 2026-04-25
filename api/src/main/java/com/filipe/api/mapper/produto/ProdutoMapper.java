package com.filipe.api.mapper.produto;

import com.filipe.api.domain.produto.Categoria;
import com.filipe.api.domain.produto.Fornecedor;
import com.filipe.api.domain.produto.Produto;
import com.filipe.api.dto.produto.ProdutoDetalheResponse;
import com.filipe.api.dto.produto.ProdutoRequest;
import com.filipe.api.dto.produto.ProdutoResponse;
import com.filipe.api.dto.produto.ProdutoUpdateRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProdutoMapper {

    public Produto toEntity(ProdutoRequest request, Categoria categoria, Fornecedor fornecedor) {
        return Produto.builder()
                .codigoInterno(request.codigoInterno())
                .codigoBarras(request.codigoBarras())
                .nome(request.nome())
                .descricao(request.descricao())
                .categoria(categoria)
                .fornecedor(fornecedor)
                .precoCusto(request.precoCusto())
                .precoVenda(request.precoVenda())
                .ncm(request.ncm())
                .cest(request.cest())
                .cfop(request.cfop())
                .situacaoTributaria(request.situacaoTributaria())
                .aliquotaIcms(request.aliquotaIcms())
                .aliquotaPis(request.aliquotaPis())
                .aliquotaCofins(request.aliquotaCofins())
                .ativo(true)
                .build();
    }

    public void updateEntity(Produto produto, ProdutoUpdateRequest request, Categoria categoria, Fornecedor fornecedor) {
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setCategoria(categoria);
        produto.setFornecedor(fornecedor);
        produto.setPrecoCusto(request.precoCusto());
        produto.setPrecoVenda(request.precoVenda());
        produto.setNcm(request.ncm());
        produto.setCest(request.cest());
        produto.setCfop(request.cfop());
        produto.setSituacaoTributaria(request.situacaoTributaria());
        produto.setAliquotaIcms(request.aliquotaIcms());
        produto.setAliquotaPis(request.aliquotaPis());
        produto.setAliquotaCofins(request.aliquotaCofins());
    }

    public ProdutoResponse toResponse(Produto produto, BigDecimal quantidadeEstoque) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getCodigoInterno(),
                produto.getCodigoBarras(),
                produto.getNome(),
                produto.getCategoria() != null ? produto.getCategoria().getNome() : null,
                produto.getFornecedor() != null ? produto.getFornecedor().getNome() : null,
                produto.getPrecoVenda(),
                quantidadeEstoque,
                produto.getAtivo()
        );
    }

    public ProdutoDetalheResponse toDetalheResponse(Produto produto, BigDecimal quantidadeEstoque) {
        return new ProdutoDetalheResponse(
                produto.getId(),
                produto.getCodigoInterno(),
                produto.getCodigoBarras(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getCategoria().getId(),
                produto.getFornecedor().getId(),
                produto.getPrecoCusto(),
                produto.getPrecoVenda(),
                produto.getNcm(),
                produto.getCest(),
                produto.getCfop(),
                produto.getSituacaoTributaria(),
                produto.getAliquotaIcms(),
                produto.getAliquotaPis(),
                produto.getAliquotaCofins(),
                quantidadeEstoque,
                produto.getAtivo()
        );
    }

    public com.filipe.api.dto.produto.ProdutoPdvResponse toPdvResponse(Produto produto, BigDecimal quantidadeEstoque) {
        return new com.filipe.api.dto.produto.ProdutoPdvResponse(
                produto.getId(),
                produto.getNome(),
                produto.getCodigoBarras(),
                produto.getPrecoVenda(),
                quantidadeEstoque
        );
    }
}
