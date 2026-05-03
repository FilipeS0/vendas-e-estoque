CREATE TABLE configuracoes (
    id UUID PRIMARY KEY,
    razao_social VARCHAR(255),
    cnpj VARCHAR(20),
    inscricao_estadual VARCHAR(50),
    endereco VARCHAR(255),
    regime_tributario VARCHAR(50),
    ambiente_sefaz VARCHAR(50),
    serie_nfce INTEGER,
    numero_sequencial_nfce BIGINT,
    impressora_termica_ip VARCHAR(50),
    impressora_termica_porta INTEGER,
    alerta_estoque_minimo_global INTEGER,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
