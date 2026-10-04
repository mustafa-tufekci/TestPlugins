# Progress — TestPlugins

## Status at last review (2026-10-04, HEAD `5f05052`)

- 13 modules (new: **Anizium v1**, `status = 3` Beta, TvType Anime); rest unchanged.
- HEAD == `fork/master` (pushed, CI `37211987526` success): DiziPal v31 (`5f05052`).
  Last pushed state = `7d4d3ab`, CI run `36903143263` success, `builds` branch publishes
  12 modules incl. `SelcukFlix v4` (45142 bytes) — verified via GitHub API.
- `memory-bank/` is now tracked (user committed it in `0f06e40 "deneme"`).
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
- **DiziPal (v31)** — domain rotated again: `dizipal1584.com` TLS-reset (dead), new canonical `dizipal1586.com` (verified live + `rel=canonical`; `1585` 301s to it, `1587/1588` unregistered). Full 13-domain health check 2026-10-04: all other mainUrls HTTP 200.
- **InatBox (v16)**, **Animecix (v6)**, **DiziFilm (v3)**, **FullHDFilm (v3)**,
  **DiziBal (v2)** — building and marked Ok.
- **SelcukFlix (v4)** — API-based module (`selcukflix.com/api/bg/`), `status = 3`
  (Beta), Movie+TvSeries, self-contained `SlcContentX` extractor; v2 = all sources,
  v3 = nik-style category homepage + load extras; v4 = homepage trimmed 29 → 5 rows
  (`date_desc`/`imdb_desc`/`comment_desc` via `"orderType|categoryIds"` row data).
- **Anizium (v1)** — NEW anime provider via `api.anizium.co` (+ `Cf-Control` header):
  4 `/page/home` rows, search with `not_displayed` pagination, load via `/anime/get`
  (movie = single Film episode), `loadLinks` servers 1 (mp4) + 2 (hls) `plan=standart`,
  tr/en/ar/es vtt subtitles. `status = 3` (Beta). Locally built (`Anizium.cs3` 18938
  bytes), **not pushed, awaiting approval**. Risk: `Cf-Control` token rotation kills it.
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
