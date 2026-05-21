-- Migration 001: Add columns present in spec but missing from initial Supabase schema
-- Run in: Supabase Dashboard → SQL Editor
-- Safe to run multiple times (IF NOT EXISTS / column checks)
-- Audited against live DB on 2026-05-21.

-- ─── profiles ─────────────────────────────────────────────────────────────────

ALTER TABLE public.profiles
    ADD COLUMN IF NOT EXISTS streak_days         INT          NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS credits             INT          NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS preferred_categories TEXT[]      DEFAULT '{}';

-- ─── apps ─────────────────────────────────────────────────────────────────────

ALTER TABLE public.apps
    ADD COLUMN IF NOT EXISTS package_name        TEXT,
    ADD COLUMN IF NOT EXISTS play_opt_in_url     TEXT,
    ADD COLUMN IF NOT EXISTS required_testers    INT          NOT NULL DEFAULT 12,
    ADD COLUMN IF NOT EXISTS required_days       INT          NOT NULL DEFAULT 14,
    ADD COLUMN IF NOT EXISTS deleted_at          TIMESTAMPTZ;

-- Soft-delete helper: hide deleted apps from default queries
CREATE INDEX IF NOT EXISTS apps_deleted_at_idx ON public.apps (deleted_at)
    WHERE deleted_at IS NULL;

-- ─── matches ──────────────────────────────────────────────────────────────────

ALTER TABLE public.matches
    ADD COLUMN IF NOT EXISTS match_score         DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS days_active         INT          NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_heartbeat_at   TIMESTAMPTZ;

-- ─── notifications ────────────────────────────────────────────────────────────

ALTER TABLE public.notifications
    ADD COLUMN IF NOT EXISTS is_read             BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deep_link           TEXT;

-- ─── proofs ───────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.proofs (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id        UUID        NOT NULL REFERENCES public.matches(id) ON DELETE CASCADE,
    tester_id       UUID        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    app_id          UUID        NOT NULL REFERENCES public.apps(id) ON DELETE CASCADE,
    app_name        TEXT        NOT NULL,
    tester_handle   TEXT        NOT NULL,
    tier            TEXT        NOT NULL DEFAULT 'Newcomer',
    completed_at    TIMESTAMPTZ NOT NULL,
    hmac_signature  TEXT        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Proof lookup by match (one-to-one)
CREATE UNIQUE INDEX IF NOT EXISTS proofs_match_id_idx ON public.proofs (match_id);

-- RLS: testers can read their own proofs; public can verify by proof id (read-only)
ALTER TABLE public.proofs ENABLE ROW LEVEL SECURITY;

CREATE POLICY IF NOT EXISTS "tester_read_own_proofs"
    ON public.proofs FOR SELECT
    USING (tester_id = auth.uid());

CREATE POLICY IF NOT EXISTS "public_verify_proof"
    ON public.proofs FOR SELECT
    USING (true);  -- proof verification is intentionally public (HMAC ensures authenticity)
