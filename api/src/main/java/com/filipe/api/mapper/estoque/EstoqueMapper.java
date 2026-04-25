package com.filipe.api.mapper.estoque;

import com.filipe.api.domain.estoque.EstoqueAtual;
import com.filipe.api.domain.estoque.MovimentacaoEstoque;
import com.filipe.api.domain.estoque.TipoMovimentacaoEstoque;
import com.filipe.api.domain.produto.Produto;
import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.dto.estoque.EstoqueAtualResponse;
import com.filipe.api.dto.estoque.MovimentacaoEstoqueResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EstoqueMapper {

    public MovimentacaoEstoqueResponse toResponse(MovimentacaoEstoque movimentacao, Usuario usuario) {
        return new MovimentacaoEstoqueResponse(
                movimentacao.getId(),
                movimentacao.getProduto().getId(),
                movimentacao.getProduto().getNome(),
                movimentacao.getTipo().name(),
                movimentacao.getQuantidade(),
                movimentacao.getQuantidadeAnterior(),
                movimentacao.getQuantidadeResultante(),
                movimentacao.getMotivo(),
                movimentacao.getReferencia(),
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getNome() : null,
                movimentacao.getDataHora()
        );
    }

    public EstoqueAtualResponse toEstoqueAtualResponse(EstoqueAtual estoque) {
        return new EstoqueAtualResponse(
                estoque.getProduto().getId(),
                estoque.getProduto().getNome(),
                estoque.getProduto().getCodigoBarras(),
                estoque.getProduto().getCategoria() != null ? estoque.getProduto().getCategoria().getNome() : null,
                estoque.getQuantidadeAtual(),
                estoque.getQuantidadeMinima()
        );
    }

    public MovimentacaoEstoque toEntity(
            Produto produto,
            TipoMovimentacaoEstoque tipo,
            BigDecimal quantidade,
            BigDecimal quantidadeAnterior,
            BigDecimal quantidadeResultante,
            String motivo,
            String referencia,
            Usuario usuario) {
        return MovimentacaoEstoque.builder()
                .produto(produto)
                .tipo(tipo)
                .quantidade(quantidade)
                .quantidadeAnterior(quantidadeAnterior)
                .quantidadeResultante(quantidadeResultante)
                .motivo(motivo != null ? motivo.trim() : null)
                .referencia(referencia)
                .usuario(usuario)
                .build();
    }
}