# Progress — TestPlugins

## Status at last review (2026-09-29, HEAD `4478f00`)

- 11 modules, all `status = 1` (Ok) in their `cloudstream {}` blocks.
- HEAD == `fork/master` (both pushed); CI run `36623018180` success; `builds` branch publishes
  `DiziBOX v9` (39221 bytes) and `DiziPal v30` — verified via GitHub API.
- Last successful pattern: `./gradlew make makePluginsJson` produces artifacts; CI republishes
  the `builds` branch on push to `master`.

## What works

- **HDFilmCehennemi (v36)** — full flow incl. splice-family player script decoding, subtitle
  track extraction from page script with absolute `.vtt` URLs, metadata parsing fixed to read
  HTML via JsonNode (no Boolean-parse bug from `meta.canonical`).
- **HDFilmCehennemi2 (v10)** — new site template, vidload player, `Tracks` JSON subtitle
  parsing, actor photos from the *oyuncu listesi* block.
- **Dizilla (v26)** — hero image as background poster with poster fallback.
- **SezonlukDizi (v12)** — self-contained `DzenSez` extractor (no reliance on another plugin).
- **DiziPal (v30)** — mainUrl moved to canonical `dizipal1584.com` after SSL/diagnosis run.
- **InatBox (v16)**, **Animecix (v6)**, **DiziFilm (v3)**, **FullHDFilm (v3)**,
  **DiziBal (v2)** — building and marked Ok.
- **DiziBOX (v9)** — Cloudflare challenge detection rewritten after live diagnosis (v7);
  homepage trimmed 26 → 5 rows with 150 ms sequential delay (v8); search switched to
  AJAX `dwls_search` (+XRW header, Jackson parse) with POST/GET fallbacks + `getMainPage`
  page-1 canonicalization and `cacheTime = 60` (v9, ported from active `feroxx` fork).
  Full selector + player chain and both search paths verified against `dizibox.live`.

## What's left / open

- No automated tests exist; verification is compile + manual playback in the Cloudstream app.
- DiziBOX v7/v8/v9 are compiled but **not yet played back in the app** — needs a manual check
  after push (search results + rows + m3u8 with the new UA header).
- Molystream payload no longer ships subtitles (`tracks: [null, null]`) — subtitles
  unavailable on that source; reported, deliberately out of scope for v7.
- `README.md` still upstream boilerplate (references `ExampleProvider`).
- Stray root files `t.vtt`, `t2.vtt` from subtitle debugging are uncommitted leftovers.
- Memory bank created 2026-09-26 — first population of these files, no historical changelog
  beyond git history.

## Known issues / risks

- Site templates and player obfuscation change without notice → plugins break silently;
  `status = 1` reflects last manual verification, not continuous monitoring.
- Cloudflare challenge markup drifts (EN ↔ TR title, challenge-platform script path) —
  never match a single literal string; match title + marker + `(403/503 && Server: cloudflare)`.
- Jackson pinned at 2.13.1; bumping breaks older Android devices.
- jvmTarget 1.8 forbids inline APIs built with JVM 11 bytecode (`tryParseJson`).
- Cross-plugin extractor lookup is global and invisible from module source — a module can
  appear self-contained but depend on another installed plugin.


## Evolution of decisions

1. Vendored the recloudstream gradle plugin jar after JitPack `-SNAPSHOT` resolution broke.
2. Moved from shared extractors to bundled ones (SezonlukDizi `Dzen` → `DzenSez`) for
   self-containment.
3. Adopted `AGENTS.md` to persist build commands and compile gotchas across sessions.
4. Added this memory bank so context survives resets.
