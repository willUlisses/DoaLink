ALTER TABLE ofertas_doacao              ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;
ALTER TABLE disponibilidades_transporte ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;
ALTER TABLE reservas_entrega            ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;

