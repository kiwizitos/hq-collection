-- ============================================================
-- Collection App — Migração 002: Multi-status (Posse + Leitura)
-- Substituir a coluna única `category` por dois campos independentes.
--
-- Executar no SQL Editor em app.supabase.com > SQL Editor
-- ============================================================

-- 1. Criar os novos enums
CREATE TYPE ownership_enum    AS ENUM ('TENHO', 'QUERO');
CREATE TYPE read_status_enum  AS ENUM ('LIDO',  'LENDO');

-- 2. Adicionar as novas colunas (nullable para permitir a migração)
ALTER TABLE user_items ADD COLUMN ownership   ownership_enum;
ALTER TABLE user_items ADD COLUMN read_status read_status_enum;

-- 3. Migrar dados existentes da coluna `category` para as novas colunas
UPDATE user_items SET ownership   = 'TENHO' WHERE category = 'TEM';
UPDATE user_items SET ownership   = 'QUERO' WHERE category = 'QUERO';
UPDATE user_items SET read_status = 'LIDO'  WHERE category = 'LIDO';
UPDATE user_items SET read_status = 'LENDO' WHERE category = 'LENDO';

-- 4. Adicionar constraint: ao menos um dos campos deve ser preenchido
ALTER TABLE user_items
    ADD CONSTRAINT check_at_least_one_status
    CHECK (ownership IS NOT NULL OR read_status IS NOT NULL);

-- 5. Remover a coluna e o tipo antigo
ALTER TABLE user_items DROP COLUMN category;
DROP TYPE category_enum;

-- 6. Índices adicionais para buscas por status
CREATE INDEX idx_user_items_ownership   ON user_items (user_id, ownership);
CREATE INDEX idx_user_items_read_status ON user_items (user_id, read_status);

