CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Perfis
CREATE TABLE perfis (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(100) NOT NULL,
    permissoes JSONB,
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Usuários
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    perfil_id UUID REFERENCES perfis(id),
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categorias
CREATE TABLE categorias (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Fornecedores
CREATE TABLE fornecedores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(150) NOT NULL,
    cnpj VARCHAR(14) UNIQUE,
    telefone VARCHAR(20),
    email VARCHAR(100),
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Produtos
CREATE TABLE produtos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    codigo_interno VARCHAR(50) UNIQUE,
    codigo_barras VARCHAR(13) UNIQUE,
    nome VARCHAR(200) NOT NULL,
    descricao TEXT,
    categoria_id UUID REFERENCES categorias(id),
    fornecedor_id UUID REFERENCES fornecedores(id),
    preco_custo NUMERIC(15, 2) NOT NULL,
    preco_venda NUMERIC(15, 2) NOT NULL,
    ncm VARCHAR(8) NOT NULL,
    cest VARCHAR(10),
    cfop VARCHAR(4) NOT NULL,
    situacao_tributaria VARCHAR(50),
    aliquota_icms NUMERIC(5, 2),
    aliquota_pis NUMERIC(5, 2),
    aliquota_cofins NUMERIC(5, 2),
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Estoque Atual
CREATE TABLE estoque_atual (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    produto_id UUID UNIQUE REFERENCES produtos(id),
    quantidade_atual NUMERIC(15, 3) NOT NULL DEFAULT 0,
    quantidade_minima NUMERIC(15, 3) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Movimentações Estoque
CREATE TABLE movimentacoes_estoque (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    produto_id UUID REFERENCES produtos(id),
    tipo VARCHAR(50) NOT NULL,
    quantidade NUMERIC(15, 3) NOT NULL,
    quantidade_anterior NUMERIC(15, 3) NOT NULL,
    quantidade_resultante NUMERIC(15, 3) NOT NULL,
    motivo TEXT,
    referencia VARCHAR(100),
    usuario_id UUID REFERENCES usuarios(id),
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Clientes
CREATE TABLE clientes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) UNIQUE,
    telefone VARCHAR(20),
    limite_credito NUMERIC(15, 2) DEFAULT 0,
    saldo_devedor NUMERIC(15, 2) DEFAULT 0,
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Caixas
CREATE TABLE caixas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    operador_id UUID REFERENCES usuarios(id),
    data_abertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP,
    valor_abertura NUMERIC(15, 2) NOT NULL,
    valor_fechamento_sis NUMERIC(15, 2),
    valor_fechamento_fis NUMERIC(15, 2),
    status VARCHAR(50) NOT NULL,
    diferenca NUMERIC(15, 2)
);

-- Lançamentos Caixa
CREATE TABLE lancamentos_caixa (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    caixa_id UUID REFERENCES caixas(id),
    tipo VARCHAR(50) NOT NULL, -- ENTRADA/SAIDA
    forma_pagamento VARCHAR(50),
    valor NUMERIC(15, 2) NOT NULL,
    descricao TEXT,
    referencia_id UUID,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_id UUID REFERENCES usuarios(id)
);

-- Vendas
CREATE TABLE vendas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero BIGSERIAL UNIQUE,
    operador_id UUID REFERENCES usuarios(id),
    caixa_id UUID REFERENCES caixas(id),
    cliente_id UUID REFERENCES clientes(id),
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valor_bruto NUMERIC(15, 2) NOT NULL,
    valor_desconto NUMERIC(15, 2) DEFAULT 0,
    valor_total NUMERIC(15, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Itens Venda
CREATE TABLE itens_venda (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    venda_id UUID REFERENCES vendas(id),
    produto_id UUID REFERENCES produtos(id),
    quantidade NUMERIC(15, 3) NOT NULL,
    preco_unitario NUMERIC(15, 2) NOT NULL,
    desconto NUMERIC(15, 2) DEFAULT 0,
    valor_total NUMERIC(15, 2) NOT NULL
);

-- Pagamentos
CREATE TABLE pagamentos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    venda_id UUID REFERENCES vendas(id),
    forma_pagamento VARCHAR(50) NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    troco NUMERIC(15, 2) DEFAULT 0,
    nsu VARCHAR(50),
    autorizacao VARCHAR(50)
);

-- Crediario
CREATE TABLE crediario (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cliente_id UUID REFERENCES clientes(id),
    venda_id UUID REFERENCES vendas(id),
    valor_total NUMERIC(15, 2) NOT NULL,
    valor_pago NUMERIC(15, 2) DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Parcelas Crediario
CREATE TABLE parcelas_crediario (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    crediario_id UUID REFERENCES crediario(id),
    numero_parcela INT NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    data_vencimento DATE NOT NULL,
    data_pagamento DATE,
    valor_pago NUMERIC(15, 2) DEFAULT 0,
    status VARCHAR(50) NOT NULL
);

-- Notas Fiscais
CREATE TABLE notas_fiscais (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    venda_id UUID UNIQUE REFERENCES vendas(id),
    numero BIGINT,
    serie INT,
    chave_acesso VARCHAR(44),
    data_emissao TIMESTAMP,
    xml_autorizado TEXT,
    url_danfe VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    mensagem_retorno TEXT,
    protocolo VARCHAR(50),
    ambiente VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
