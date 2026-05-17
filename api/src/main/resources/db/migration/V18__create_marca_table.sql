CREATE TABLE marcas (
    id UUID PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

ALTER TABLE produtos ADD COLUMN marca_id UUID;
ALTER TABLE produtos ADD CONSTRAINT fk_produto_marca FOREIGN KEY (marca_id) REFERENCES marcas(id);
