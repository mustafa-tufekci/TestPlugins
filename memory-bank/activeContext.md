# Active Context — TestPlugins

## Current focus

Maintenance of the two Turkish film providers, most recently **HDFilmCehennemi2** (new
template + vidload player), and the older **HDFilmCehennemi** (obfuscated player).

## Recent changes (see also progress.md)

- `feat(anizium): add Anizium anime provider via api.anizium.co (v1)` — `43c9b53` (pushed, CI green).
- `feat(selcukflix): trim homepage to 5 rows with sort orders (v4)` — `7d4d3ab` (pushed, CI green).
- `deneme` — `0f06e40`: user committed `memory-bank/*.md` → bank files are now **tracked** (rule change: bank updates will show as normal diffs).
- `feat(SelcukFlix): nik-style category homepage and load extras (v3)` — `33b620e` (pushed, CI green).
- `fix(SelcukFlix): try all episode/movie sources instead of first only (v2)` — `00a4e5d` (pushed, CI green).
- `feat(SelcukFlix): initial port from Kekik reference (v1)` — `d325895` (pushed, CI green).
- `feat(dizibox): ajax dwls_search with fallbacks, homepage hardening (v9)` — `4478f00` (pushed, CI green).
- `feat(dizibox): trim homepage to 5 rows, 150ms delay (v8)` — `37b742e` (pushed, CI green).
- `fix(dizipal): switch to rotated domain dizipal1586.com (v31)` — `5f05052` (pushed, CI green).
- `fix(dizipal): switch to canonical domain dizipal1584.com (v30)` — `145bb3c` (pushed, CI green).
- `fix(dizibox): detect current cloudflare challenge page (v7)` — `f3c8e4f` (pushed, CI green).
- `feat(hdfilmcehennemi2): actor photos from oyuncu listesi block (v10)` — `b65c4c5`.
- `fix(hdfilmcehennemi2): parse vidload Tracks JSON for subtitles (v9)`.
- `feat(hdfilmcehennemi2): add HDFilmCehennemi2 plugin v8 (new template, vidload player)`.
- `fix(hdfilmcehennemi): extract player subtitle tracks from page script, keep absolute vtt urls (v36)`.
- `fix(hdfilmcehennemi): decode new splice-family player obfuscation (v35)`.
- `docs: add AGENTS.md with build/workflow rules and compile gotchas`.

**State:** HEAD = `5f05052` == `fork/master`, **pushed**; CI run `37211987526` success;
Last pushed = `7d4d3ab`; CI run `36903143263` success; `builds` branch publishes 12 modules
incl. `SelcukFlix v4` (`SelcukFlix.cs3` 45142 bytes).
Earlier: `0f06e40` + SelcukFlix v3/v2/v1 runs all green.
`origin/master` (upstream) is unrelated and intentionally not synced.

## DiziPal SSL error (2026-09-28) — `CertPathValidatorException: Trust anchor …`

- User saw this on the plugin homepage. Origin is healthy: `dizipal1583.com`/`1584.com` serve a
  complete chain (leaf → GTS `WE1` → `GTS Root R4` cross-signed by `GlobalSign Root CA`), certs
  reissued **2026-09-27**; `openssl verify` against Mozilla roots passes for both the 3-cert and
  2-cert variants. Rows/search verified live (54 cards, `cKey` + `/bg/searchcontent` OK).
- ⇒ The cert the *device* saw was not this chain → DNS/ISP hijack (dizipal is routinely
  BTK-blocked and rotates numbered domains) or a local TLS interceptor (AdGuard/antivirus/
  private DNS). Cloudstream prints `Try a VPN or DNS.` for it.
- Shipped fix: `mainUrl` + `iconUrl` moved to the canonical host `dizipal1584.com` (page
  canonical + 222 self-links; 1583 only 301s to it). Domain numbers rotate fast — re-check
  `rel=canonical` whenever DiziPal errors again.

## DiziBOX diagnosis (2026-09-28) — root cause found & fixed in v7

- Site now returns Cloudflare **managed challenge**: `HTTP 403`, `server: cloudflare`,
  `cf-mitigated: challenge`, title `Just a moment...`, body `/cdn-cgi/challenge-platform/`.
  The old Turkish warning string the plugin matched **is never produced anymore**.
- So `CloudflareInterceptor` never called `cloudflareKiller.intercept` → no WebView bypass →
  every page returned challenge HTML → empty homepage/search, load without episodes,
  `loadLinks` false → "no links found".
- Fix: broaden trigger to title `Just a moment...` / `Bir dakika lütfen...`, body
  `cdn-cgi/challenge-platform`, legacy Turkish string, or
  `code in [403,503] && Server == cloudflare`; `response.close()` before re-invoking the
  killer; `sequentialMainPageDelay` 50 → 200 ms; emit `User-Agent` in m3u8 headers
  (molystream segments answer **404** to `ExoPlayerLib/2.19.1`).
- Everything else verified live on 2026-09-28: all CSS selectors, `1.Sezon 7.Bölüm` regex,
  POST search, `woca-linkpages-dd` options, king.php → `#Player iframe` → moly embed →
  `CryptoJS.AES.decrypt("…","…")` regex → `file: '/embed/sheila/…'` → m3u8 → segment 200
  **only with** `Referer: https://dbx.molystream.org/`.
- **Reference repo `nikyokki/nik-cloudstream` uses the same stale Turkish string** → its
  DiziBox (v10) is broken too; nothing to port from it.

## DiziBOX homepage speed + search check (2026-09-28) — v8

- User: homepage looked permanently loading → 26 sequential rows (`mainPageOf`) at 200 ms each
  ≈ 20 s before anything shows. Cut to **5 rows**: `/` (Beklenen/Eklenen), `/efsane-diziler/`,
  `/tum-bolumler/page/SAYFA/`, `…?tip=populer`, `/dizi-arsivi/page/SAYFA/` — dropped the
  "Yerli" + 21 genre archive rows. Delay 200 → 150 ms. `getMainPage`'s `else` branch still
  serves the archive row (`article.detailed-article`). Version 7 → 8.
- All 5 rows verified live via in-page `fetch`: rec 10 / poster 25 / epcard 30 / epcard 30 /
  detail 15.
- **Search verified working**: plugin's exact `POST https://www.dizibox.live/` with
  form body `s=lanterns` + cookies → 200, title `lanterns - DiziBOX`, 1 `article.detailed-article`
  whose `h3 a` text = `Lanterns`, href `/diziler/lanterns/`, `figure img` poster present →
  `toDetailResult()` matches every field it reads. (GET `/?s=…` also works; site search form
  itself is `method=get`, but POST returns the same result set.)

## DiziBOX search empty in app + slow homepage (2026-09-29) — v9

- User: homepage perpetually loading; DiziBOX absent from in-app search results.
- External research: `keyiflerolsun/Kekik-cloudstream` (archived Mar 2025), `nik-cloudstream`
  (Aug 2025), `fsamet`/`maarrem`/`MakotoTokioki` cs-Kekik forks — all stale (GET `?s=` search,
  old CF string). Only active impl: **`feroxx/Kekik-cloudstream`** (DiziBox rewritten
  2026-09-08, v23, `status = 1`, same `mainUrl = https://www.dizibox.live`).
- Ported from feroxx: `search()` tries AJAX `GET wp-admin/admin-ajax.php` with
  `params = {s, action: dwls_search}` + `X-Requested-With: XMLHttpRequest` + `Referer` first
  (our `29d1a85` attempt lacked the XRW header and used inline `parsedSafe`, hence failed),
  parsed via repo-standard Jackson `objectMapper` into new `DbxAjaxSearchResponse`/
  `DbxAjaxSearchResult` models (`DiziBoxModels.kt`, `Dbx`-prefixed: package `com.panates` is
  shared across modules); then POST `/` fallback, then GET `/?s=` fallback.
- AJAX endpoint verified live: `?s=lanterns&action=dwls_search` → 200 JSON, `results[0]` =
  `Lanterns`, `permalink` `/diziler/lanterns/`, `attachment_thumbnail` `…/lanterns-50x50.jpg`
  (code rewrites `50x50` → `200x290`).
- `getMainPage()` hardening: `page == 1` strips `/page/SAYFA/` to the canonical URL
  (verified: `/tum-bolumler/page/1/` and `/dizi-arsivi/page/1/` both 301 to the bare path),
  plus `cacheTime = 60` on the row request (NiceHttp 0.4.11 supports it — confirmed via
  `javap` + upstream README; compiles clean). Version 8 → 9.
- Builds: `:DiziBOX:assembleDebug` + `make makePluginsJson` → SUCCESS; `plugins.json` →
  `('DiziBOX', 9)`; `DiziBOX.cs3` 39221 bytes. Commit `4478f00` (local, awaiting push).


## SelcukFlix — new module by user (2026-09-30, v1→v3, all pushed + CI green)

- `d325895` v1: initial port from Kekik reference — `SelcukFlix.kt` (MainAPI,
  `mainUrl = https://selcukflix.com`, API-based: `$mainUrl/api/bg/$endpoint` + search
  `$mainUrl/api/bg/searchcontent?searchterm=`), `SelcukFlixModels.kt` (19 `Slc*` data
  classes), `SelcukFlixPlugin.kt`, extractor `SlcContentXExtractor.kt`
  (`SlcContentX` + `SlcHotlinger`). `status = 3` (Beta), Movie+TvSeries.
- `00a4e5d` v2: try all episode/movie sources instead of first only.
- `33b620e` v3: nik-style category homepage + load extras.
- Verified, not built locally: all 4 pushes CI-success; `builds`/`plugins.json` lists
  `SelcukFlix v3`; all `Slc*`/`SelcukFlix` class names unique across modules (no
  `com.panates` collision); `settings.gradle.kts` auto-includes the module.

## SelcukFlix homepage trim (2026-10-01) — v4

- User: homepage with per-genre rows (29 sequential API POSTs) loads forever.
- Live site homepage has only 4 content blocks: Popüler Diziler, Güncel Bölümler (tabbed),
  Trend Filmler, Son Eklenen Filmler (tabbed). Trend lists are SSR (`getTrendSeries`);
  bg API has no trend endpoint — verified orderTypes on /kesfet: `date_desc`,
  `imdb_desc`, `comment_desc` (+ presumed `imdb_asc`); `findMovies` + `imdb_desc` /
  `comment_desc` verified 200 with encrypted `response` payload.
- Shipped: 29 → 5 rows (Yeni Eklenen Filmler/Diziler `date_desc`, IMDb Top Film/Dizi
  `imdb_desc`, Popüler Diziler `comment_desc`); row `data` format is now
  `"orderType|categoryIdsComma"`, parsed in `getMainPage`. Version 3 → 4.
- Builds: `:SelcukFlix:assembleDebug` + `make makePluginsJson` → SUCCESS; `plugins.json`
  → `('SelcukFlix', 4)`; `SelcukFlix.cs3` 45142 bytes. Commit `7d4d3ab` (local, awaiting push).

## Next steps

- Keep the memory bank current after every non-trivial change (`activeContext.md`,
  `progress.md`, and `productContext.md`'s version table).
- Watch for further site template/player changes on hdfilmcehennemi*.biz hosts.
- When the user asks for a release: commit → local build → await push approval → push to
  `fork` → `gh run watch` → verify `plugins.json` on `builds`.

## Patterns & preferences learned

- The user wants **memory-bank discipline**: read all bank files at task start, update after
  significant changes, and re-review everything on the trigger phrase **update memory bank**.
- Concise commit subjects, version number in the subject, module name in parens.
- Prefer self-contained modules over cross-plugin extractor dependencies.
- Verify against the real site (trace load/stream flow) before flipping `status = 1`.

## Insights

- `settings.gradle.kts` auto-inclusion means a new module only needs a directory with a
  `build.gradle.kts` — no registration step.
- Because all modules share package `com.panates`, adding a class may break *another*
  module's compile; a full `./gradlew make` is the only safe check after adding top-level types.
