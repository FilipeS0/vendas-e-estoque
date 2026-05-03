package com.filipe.api.shared.fiscal;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class NfcePayload {
    private String nomeDestinatario;
    private String cpfDestinatario;
    private List<Item> items;
    private List<Pagamento> pagamentos;
    
    @Data
    @Builder
    public static class Item {
        private String codigo;
        private String descricao;
        private String ncm;
        private String cfop;
        private BigDecimal valorUnitario;
        private BigDecimal quantidade;
        private BigDecimal valorTotal;
    }

    @Data
    @Builder
    public static class Pagamento {
        private String formaPagamento;
        private BigDecimal valor;
    }
}
