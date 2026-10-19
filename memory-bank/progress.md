# Progress — TestPlugins

## Status at last review (2026-10-19)

- 15 modules, all building: `Animecix (v6)`, `Anizium (v1)`, `BelgeselX (v3)`, `CizgiMax (v2)`,
  `DiziBal (v2)`, `DiziFilm (v4)`, `Dizilla (v30)`, `DiziPal (v35)`,
  `FullHDFilm (v3)`, `HDFilmCehennemi (v36)`, `HDFilmCehennemi2 (v11)`, `InatBox (v16)`,
  `SelcukFlix (v9)`, `SezonlukDizi (v12)`, **Youtube (v2)**.
- **DiziBOX**: Removed completely from repository on user request.
- **Youtube v2**: Added Turkish localization (`Localization("tr", "TR")`, `ContentCountry("TR")`, `language = "tr"`, Turkish kiosk headers Trendler, Müzik, Oyun, Filmler & Diziler, Podcast'ler, Canlı Yayınlar) on `NewPipe` and extractors so the homepage and search display Turkish content by default. Ported and improved from `recloudstream/extensions` with upstream `loadLinks` URL formatting bug fix.
- **BelgeselX v3**: drops dead empty-cid jw links, sira=1 retry fallback for series, defers YouTube mirrors.
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
- **DiziPal (v34)** — rotation chain: 1584 dead → v31 `1586` → v32 `1588` → v33 `1590` → v34 `1591`.
  Subtitle fix (v34): DPlayer hosts embed subs in iframe HTML as JS array (`{"file":"…vtt?sp=3","label":"T\u00fcrk\u00e7e","kind":"captions"}`), NOT in source2.json (no `tracks` key at all) and NOT in master.m3u8 (only AUDIO groups). Sub CDN 404s without Referer → `SubtitleFile.headers` = Referer(absIframe)+UA. Pattern ported from SelcukFlix v9.
  (`f42d224`). Site announces its **next** address `dizipal1591.com` (not yet live on
  2026-10-18); expect another rotation soon.
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

## What's left / open

- No automated tests exist; verification is compile + manual playback in the Cloudstream app.
- Molystream payload no longer ships subtitles (`tracks: [null, null]`) — subtitles
  unavailable on that source; reported, deliberately out of scope for v7.
- `README.md` still upstream boilerplate (references `ExampleProvider`).
- Stray root files `t.vtt`, `t2.vtt` from subtitle debugging were removed (`4eb7833`).
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
