# Plan 02: Safe Cleanup and Single Source-of-Truth

## Goal

Remove duplication risk and keep only one authoritative tree for development.

## Current Risk Snapshot

- Root repo tracks `bin` as a git submodule pointer (mode `160000`).
- Working tree currently shows `M bin`, so submodule state is dirty.
- There are duplicate-looking files under root and under `bin/`.

## Source-of-Truth Decision

- Keep root tree as canonical:
  - `src/`
  - `frontend/`
  - `.github/`
  - root scripts and manifests
- Remove `bin` from active development workflow.

## Safe Cleanup Procedure

1. Create a safety tag/backup branch before cleanup.
2. Inspect submodule for unique work before removal.
3. If unique work exists in `bin`, export it as patch or commit it in the submodule repo first.
4. Deinitialize and remove submodule from root repo.
5. Ensure `.gitmodules` is removed or no longer references `bin`.
6. Add a guard in docs and CI checks to prevent reintroducing `bin` as source code.
7. Run full build and test after cleanup.

## Suggested Commands

```powershell
# Safety snapshot
 git checkout -b chore/pre-cleanup-snapshot
 git tag cleanup-pre-bin-removal

# Inspect submodule work
 git -C bin status --short
 git -C bin log --oneline --decorate -n 20

# Remove submodule from root repo (only after preserving needed work)
 git submodule deinit -f bin
 git rm -f bin
 if (Test-Path .gitmodules) { git add .gitmodules }

# Validate
 git status
 ./mvnw -Dmaven.repo.local=.m2repo test
 npm --prefix frontend run build
```

## Post-Cleanup Guardrails

- Add section in README stating root paths are canonical.
- Keep `target/`, `node_modules/`, and runtime logs ignored.
- Reject PRs that modify files under removed/deprecated mirror paths.

## Rollback Plan

If anything fails after cleanup:

1. Reset to `cleanup-pre-bin-removal` tag in a recovery branch.
2. Re-run validation and identify missed dependency on submodule paths.
3. Re-apply cleanup in smaller commits.
