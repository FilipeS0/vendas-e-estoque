package com.filipe.api.dto.venda;

import com.filipe.api.domain.venda.StatusVenda;
import com.filipe.api.dto.caixa.CaixaResponse;
import com.filipe.api.dto.cliente.ClienteResumoResponse;
import com.filipe.api.dto.fiscal.NotaFiscalResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record VendaResponse(
    UUID id,
    Long numero,
    LocalDateTime dataHora,
    BigDecimal valorBruto,
    BigDecimal valorDesconto,
    BigDecimal valorTotal,
    StatusVenda status,
    List<ItemVendaResponse> itens,
    List<PagamentoResponse> pagamentos,
    ClienteResumoResponse cliente,
    CaixaResponse caixa,
    NotaFiscalResponse notaFiscal
) {}
