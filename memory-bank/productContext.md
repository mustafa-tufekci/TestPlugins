# Product Context — TestPlugins

## Why this project exists

Cloudstream users in the Turkish-speaking market need working, maintained providers for
film/series/anime sites. Sites change templates, players, and obfuscation constantly, so
plugins need frequent maintenance. This repo is the personal fork ("Panates Plugins") that
ships those fixes.

## How it should work

- A user adds `repo.json` as a Cloudstream repo source and installs individual plugins.
- Each plugin exposes `MainAPI` implementations (search, main page, load) and, where needed,
  `ExtractorAPI` implementations for the video hosts the sites embed.
- Playback must resolve to a playable `ExtractorLink` (m3u8/mp4) with subtitles when the
  player provides them.

## UX goals

- Correct metadata: posters, backgrounds, ratings (`Score`), actors with photos when the
  site provides them.
- Subtitles preserved (absolute `.vtt` URLs, Tracks JSON parsing).
- Fast, resilient loads: per-item `continue` on parse failures instead of failing the whole page.
- `status = 1` (Ok) only for fully verified flows; otherwise 3 (Beta) or 0 (Down).

## Providers currently shipped

| Module | Version | Status | Notes |
|---|---|---|---|
| HDFilmCehennemi | 36 | 1 | Obfuscated player, splice-family decoding, subtitle tracks |
| HDFilmCehennemi2 | 10 | 1 | New template, vidload player, actor photos |
| Dizilla | 26 | 1 | Hero image background + poster fallback |
| DiziPal | 30 | 1 | mainUrl = canonical `dizipal1584.com` (numbered domains rotate) |
| SezonlukDizi | 12 | 1 | Self-contained `DzenSez` extractor |
| InatBox | 16 | 1 | Requires kotlinx-coroutines dependency |
| Animecix | 6 | 1 | |
| DiziBOX | 9 | 1 | CF managed-challenge detection (`Just a moment...`), AJAX dwls_search (+XRW) with POST/GET fallback, 5 homepage rows (150ms, page-1 canonical, cacheTime 60), UA in stream headers |
| DiziFilm | 3 | 1 | |
| FullHDFilm | 3 | 1 | |
| DiziBal | 2 | 1 | |
