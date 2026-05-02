-- No actual schema change needed as JPA EnumType.STRING uses VARCHAR, 
-- but we ensure all existing statuses match the new Enum names.
UPDATE notas_fiscais SET status = 'AUTORIZADA' WHERE status = 'AUTHORIZED';
UPDATE notas_fiscais SET status = 'CANCELADA' WHERE status = 'CANCELLED';

-- Optional: add a check constraint to ensure only valid Enum values are stored
ALTER TABLE notas_fiscais ADD CONSTRAINT chk_status_nfe 
CHECK (status IN ('PENDENTE', 'AGUARDANDO_SEFAZ', 'AUTORIZADA', 'REJEITADA', 'CONTINGENCIA', 'CANCELADA'));
