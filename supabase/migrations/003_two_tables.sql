-- ============================================================
-- Collection App — Migração 003: Duas tabelas (edições + séries)
--
-- Substitui a tabela única `user_items` por:
--   • user_editions  → volumes/edições individuais
--   • user_series    → títulos/séries completos
--
-- Executar no SQL Editor em app.supabase.com > SQL Editor
-- ============================================================

-- ── 1. Criar enums (caso ainda não existam da migração 002) ──────────────────
DO $$ BEGIN
    CREATE TYPE ownership_enum   AS ENUM ('TENHO', 'QUERO');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE read_status_enum AS ENUM ('LIDO',  'LENDO');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ── 2. Tabela de edições individuais ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_editions (
    id            uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       uuid          NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    guia_url      text          NOT NULL,
    guia_title    text          NOT NULL,
    cover_url     text,                          -- URL da capa, para exibição rápida
    series_url    text,                          -- URL da série à qual pertence (nullable)
    series_title  text,                          -- Título da série (nullable)
    ownership     ownership_enum,
    read_status   read_status_enum,
    created_at    timestamptz   DEFAULT now(),

    UNIQUE (user_id, guia_url),
    CONSTRAINT check_edition_status CHECK (ownership IS NOT NULL OR read_status IS NOT NULL)
);

-- ── 3. Tabela de séries/títulos ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_series (
    id            uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       uuid          NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    series_url    text          NOT NULL,
    series_title  text          NOT NULL,
    cover_url     text,                          -- Capa do 1º volume, para exibição rápida
    publisher     text,                          -- Editora nacional (opcional)
    issue_count   text,                          -- Nº de edições (opcional)
    ownership     ownership_enum,
    read_status   read_status_enum,
    created_at    timestamptz   DEFAULT now(),

    UNIQUE (user_id, series_url),
    CONSTRAINT check_series_status CHECK (ownership IS NOT NULL OR read_status IS NOT NULL)
);

-- ── 4. Migrar dados da tabela antiga (se existir) ────────────────────────────
INSERT INTO user_editions (user_id, guia_url, guia_title, series_url, series_title, ownership, read_status, created_at)
SELECT
    user_id,
    guia_url,
    guia_title,
    series_url,
    series_title,
    ownership,
    read_status,
    created_at
FROM user_items
ON CONFLICT (user_id, guia_url) DO NOTHING;

-- ── 5. Row Level Security — user_editions ────────────────────────────────────
ALTER TABLE user_editions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "editions_select_own" ON user_editions FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "editions_insert_own" ON user_editions FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "editions_update_own" ON user_editions FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "editions_delete_own" ON user_editions FOR DELETE USING (auth.uid() = user_id);

-- ── 6. Row Level Security — user_series ──────────────────────────────────────
ALTER TABLE user_series ENABLE ROW LEVEL SECURITY;

CREATE POLICY "series_select_own" ON user_series FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "series_insert_own" ON user_series FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "series_update_own" ON user_series FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "series_delete_own" ON user_series FOR DELETE USING (auth.uid() = user_id);

-- ── 7. Índices ────────────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_editions_user_id    ON user_editions (user_id);
CREATE INDEX IF NOT EXISTS idx_editions_series_url ON user_editions (user_id, series_url) WHERE series_url IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_series_user_id      ON user_series (user_id);

-- ── 8. (Opcional) Remover tabela antiga após confirmar migração ───────────────
-- ATENÇÃO: execute esta linha manualmente, só após confirmar que os dados migraram corretamente.
-- DROP TABLE IF EXISTS user_items;
-- DROP TYPE  IF EXISTS category_enum;

