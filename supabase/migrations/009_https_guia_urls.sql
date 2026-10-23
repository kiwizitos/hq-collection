-- Normaliza somente capas legadas do Guia dos Quadrinhos para HTTPS.
-- Nao altera identidades (guia_url/series_url): exigem auditoria e deduplicacao.
-- Confirmar schema e backup antes de executar. Nao resolve bloqueios Cloudflare.

BEGIN;

-- 1) Capas salvas
UPDATE user_editions
SET cover_url = regexp_replace(
    cover_url,
    '^http://(www\.)?guiadosquadrinhos\.com(?=/|[?#]|$)',
    'https://www.guiadosquadrinhos.com',
    'i'
)
WHERE cover_url ~* '^http://(www\.)?guiadosquadrinhos\.com(?=/|[?#]|$)';

UPDATE user_series
SET cover_url = regexp_replace(
    cover_url,
    '^http://(www\.)?guiadosquadrinhos\.com(?=/|[?#]|$)',
    'https://www.guiadosquadrinhos.com',
    'i'
)
WHERE cover_url ~* '^http://(www\.)?guiadosquadrinhos\.com(?=/|[?#]|$)';

COMMIT;
