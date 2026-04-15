-- ============================================================
-- Collection App — Schema unificado (referência)
-- Aplique apenas em banco zerado. Para bancos existentes, use
-- as migrations 001–007 em sequência.
--
-- Para recriar do zero:
--   DROP TABLE IF EXISTS user_editions, user_series CASCADE;
--   DROP TYPE  IF EXISTS ownership_enum, read_status_enum CASCADE;
-- ============================================================

-- ── 1. Enums ──────────────────────────────────────────────────────────────────
CREATE TYPE ownership_enum   AS ENUM ('TENHO', 'QUERO');
CREATE TYPE read_status_enum AS ENUM ('LIDO',  'LENDO');

-- ── 2. Séries salvas pelo usuário ─────────────────────────────────────────────
-- Referência rápida à série (sem status de posse/leitura).
-- Salvo ao clicar "Salvar série" na CoversScreen.
CREATE TABLE user_series (
    id           uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      uuid        NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    series_url   text        NOT NULL,   -- ex: "capas/fabulosos-x-men-os/fxm0301"
    series_title text        NOT NULL,
    cover_url    text,                   -- thumbnail da capa do 1º volume
    publisher    text,
    issue_count  text,
    created_at   timestamptz DEFAULT now(),

    UNIQUE (user_id, series_url)
);

-- ── 3. Edições/volumes salvos pelo usuário ────────────────────────────────────
-- Um volume específico com status de posse e/ou leitura.
-- Salvo ao interagir com os chips POSSE/LEITURA na DetailsScreen.
--
-- is_standalone = TRUE quando a edição não pertence a nenhuma série com
-- galeria de capas ("Galeria de capas" ausente no HTML do Guia dos
-- Quadrinhos). Definido automaticamente pelo app, sem interação do usuário.
-- Edições standalone aparecem na aba "Avulsos" da biblioteca.
CREATE TABLE user_editions (
    id           uuid              PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      uuid              NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    guia_url     text              NOT NULL,   -- ex: "edicao/fabulosos-x-men-os-n-1/fxm0301/5968"
    title        text              NOT NULL,
    cover_url    text,
    series_url   text,                         -- FK lógica para user_series.series_url
    series_title text,
    ownership    ownership_enum,
    read_status  read_status_enum,
    is_standalone boolean          NOT NULL DEFAULT false,
    created_at   timestamptz       DEFAULT now(),

    UNIQUE (user_id, guia_url),
    CONSTRAINT check_edition_status
        CHECK (ownership IS NOT NULL OR read_status IS NOT NULL)
);

-- ── 4. Row Level Security ─────────────────────────────────────────────────────
ALTER TABLE user_series   ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_editions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "series_select"  ON user_series FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "series_insert"  ON user_series FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "series_update"  ON user_series FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "series_delete"  ON user_series FOR DELETE USING (auth.uid() = user_id);

CREATE POLICY "editions_select" ON user_editions FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "editions_insert" ON user_editions FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "editions_update" ON user_editions FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "editions_delete" ON user_editions FOR DELETE USING (auth.uid() = user_id);

-- ── 5. Índices ────────────────────────────────────────────────────────────────
CREATE INDEX idx_series_user_id      ON user_series   (user_id);
CREATE INDEX idx_editions_user_id    ON user_editions (user_id);
CREATE INDEX idx_editions_series     ON user_editions (user_id, series_url) WHERE series_url IS NOT NULL;
CREATE INDEX idx_editions_standalone ON user_editions (user_id, is_standalone) WHERE is_standalone = true;

