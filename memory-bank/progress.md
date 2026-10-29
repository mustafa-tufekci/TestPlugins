# Progress

## 2026-10-30 — bozuk kaynak onarım turu

Tamamlanan:

- BelesTivi modülü tamamen kaldırıldı (36 sağlayıcı kaldı).
- CanliTV, neowatch'in iptv-org kaynağının birebir portuyla değiştirildi (v8);
  eski hardcoded kanal listesi (`CanliTVChannels.kt`) silindi.
- ContentX oynatıcıları `window.NP_CONFIG` destekli (Dizilla v34, SelcukFlix v13,
  DiziPal v40) — "link bulunamadı" kök nedeni buydu.
- FilmMakinesi (v3) ve HDFilmCehennemi (v40): Rhino polyfill'i tam tarayıcı taklidine
  çevrildi; P.A.C.K.E.R. paketleri artık çözülüyor.
- Pilavyer zinciri `pilavyerplay.top` + `api/stream.php`/`api/sub.php`'ye taşındı
  (DiziBal v6, FilmModu v3); SetFilmIzle player hostu `setplay.sbs` doğrulandı.
- Afiş düzeltmesi: 16 modüle `<Önek>Resim` yardımcısı; 16:9 kapak/platform logosu yerine
  dikey büyük afiş, en büyük srcset, bilinen boyut yükseltmeleri (v6…v40 hepsi bump).
- Canlandırılan kaynaklar: WebteIzle (webteizle.info), SuperFilmGeldi
  (superfilmizle.icu), JetFilmizle (jetfilmizle.app), KultFilmler (kultfilmler.net,
  vidpapi FirePlayer), TurkAnime (turkanime.live, StreamCore), DiziYou (diziyou.net),
  WebDramaTurkey (FirePlayer hostu dtpasn.asia).
- Kalıcı ölü ilan edilenler: DiziMom, SinemaCX, UgurFilm → `status = 0`.

Bekleyen:

- Bu turun commit'lerinin `fork/master`'a push edilmesi (kullanıcı onayı).
- WebteIzle oynatması Cloudflare Turnstile yüzünden kapalı; resmî çözüm yok.
- DiziBox davranışı yalnızca uygulama içinde (CloudflareInterceptor) doğrulanabilir. — TestPlugins

## Status at last review (2026-10-29, HEAD `dc598d9`, push edildi + CI green)

- **37 modül**, hepsi derleniyor; `./gradlew make makePluginsJson` → BUILD SUCCESSFUL,
  `build/plugins.json` 37 sağlayıcı listeliyor. `fork/master` = `dc598d9`, CI run
  `40906447013` **success**, `builds` branch'inde 37 kayıt doğrulandı.
- 20 yeni modül Beta (`status = 3`), canlı oynatma doğrulaması bekliyor.
- Sürümler: Animecix 8, Anizium 3, BelesTivi 7, BelgeselX 4, CanliTV 7, CizgiMax 3,
  DiziBal 4, DiziBox 1, DiziFilm 5, DiziKorea 1, Dizilla 32, DiziMom 1, DiziPal 38,
  DiziYou 1, FilmMakinesi 1, FilmModu 1, FullHDFilm 4, HDFilmCehennemi 38,
  HDFilmCehennemi2 13, InatBox 17, JetFilmizle 1, KoreanTurk 1, KultFilmler 1,
  NetflixMirror 1, RareFilmm 1, RecTV 1, SelcukFlix 11, SetFilmIzle 1, SezonlukDizi 15,
  SinemaCX 1, SuperFilmGeldi 1, TurkAnime 1, UgurFilm 1, Watch2Movies 1,
  WebDramaTurkey 1, WebteIzle 1, Youtube 2.
- **Hiçbiri push edilmedi.** `fork/master` = `418a8e1`; lokal 33 commit ileride.
  Önceki CI koşusu `40189043379` (17 modüllük hâl) yeşildi.

## neowatch portu (2026-10-29)

Referans: `neoser1984/neowatch` (`/tmp/opencode/neowatch`, GPL-3.0, 35 modül +
`__Temel`). 13 ortak modül karşılaştırıldı: 7'sinde biz daha iyiydik (Animecix,
BelgeselX, Dizilla, SelcukFlix, SezonlukDizi, Youtube), 2'sinde onlar (CanliTV,
DiziBal), 4'ü dengeliydi (CizgiMax, DiziPal, FullHDFilm, HDFilmCehennemi, InatBox).
Kullanıcı "hem özellikler hem yeni sağlayıcılar" dedi → 9 özellik portu + 20 yeni modül.

### Özellik portları

- **CanliTV v7** — `CanliTVIptv.kt`: runtime `https://iptv-org.github.io/iptv/countries/tr.m3u`
  (172 kanal), 10 dk `@Volatile` cache, NSFW filtresi, `GROUP_TR` Türkçe etiketler,
  `#EXTINF`/`#EXTVLCOPT`/`url|k=v` başlık ayrıştırma. 5 yeni satır (`IPTV Türkiye`,
  `IPTV Spor/Haber/Çocuk/Müzik`), `/iptv/play?u=&n=&g=&l=&h=` URL şeması, `loadLinks`'te
  kanal başına header, `load`'da aynı gruptan 12 öneri.
- **DiziBal v4** — 13 platform/tür satırı + `?page=N` sayfalama (`a.group.block` kartları,
  JSON-LD ItemList yedeği), JSON-LD `Movie`/`TVSeries` düğümünden gölge afiş, süre, tür,
  puan (bestRating 10), oyuncular (`/storage/tmdb/profiles`), trailer.
- **Dizilla v32** — birden çok `TVSeries` düğümü toplanıyor: birincil (bölümler, oyuncular,
  ülke, `timeRequired`) + `genre` taşıyan ikincil (türler, yönetmen, sezon sayısı).
  Oyuncu rolü `additionalName`'den (`ActorData.roleString`), yönetmenler "Yönetmen" rolüyle,
  bölüm `date` (`yyyy-MM-dd`, UTC) ve `runTime` (ISO-8601 `PT48M` → dakika), `duration`,
  trailer yalnızca gerçek video URL'leri için (site `contentUrl`'ü `#trailer`'a işaret ediyor).
- **DiziPal v38** — `DpPlayerExtractors.kt` (`DpJwKaynak`/"PalPlayer" formationfeed.net,
  `DpStreamCore` streamcorecdn.com, `DpVideoPlays` videoplays.cfd) + `DpRmk.kt`
  (`[data-rm-k]` AES-CBC/PKCS5, elle yazılmış PBKDF2-HMAC-SHA512 999 tur).
- **HDFilmCehennemi v38** — `HdcGizliJsOynatici.kt`: Rhino sandbox + P.A.C.K.E.R,
  `/rplayer/`, `/playerr/`, `rapid*`, `file_link` base64. Kaydedilmez (mainUrl çakışması),
  yalnızca `loadExtractor` başarısız olursa çağrılır.
- **CizgiMax v3** — `CmxSibNet`, `CmxDrive`, `CmxCizgiDuo`, `CmxCizgiPass`.
- **FullHDFilm v4** — `FhfTurboImgz`.
- **BelgeselX v4** — `BelOdnoklassniki` + `BelOkRuSsl` + `BelOkRuHttp`.
- **InatBox v17** — dinamik dizin **port edilmedi** (IV kurtarılamıyor; neowatch'in kendi
  `makulMu()` kontrolü de reddeder, statik yedek sadece `test` içeriyor). Onun yerine 403
  `x-st` saat farkı: `clockSkewSeconds`, `signRequest(..., offsetSeconds)`, 2 denemeli döngü.

### Yeni modüller (hepsi v1, status 3)

DiziBox, DiziKorea, DiziMom, DiziYou, FilmMakinesi, FilmModu, JetFilmizle, KoreanTurk,
KultFilmler, NetflixMirror, RareFilmm, RecTV, SetFilmIzle, SinemaCX, SuperFilmGeldi,
TurkAnime, UgurFilm, Watch2Movies, WebDramaTurkey, WebteIzle.

## What works

- **HDFilmCehennemi (v38)** — splice-family decode, altyazı izleri, sayfalama,
  CloudflareInterceptor + genel Rhino/PACKER yedeği.
- **HDFilmCehennemi2 (v13)** — vidload `JSON.parse` config, `Tracks` altyazıları, oyuncu
  fotoğrafları.
- **SezonlukDizi (v15)** — kendi içinde `DzenSez` + `FilemoonSez` (Byse PoW → AES-256-GCM),
  "Son Bölümler" satırı, JSON-LD trailer, çok kelimeli arama yedeği.
- **DiziPal (v38)** — `dizipal1592.com` RSC/App-Router → PBKDF2 + AES-CBC → ContentX
  ailesi + 3 yeni decoder + `data-rm-k`.
- **Dizilla (v32)** — JSON-LD tabanlı zengin metadata, 8 host alias'ı, altyazı hattı.
- **CanliTV (v7)** — curated HLS + runtime iptv-org + Sporekrani maç satırı.
- **BelesTivi (v7)** — çift M3U + 10 gömülü Turkuvaz kanalı, 10 dk cache, `quickSearch`.
- **InatBox (v17)** — HMAC-SHA256 imzalama + saat farkı düzeltmesi sağlam; canlı testte
  Cloudflare 403 error 1006 (IP ban) dış engeldi.
- **SelcukFlix (v11)**, **Animecix (v8)**, **Anizium (v3)**, **DiziFilm (v5)**,
  **FullHDFilm (v4)**, **DiziBal (v4)**, **Youtube (v2)** — derleniyor ve çalışıyor.

## Doğrulama durumu

`cloudstream-builder` taraması (2026-10-24) 16 kaynağı uçtan uca prob etti: 14 ✅,
1 ⚠️ (InatBox, dış IP ban), 1 🛠️ (Youtube, yalnızca build). O tarihten sonra eklenen
BelesTivi v7, CanliTV v7 ve 20 yeni modül bu taramada yok.

## What's left / open

- **20 yeni modülün hiçbiri canlı doğrulanmadı** → hepsi `status = 3`. Doğrulananlar
  `status = 1` yapılmalı.
- Push onayı bekleniyor (`git push fork master`); sonra CI + `builds/plugins.json` kontrolü.
- Birim testi yok; doğrulama = derleme + uygulamada oynatma.
- `README.md` hâlâ upstream şablonu (`ExampleProvider` referansı).
- Molystream yükü altyazı göndermiyor (`tracks: [null, null]`) — bilinçli kapsam dışı.

## Known issues / risks

- Site şablonları ve oynatıcı obfuscation'ı habersiz değişir; `status = 1` son manuel
  doğrulamayı yansıtır, sürekli izlemeyi değil.
- Cloudflare challenge markup'ı kayar — tek bir literal string'e asla bağlanma.
- Jackson 2.13.1'de sabit; yükseltmek eski Android cihazları kırar.
- jvmTarget 1.8 inline JVM-11 API'lerini (`tryParseJson`) yasaklar.
- Rhino yalnızca `compileOnly`; jar cache'te yok, app sağlıyor.
- Cross-plugin extractor araması global ve modül kaynağından görünmez.

## Evolution of decisions

1. JitPack `-SNAPSHOT` kırılınca recloudstream gradle plugin jar'ı vendor'landı.
2. Paylaşılan extractor'lardan bundled extractor'lara geçildi (SezonlukDizi `Dzen` →
   `DzenSez`) — kendi kendine yeterlilik.
3. `AGENTS.md` ile build komutları ve derleme tuzakları kalıcılaştırıldı.
4. Memory bank eklendi; oturumlar arası bağlam korunuyor.
5. neowatch karşılaştırması sonrası "hazır liste" yerine **runtime kaynak** tercihi
   (CanliTV iptv-org) ve **genel JS çözücü** tercihi (HDFilmCehennemi Rhino/PACKER)
   benimsendi.
6. Doğrulanamayan upstream özellikleri körü körüne port etmeme kararı (InatBox dinamik
   dizin); bunun yerine protokol davranışı (x-st) uygulandı.
## 2026-10-29 — push öncesi denetim

- 36 kaynağın tamamı canlı prob edildi; sonuçlar `activeContext.md` içinde tablo hâlinde.
- HDFilmCehennemi ana sayfası yeni sunucu taraflı yapıya göre yeniden yazıldı (v41).
- RecTV, NetflixMirror, Watch2Movies → `status = 0` (v2): alan adları/DNS kapalı.
- Bilinen sınır: WebteIzle oynatma Turnstile ile kapalı; InatBox/Youtube ağdan
  doğrulanamıyor (imzalı CDN / NewPipe).
- Yerel commit'ler hazır, push onay bekliyor.

## 2026-10-29 — push tamamlandı

- `git push fork master` → `dc598d9..8a0d7c9` (12 commit).
- CI run `40925823516` → **success**; `fork/builds` üzerindeki `plugins.json`
  doğrulandı: **36 sağlayıcı**, sürümler yerel `build/plugins.json` ile birebir
  (HDFilmCehennemi v41, CanliTV v8, Dizilla v34, DiziPal v40, DiziYou/TurkAnime/
  KultFilmler/JetFilmizle v3, RecTV/NetflixMirror/Watch2Movies/DiziMom/SinemaCX/
  UgurFilm v2 & status 0).
- Açık kalan bilinen sınırlar: WebteIzle oynatma (Turnstile), InatBox + Youtube
  ağdan doğrulanamıyor, SuperFilmGeldi `luciferplays.com` buradan erişilemiyor.
