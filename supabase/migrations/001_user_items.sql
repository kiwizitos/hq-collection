-- ============================================================
-- Collection App — Supabase Schema
-- Executar no SQL Editor em app.supabase.com > SQL Editor
-- ============================================================

-- 1. Enum de categorias (deve espelhar Category.kt)
CREATE TYPE category_enum AS ENUM ('TEM', 'LIDO', 'LENDO', 'QUERO');

-- 2. Tabela principal
CREATE TABLE user_items (
    id           uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      uuid        NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    guia_url     text        NOT NULL,
    guia_title   text        NOT NULL,
    series_url   text,
    series_title text,
    category     category_enum NOT NULL,
    created_at   timestamptz DEFAULT now(),

    -- Garante que um usuário não salve a mesma edição duas vezes
    UNIQUE (user_id, guia_url)
);

-- 3. Row Level Security
ALTER TABLE user_items ENABLE ROW LEVEL SECURITY;

-- SELECT: usuário só vê seus próprios itens
CREATE POLICY "select_own_items"
    ON user_items FOR SELECT
    USING (auth.uid() = user_id);

-- INSERT: user_id da nova linha deve ser o do usuário logado
CREATE POLICY "insert_own_items"
    ON user_items FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- UPDATE: usuário só atualiza seus próprios itens
CREATE POLICY "update_own_items"
    ON user_items FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- DELETE: usuário só deleta seus próprios itens
CREATE POLICY "delete_own_items"
    ON user_items FOR DELETE
    USING (auth.uid() = user_id);

-- 4. Índice para buscas por usuário (melhora performance do loadGallery)
CREATE INDEX idx_user_items_user_id ON user_items (user_id);

