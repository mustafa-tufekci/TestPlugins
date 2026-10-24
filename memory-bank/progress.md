# Progress — TestPlugins

## Status at last review (2026-10-24)

- 16 modules, all building: `Animecix (v6)`, `Anizium (v1)`, `BelgeselX (v3)`, **CanliTV (v1)**, `CizgiMax (v2)`,
  `DiziBal (v2)`, `DiziFilm (v4)`, **Dizilla (v31)**, `DiziPal (v37)`,
  `FullHDFilm (v3)`, `HDFilmCehennemi (v36)`, `HDFilmCehennemi2 (v11)`, `InatBox (v16)`,
  `SelcukFlix (v10)`, `SezonlukDizi (v15)`, `Youtube (v2)`.
- `./gradlew make makePluginsJson --dry-run` passes cleanly with Java 17 + Android SDK; all targets resolve.
- **cloudstream-builder verification (2026-10-24)**: all 16 provider sources probed end-to-end. Results below.
- **Dizilla (v31)**: Fixed site redesign (Next.js App Router / Turbopack migration). Replaced legacy `__NEXT_DATA__` and AES decryption with Schema.org `TVSeries` JSON-LD parsing, updated search to clean `/ara?q=`, parsed Next.js embedded `sources` JSON, registered `Filese`, `FourFilese`, `Okvip`, `FourOkvip` extractors.
- **CanliTV (v1)**: New live TV & sports plugin with 20+ verified HLS channels (TRT Spor 1-2, TRT Spor Yıldız, tabii Spor 1-6 UEFA, TRT 1, TV8, Show TV, Kanal D, NOW TV dynamic m3u8 resolver, TRT Haber, TRT Belgesel, TRT 2, TRT World, TRT Çocuk, TRT Müzik, Kral FM, Power Türk) plus dynamic `Sporekrani` match schedule scraper mapping to live channel streams.
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
- **SezonlukDizi (v13)** — self-contained `DzenSez` extractor + new `FilemoonSez` (Byse / bysejikuar.com):
  site moved nearly all embed servers behind Cloudflare Turnstile (`/ajax/reCAPTCHADATA.asp` iframe), only Filemoon
  (`bysejikuar.com/e/<code>`) leaks the real iframe. FilemoonSez reverse-engineers Byse: POST `/api/videos/<code>/embed/captcha`
  → custom 32-bit `gr` hash PoW (`<pow_nonce>:<counter>` leading zero bits ≥ difficulty, solution=counter) → `captcha/verify`
  → POST `embed/playback` (X-Captcha-Token + dummy fingerprint accepted) → AES-256-GCM decrypt
  (key = b64url(key_parts[version-1]) + b64url(key_parts[30-version])) → sources m3u8 + tracks subs. Kotlin port verified
  against Python reference on live challenges. v14: `renameLink` in `SezonlukDizi.kt` now sets `source = newName` (was
  `link.source ?: newName`) — the player groups links by `source`, so both language links ("AltYazı - Filemoon" /
  "Dublaj - Filemoon") collapsed into one card and only the sub stream showed. Both codes verified server-side
  (`hz0g2bf6abdq` sub + `udeyrb95j88u` dub via /tmp/opencode/byse_flow.py).
  v15 (cloudstream-builder denetimi sonrası): mainPage'in başına **"Son Bölümler"** eklendi
  (`POST /ajax/dataDefaultSonCikan.asp?d=1&k=0&s=<page>`, boş gövde şart — IIS 411 aksi halde; sayfa 1..5,
  sayfa başına 15 kart, kart = dizi detay linki + "Dizi - 2.Sezon 6.Bölüm" başlığı + poster). JSON-LD
  `@graph` içindeki `trailer[].embedUrl` recursive olarak bulunup `addTrailer` ile trailer alanına veriliyor.
  FilemoonSez linklerine `Referer: https://sezonlukdizi.cc/` eklendi ve kaynaklar mime türü başına tekilleştirildi
  (en yüksek kalite; HLS master kuralı). PoW döngüsü artık tablo/buffer'ları yeniden kullanıyor (iterasyon başına
  512'lik IntArray ayırmıyor, ~150 MB çöp önlendi) ve sayaç byte'ları allocation'sız yazılıyor. Arama, sonuç boşsa
  kelime sayısını azaltarak tekrar deniyor ("regular show the lost tapes" → "regular show" → sonuç).
- **DiziPal (v34)** — rotation chain: 1584 dead → v31 `1586` → v32 `1588` → v33 `1590` → v34 `1591`.
  Subtitle fix (v34): DPlayer hosts embed subs in iframe HTML as JS array (`{"file":"…vtt?sp=3","label":"T\u00fcrk\u00e7e","kind":"captions"}`), NOT in source2.json (no `tracks` key at all) and NOT in master.m3u8 (only AUDIO groups). Sub CDN 404s without Referer → `SubtitleFile.headers` = Referer(absIframe)+UA. Pattern ported from SelcukFlix v9.
  (`f42d224`). Site announces its **next** address `dizipal1591.com` (not yet live on
  2026-10-18); expect another rotation soon.
- **InatBox (v16)**, **Animecix (v6)**, **DiziFilm (v3)**, **FullHDFilm (v3)**,
  **DiziBal (v2)** — building and marked Ok.
- **SelcukFlix (v10)** — movie source fix: film iframe'leri `sn.dplayer*.site` + v1'den beri `sn.hotlinger.com`'a
rewrite ediliyordu ama `sn.hotlinger.com` için kayıtlı extractor yoktu; cloudstream `loadExtractor`
matching `url.startsWith(schemaless mainUrl)` olduğundan film kaynakları sessizce boş dönüyordu
(diziler `four.pichive.online` → `SlcFourPichive` eşleştiği için çalışıyordu). Fix: `SlcSnHotlinger`
(`https://sn.hotlinger.com`) extractor kaydı + plugin registration. Doğrulandı: `sn.hotlinger.com`
iframe→openPlayer token→source2.php `file":"…m.php…m3u8` HLS master (TR audio + FHD) oynanabilir,
gömülü `{"file":"…vtt","label":"","kind":"captions"}` altyazıları extractor regex #1'den çekiliyor.
API-based module (`selcukflix.app/api/bg/`), `status = 3`
  (Beta), Movie+TvSeries, self-contained `SlcContentX` extractor; v2 = all sources,
  v3 = nik-style category homepage + load extras; v4 = homepage trimmed 29 → 5 rows
  (`date_desc`/`imdb_desc`/`comment_desc` via `"orderType|categoryIds"` row data).
- **Anizium (v1)** — NEW anime provider via `api.anizium.co` (+ `Cf-Control` header):
  4 `/page/home` rows, search with `not_displayed` pagination, load via `/anime/get`
  (movie = single Film episode), `loadLinks` servers 1 (mp4) + 2 (hls) `plan=standart`,
  tr/en/ar/es vtt subtitles. `status = 3` (Beta). Locally built (`Anizium.cs3` 18938
  bytes), **not pushed, awaiting approval**. Risk: `Cf-Control` token rotation kills it.

## cloudstream-builder verification results (2026-10-24)

All 16 provider sources were probed against live sites. Status key: ✅ fully verified end-to-end, ⚠️ code OK but external block, 🛠️ build only.

| # | Module | Site | Verification | Result |
|---|--------|------|--------------|--------|
| 1 | Animecix | animecix.tv | /secure/homepage/lists-guests, /secure/search/bleach, /secure/episode-videos-points → Tau Video embeds + Sibnet | ✅ |
| 2 | Anizium | api.anizium.co | /page/home with Cf-Control header → 447KB JSON | ✅ (Beta) |
| 3 | BelgeselX | belgeselx.com | Homepage cats, Google CSE search, diziGetir episodes, /video/data/new5.php YouTube embed | ✅ (Beta) |
| 4 | CanliTV | canlitv.local + sporekrani.com | 20+ HLS channels (TRT Spor, tabii Spor UEFA, national channels) 200 m3u8; Sporekrani 206 matches | ✅ |
| 5 | CizgiMax | cizgimax.online | Homepage 250 cards, /ara?q=, 157 episodes, atob servers → Dublaj Sibnet | ✅ (Beta) |
| 6 | DiziBal | dizibal.org | Homepage 60 cards, /ara?q=matrix, /series/season/episode, data-pv → pilavyerplay m3u8 + 4 subs | ✅ |
| 7 | DiziFilm | dizifilmizle.to | Homepage 61 cards, Next.js RSC flight payload, vidmixi.com embed with bePlayer | ✅ |
| 8 | Dizilla | dizilla.now | Homepage + category, /ara?q=breaking, Next.js Schema.org TVSeries (62 eps), four.pichive.online m3u8 | ✅ |
| 9 | DiziPal | dizipal1592.com | RSC flight → enc+k (PBKDF2-HMAC-SHA512 + AES-256-CBC) → four.dplayer82.site iframe → openPlayer token → source2.php → m.php HLS master | ✅ |
| 10 | FullHDFilm | fullhdfilmizlesene.now | Homepage 56 cards, /arama/matrix, scx rot13+base64 → rapidvid.org → cdnimg4410.shop m3u8 + subs | ✅ |
| 11 | HDFilmCehennemi | hdfilmcehennemi.nl | Homepage pagination API, search, video embeds, Rapidrame eval(IIFE) unpacked → s230.rapidrame.com master.m3u8 | ✅ |
| 12 | HDFilmCehennemi2 | hdfilmcehennemi2.biz | Next.js /api/search, videoPlayerData JSON-LD, resolveVidload → haccodavayup.shop m3u8, VidMolyExtractor | ✅ |
| 13 | InatBox | diziboxen.help / sprboxs.bar / 4k.filmizleeeee.cfd | Cloudflare 403 error code 1006 (ip_banned) on all domains — IP-level block, code + signing (HMAC-SHA256 with HMK) intact | ⚠️ |
| 14 | SelcukFlix | selcukflix.app | AES-256-CBC encrypted API, __NEXT_DATA__ secureData, pichive/four.pichive/hotlinger/sn.hotlinger iframes, SlcContentX extractor | ✅ |
| 15 | SezonlukDizi | sezonlukdizi.cc | ASP.NET AJAX endpoints, arama/search, dataAlternatif/dataEmbed, FilemoonSez Byse PoW + AES-GCM, bysejikuar.com m3u8 | ✅ |
| 16 | Youtube | youtube.com | NewPipeExtractor v0.25.2, Turkish localization (tr/TR), kiosks: Trendler, Müzik, Oyun, Filmler&Diziler, Podcast, Canlı | 🛠️ |

**Build**: `./gradlew make makePluginsJson` → SUCCESS, all 16 `.cs3` artifacts + `plugins.json` produced.

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
