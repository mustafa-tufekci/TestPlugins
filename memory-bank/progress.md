# Progress — TestPlugins

## Status at last review (2026-10-22)

- 16 modules, all building: `Animecix (v6)`, `Anizium (v1)`, `BelgeselX (v3)`, **CanliTV (v1)**, `CizgiMax (v2)`,
  `DiziBal (v2)`, `DiziFilm (v4)`, **Dizilla (v31)**, `DiziPal (v35)`,
  `FullHDFilm (v3)`, `HDFilmCehennemi (v36)`, `HDFilmCehennemi2 (v11)`, `InatBox (v16)`,
  `SelcukFlix (v10)`, `SezonlukDizi (v15)`, `Youtube (v2)`.
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
