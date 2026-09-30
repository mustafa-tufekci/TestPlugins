# Project Brief — TestPlugins

**Source of truth for project scope.**

## What this repo is

A Cloudstream3 plugin monorepo. Each subdirectory is a Gradle module that compiles into a
single `.cs3` plugin artifact (zip with `manifest.json` + `classes.dex`). Artifacts are
published on the `builds` branch alongside a `plugins.json` index that Cloudstream consumes
via `repo.json`.

Fork workflow: local repo pushes to `fork` = `mustafa-tufekci/TestPlugins` (branch `master`).
`origin` = upstream `recloudstream/TestPlugins` — never push there.

## Core requirements

1. Every user-facing change bumps `version = N` in that module's `build.gradle.kts`
   (no bump ⇒ Cloudstream users get no update).
2. Every change must compile: `./gradlew :<Module>:assembleDebug` locally before commit.
3. Commit messages follow `feat(<module>): … (vN)` / `fix(<module>): … (vN)`.
4. **Never push without explicit user approval.** Commit freely, build locally, batch-push
   only when the user says so.
5. Published artifacts (`build/`, `**/build`) are gitignored — never commit them.

## Scope boundaries

- In scope: Turkish/generic streaming provider plugins, shared extractor APIs, plugin
  manifest/index metadata.
- Out of scope: modifying the Cloudstream app itself; upstream repo changes.
