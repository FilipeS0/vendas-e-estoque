CREATE TABLE ncm (
    codigo VARCHAR(8) PRIMARY KEY,
    descricao TEXT NOT NULL
);

INSERT INTO ncm (codigo, descricao) VALUES 
('22030000', 'Cervejas de malte'),
('22021000', 'Águas minerais e águas gaseificadas, adicionadas de açúcar'),
('21069090', 'Outras preparações alimentícias não especificadas'),
('19059020', 'Biscoitos e bolachas'),
('17049010', 'Chocolate branco'),
('48181000', 'Papel higiênico'),
('34011190', 'Sabões de toucador');
