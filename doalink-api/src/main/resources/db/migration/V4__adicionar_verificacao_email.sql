-- 1. Adicionar flag de verificação de e-mail na tabela de usuários
ALTER TABLE usuarios ADD COLUMN email_verificado BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Tabela de códigos de confirmação de e-mail
CREATE TABLE codigos_verificacao_email (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT      NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    codigo_hash VARCHAR(64) NOT NULL,
    expiracao   TIMESTAMPTZ NOT NULL,
    utilizado   BOOLEAN     NOT NULL DEFAULT FALSE,
    revogado    BOOLEAN     NOT NULL DEFAULT FALSE,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índices para performance
CREATE INDEX idx_codigos_verificacao_usuario ON codigos_verificacao_email (usuario_id);
CREATE INDEX idx_codigos_verificacao_hash ON codigos_verificacao_email (codigo_hash);
