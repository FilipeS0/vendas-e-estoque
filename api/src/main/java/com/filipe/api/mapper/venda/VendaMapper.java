package com.filipe.api.mapper.venda;

import com.filipe.api.domain.venda.ItemVenda;
import com.filipe.api.domain.venda.Pagamento;
import com.filipe.api.domain.venda.Venda;
import com.filipe.api.dto.cliente.ClienteResumoResponse;
import com.filipe.api.dto.venda.ItemVendaResponse;
import com.filipe.api.dto.venda.PagamentoResponse;
import com.filipe.api.dto.venda.VendaResponse;
import com.filipe.api.mapper.caixa.CaixaMapper;
import com.filipe.api.mapper.fiscal.NotaFiscalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VendaMapper {

    private final CaixaMapper caixaMapper;
    private final NotaFiscalMapper notaFiscalMapper;

    public VendaResponse toResponse(Venda venda) {
        List<ItemVendaResponse> itensResponse = venda.getItens().stream()
                .map(this::toItemResponse)
                .toList();

        List<PagamentoResponse> pagamentosResponse = venda.getPagamentos().stream()
                .map(this::toPagamentoResponse)
                .toList();

        return new VendaResponse(
                venda.getId(),
                venda.getNumero(),
                venda.getDataHora(),
                venda.getValorBruto(),
                venda.getValorDesconto(),
                venda.getValorTotal(),
                venda.getStatus(),
                itensResponse,
                pagamentosResponse,
                venda.getCliente() != null ? new ClienteResumoResponse(
                        venda.getCliente().getId(),
                        venda.getCliente().getNome(),
                        venda.getCliente().getCpf()
                ) : null,
                venda.getCaixa() != null ? caixaMapper.toResponse(venda.getCaixa()) : null,
                venda.getNotaFiscal() != null ? notaFiscalMapper.toResponse(venda.getNotaFiscal()) : null
        );
    }

    public ItemVendaResponse toItemResponse(ItemVenda itemVenda) {
        return new ItemVendaResponse(
                itemVenda.getId(),
                itemVenda.getProduto().getId(),
                itemVenda.getProduto().getNome(),
                itemVenda.getQuantidade(),
                itemVenda.getPrecoUnitario(),
                itemVenda.getDesconto(),
                itemVenda.getValorTotal()
        );
    }

    public PagamentoResponse toPagamentoResponse(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getFormaPagamento(),
                pagamento.getValor(),
                pagamento.getTroco()
        );
    }
}
