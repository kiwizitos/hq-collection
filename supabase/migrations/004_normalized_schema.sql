-- ============================================================
-- Collection App — Schema normalizado (do zero)
--
-- Rodar no SQL Editor do Supabase após apagar as tabelas antigas.
-- DROP TABLE IF EXISTS user_items, user_editions, user_series CASCADE;
-- DROP TYPE  IF EXISTS category_enum, ownership_enum, read_status_enum CASCADE;
-- ============================================================

-- ── 1. Enums de status (exclusivos do usuário) ───────────────────────────────
CREATE TYPE ownership_enum    AS ENUM ('TENHO', 'QUERO');
CREATE TYPE read_status_enum  AS ENUM ('LIDO',  'LENDO');

-- ── 2. Tabela de séries/títulos — dados públicos do Guia ─────────────────────
--   • Preenchida pelo app no primeiro acesso à série
--   • Compartilhada entre todos os usuários (sem user_id)
--   • guia_url = chave natural: ex. "capas/fabulosos-x-men-os/fxm0301"
CREATE TABLE series (
    guia_url      text PRIMARY KEY,
    title         text NOT NULL,
    publisher     text,
    licensor      text,
    year          text,
    issue_count   text,
    cover_url     text,           -- URL da capa do 1º volume (thumbnail)
    updated_at    timestamptz DEFAULT now()
);

-- ── 3. Tabela de edições — dados públicos do Guia ────────────────────────────
--   • Preenchida pelo app no primeiro acesso à edição
--   • Compartilhada entre todos os usuários (sem user_id)
--   • guia_url = chave natural: ex. "edicao/fabulosos-x-men-os-n-1/fxm0301/5968"
CREATE TABLE editions (
    guia_url      text PRIMARY KEY,
    title         text NOT NULL,
    cover_url     text,           -- URL da capa desta edição específica
    published_in  text,           -- "janeiro de 1996"
    series_url    text REFERENCES series(guia_url) ON DELETE SET NULL,
    updated_at    timestamptz DEFAULT now()
);

-- ── 4. Tabela de status do usuário — única tabela com user_id ────────────────
--   • Uma linha por (usuário × item)
--   • item_url aponta para editions.guia_url OU series.guia_url
--   • item_type distingue qual das duas tabelas referenciar
--   • ownership e read_status são os únicos dados exclusivos do usuário
CREATE TYPE item_type_enum AS ENUM ('EDITION', 'SERIES');

CREATE TABLE user_items (
    id            uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       uuid          NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    item_url      text          NOT NULL,   -- FK lógica para editions ou series
    item_type     item_type_enum NOT NULL,
    ownership     ownership_enum,
    read_status   read_status_enum,
    created_at    timestamptz   DEFAULT now(),

    UNIQUE (user_id, item_url),
    CONSTRAINT check_at_least_one_status
        CHECK (ownership IS NOT NULL OR read_status IS NOT NULL)
);

-- ── 5. Row Level Security ─────────────────────────────────────────────────────
-- series e editions são leitura pública (qualquer auth pode ler)
ALTER TABLE series   ENABLE ROW LEVEL SECURITY;
ALTER TABLE editions ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_items ENABLE ROW LEVEL SECURITY;

CREATE POLICY "series_public_read"   ON series   FOR SELECT USING (true);
CREATE POLICY "series_public_insert" ON series   FOR INSERT WITH CHECK (true);
CREATE POLICY "series_public_update" ON series   FOR UPDATE USING (true);

CREATE POLICY "editions_public_read"   ON editions FOR SELECT USING (true);
CREATE POLICY "editions_public_insert" ON editions FOR INSERT WITH CHECK (true);
CREATE POLICY "editions_public_update" ON editions FOR UPDATE USING (true);

CREATE POLICY "user_items_select" ON user_items FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "user_items_insert" ON user_items FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "user_items_update" ON user_items FOR UPDATE
    USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "user_items_delete" ON user_items FOR DELETE USING (auth.uid() = user_id);

-- ── 6. Índices ────────────────────────────────────────────────────────────────
CREATE INDEX idx_user_items_user_id   ON user_items (user_id);
CREATE INDEX idx_user_items_item_type ON user_items (user_id, item_type);
CREATE INDEX idx_editions_series_url  ON editions (series_url);

