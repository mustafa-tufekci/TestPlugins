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
| HDFilmCehennemi | 37 | 1 | Obfuscated player, splice-family decoding, subtitle tracks; v37 adds Close hostname salt |
| HDFilmCehennemi2 | 13 | 1 | New template, vidload player (escaped `JSON.parse` config), actor photos, VidMolyExtractor |
| Dizilla | 31 | 1 | Next.js App Router / Turbopack: Schema.org TVSeries JSON-LD, clean search /ara?q=, Pichive/FourPichive/Filese/Okvip extractors |
| DiziPal | 37 | 1 | mainUrl = `dizipal1592.com`. RSC flight → enc+k (PBKDF2-HMAC-SHA512 + AES-256-CBC) → four.dplayer82.site iframe → openPlayer token → source2.php → m.php HLS master. Bundled `DpContentX` extractor family (`DpContentX`, `DpHotlinger`, `DpPichive`, `DpFourPichive`, `DpSnHotlinger`) |
| CanliTV | 6 | 1 | Live TV & Sports: Sporekrani live match scraper + ~235 verified HLS channel entries (110 from iptv-org in v5, 102 from Beleş TiVi in v6; v4 TURKTV batch was reverted). Direct Wikimedia/favicon poster URLs |
| BelesTivi | 7 | 1 | Live TV from the Beleş TiVi app (com.sunargames.belestivi): dual M3U (`belesplus.m3u` + `playlist.m3u`), 10-min in-memory cache, 10 embedded Turkuvaz channels, YouTube live row, `quickSearch` override (v7) |
| BelgeselX | 3 | 3 | belgeselx.com documentary provider (Beta); jwplayer `/video/data/*.php` sources + iframe fallback |
| CizgiMax | 2 | 3 | cizgimax.online cartoon/anime provider (Beta); atob `servers` JSON → Dublaj Sibnet |
| SezonlukDizi | 15 | 1 | Self-contained `DzenSez` + `FilemoonSez` (Byse PoW+AES-GCM); mainPage'de "Son Bölümler", trailer, arama fallback |
| InatBox | 16 | 1 | Code + HMAC-SHA256 signing intact; Cloudflare 403 error 1006 IP ban on all domains (diziboxen.help, sprboxs.bar, 4k.filmizleeeee.cfd) — external block |
| Animecix | 8 | 1 | animecix.tv secure API, Tau Video + Sibnet links; v7/v8 fix film sources via `/secure/videos`, metaCache, season filter |
| DiziFilm | 5 | 1 | dizifilmizle.to Next.js RSC flight payload, vidmixi.com embed with bePlayer; v5 search content_type filter + JSON-LD year |
| FullHDFilm | 3 | 1 | fullhdfilmizlesene.now scx rot13+base64 → rapidvid.org m3u8 + subs |
| DiziBal | 3 | 1 | dizibal.org SPA JSON-LD homepage + `/ara/oneri` search API, data-pv → pilavyerplay m3u8 + 4 subtitle tracks |
| SelcukFlix | 11 | 3 | API-based (`selcukflix.app/api/bg/`), AES-256-CBC encrypted, 5 homepage rows, `SlcContentX`/`SlcHotlinger`/`SlcPichive`/`SlcFourPichive`/`SlcSnHotlinger` extractors; v11 search uses `used_type` |
| Anizium | 3 | 3 | Anime API (`api.anizium.co` + `Cf-Control` header), servers 1 (mp4) + 2 (hls), tr/en/ar/es vtt subtitles; v2/v3 genre names + epoch years |
| Youtube | 2 | 1 | YouTube provider (Turkish localization TR/tr, videos, channels, playlists, live, kiosks & NewPipe integration) |
