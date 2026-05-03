CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    acao VARCHAR(100) NOT NULL,
    entidade VARCHAR(100) NOT NULL,
    entidade_id UUID,
    usuario_id UUID REFERENCES usuarios(id),
    detalhe TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_log_entidade ON audit_log(entidade, entidade_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
