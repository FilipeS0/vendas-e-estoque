CREATE TABLE historico_precos (
    id UUID PRIMARY KEY,
    produto_id UUID NOT NULL,
    preco_custo DECIMAL(15, 2) NOT NULL,
    preco_venda DECIMAL(15, 2) NOT NULL,
    data_alteracao TIMESTAMP NOT NULL,
    motivo VARCHAR(255),
    operador_id UUID,
    CONSTRAINT fk_historico_precos_produto FOREIGN KEY (produto_id) REFERENCES produtos(id)
);
