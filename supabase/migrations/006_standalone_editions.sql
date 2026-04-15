-- ============================================================
-- Collection App — Quadrinhos soltos (standalone editions)
--
-- Adiciona suporte a volumes intencionalmente não agrupados.
-- is_standalone = TRUE → o volume é exibido individualmente
--                        na aba "Avulsos", mesmo tendo series_url.
-- DEFAULT FALSE garante compatibilidade total com registros existentes.
-- ============================================================

ALTER TABLE user_editions
    ADD COLUMN is_standalone boolean NOT NULL DEFAULT false;

