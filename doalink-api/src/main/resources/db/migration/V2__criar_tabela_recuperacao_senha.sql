CREATE TABLE token_recuperacao_senha (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT      NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expiracao   TIMESTAMPTZ NOT NULL,
    utilizado   BOOLEAN     NOT NULL DEFAULT FALSE,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_token_recuperacao_hash ON token_recuperacao_senha (token_hash);
CREATE INDEX idx_token_recuperacao_usuario ON token_recuperacao_senha (usuario_id);
