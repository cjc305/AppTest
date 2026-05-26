-- Migration 002: RPC for app owner to list matched testers' emails
-- Run in: Supabase Dashboard → SQL Editor
-- Safe to run multiple times (CREATE OR REPLACE FUNCTION)
--
-- Context (Plan A, 2026-05-26):
-- Replaces the Google-Group auto-sync flow (Plan C, migration 001-ish field
-- `apps.testing_group_email`). The new flow shows dev a copyable list of
-- matched testers' emails in AppEditor; dev pastes them into Play Console's
-- closed-test allowlist manually. Simpler than Plan C (no 5-step Group setup).
--
-- Privacy note: testers' email is normally hidden from owners by RLS
-- (profiles policy in §7 of _specs/database_schema.md). This RPC intentionally
-- bypasses that via SECURITY DEFINER, but ONLY for the app owner viewing their
-- own app. Justification: Play Console allowlist needs the real Gmail address;
-- there is no hashing path that Play accepts.

-- ─── RPC ──────────────────────────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION public.get_matched_tester_emails(p_app_id uuid)
RETURNS TABLE (
    email       text,
    status      text,
    assigned_at timestamptz
)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, auth
AS $$
DECLARE
    v_owner uuid;
BEGIN
    -- Ownership check: caller must own this app (and it must not be soft-deleted).
    SELECT owner_id INTO v_owner
    FROM public.apps
    WHERE id = p_app_id
      AND deleted_at IS NULL;

    IF v_owner IS NULL THEN
        RAISE EXCEPTION 'app not found or deleted' USING ERRCODE = 'P0002';
    END IF;

    IF v_owner <> auth.uid() THEN
        RAISE EXCEPTION 'forbidden: not the app owner' USING ERRCODE = '42501';
    END IF;

    -- Return matched testers' emails, excluding abandoned (no point listing
    -- testers who already quit; Play Console allowlist should be active set only).
    RETURN QUERY
    SELECT
        u.email::text          AS email,
        m.status::text         AS status,
        m.assigned_at          AS assigned_at
    FROM public.matches m
    JOIN auth.users u ON u.id = m.tester_id
    WHERE m.app_id = p_app_id
      AND m.status IN ('matched', 'installed', 'active', 'completed')
      AND u.email IS NOT NULL
    ORDER BY m.assigned_at DESC NULLS LAST;
END;
$$;

-- Lock down + grant: only authenticated users may call; the function itself
-- enforces the per-app ownership check.
REVOKE ALL ON FUNCTION public.get_matched_tester_emails(uuid) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.get_matched_tester_emails(uuid) TO authenticated;
