-- Migration: 007_standalone_series
-- Adds is_standalone to user_series to flag single-edition titles that should
-- navigate directly to the edition detail page, bypassing the covers list.

ALTER TABLE user_series
    ADD COLUMN IF NOT EXISTS is_standalone BOOLEAN NOT NULL DEFAULT false;

COMMENT ON COLUMN user_series.is_standalone IS
    'True when the series contains only a single "edição única". '
    'The app skips the covers page and navigates directly to the edition detail.';

