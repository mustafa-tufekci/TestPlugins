# Progress

## 2026-11-25 — DiziMom modülü tamamen kaldırıldı (31 sağlayıcı)

- Kullanıcı raporu: ddizimom.net devlet tarafından engellenmiş, erişime kapatılmış.
  Derin doğrulama siteyi "canlı" gösteriyordu (arama motoru tarayıcısı sayfaları
  çekiyordu) ama TR'den erişim BTK/ISP düzeyinde kapalıymış → kullanıcı için ölü.
  DiziMom (Dzm/DzmPlugin) repodan `git rm` ile tamamen kaldırıldı; çapraz referans
  yoktu (self-contained). README güncellendi: 32 → 31 sağlayıcı.
- Kapalı/erişilemez turu sonucu: RecTV zaten status=0 (Cloudflare Access), InatBox
  v19 ile onarıldı, diğer kaynaklar canlı. DiziMom'dan artık plugins.json kaydı yok.
- Build + makePluginsJson temiz, 31 kayıt. Push onayı bekliyor.

## 2026-11-25 — InatBox Türkçe karakter mojibake onarımı (v19)

- Kullanıcı raporu: eklenti listesinde ad ve kategoriler bozuk ("İnatBox" → "Ä°natBox",
  "Kanalları" → "KanallarÄ±", "Çocuk" → "Ã‡ocuk").
- **Kök neden:** v18 domain değişikliği sırasında InatBox.kt PowerShell'de
  `Get-Content -Raw | Set-Content` ile döndürüldü; PS 5.1 BOM'suz UTF-8'i Windows-1254
  sanıp geri yazdı → UTF-8→CP1254→UTF-8 çift kodlama. **Ders: .kt dosyalarında asla
  PS boru hattı kullanma; yalnız edit aracı (veya git checkout ile bayt-restorasyon).**
- Onarım: dosya git'ten bayt-exact geri alındı (`git checkout b18c599 -- …`), v18
  değişiklikleri edit aracıyla yeniden uygulandı (domainler, canlı kategori dizini,
  çağrı noktaları). Bayt doğrulaması: isim C4 B0 (İ) ✓, FFFD 0, çift-kodlama imzası 0;
  repo geneli 32 modül taraması temiz; üretilen dex'te "İnatBox" doğru, mojibake yok.
- **v18'i bozuk yayınladık** → düzeltme v19 (build.gradle 17→18→19).
- Push onayı bekliyor.

## 2026-11-25 — Tüm kaynakların bağlantı denetimi + InatBox v18 domain onarımı

- 32 kaynağın ana alan adı tarandı (curl, bu makineden; makine Türkiye dışı çıkışlı,
  birkaç site geo/bot filtreli). Sonuçlar:
  - ✅ Doğrudan 200: Animecix, BelgeselX, CanliTV (m3u), CizgiMax, DiziFilm, DiziKorea,
    DiziYou, HDFilmCehennemi2, KultFilmler, RareFilmm, SetFilmIzle, SezonlukDizi,
    SuperFilmGeldi (→superfilmizle2.icu yönlendirmesiyle), TurkAnime, Youtube (15).
  - ⚠️ Canlı ama bot/geo-kapılı (301-no-location challenge, derin doğrulama yapılamadı):
    DiziPal, Dizilla, DiziBal, Anizium, DiziBox, FilmMakinesi, FullHDFilm,
    HDFilmCehennemi, JetFilmizle, KoreanTurk, SelcukFlix, Watch2Movies, WebDramaTurkey.
  - 🔄 Rotasyon sinyali: FilmModu news→icu (301), SuperFilmGeldi icu→superfilmizle2.icu
    (yönlendirme çalışıyor; mainUrl güncellemesi ertelenebilir).
  - ❌ DiziMom (ddizimom.net): iki çıkıştan da TLS el sıkışması reddi — TR-geo sanılıyor;
    cihazda doğrulanmalı.
  - ❌ RecTV: Cloudflare Access login'i arkasında (status=0 zaten doğru).
- **InatBox gerçek arıza**: diziboxen.help NXDOMAIN (Google DNS'te bile yok), sprboxs.bar
  da ölü. Uygulamanın uzak ayar zinciri elle çözüldü ve uçtan uca doğrulandı:
  mtlshash/cert (katmanlı AES, IV-dizesi anahtar) → DC2 ct.php (imzalı POST) →
  30 kategorili canlı liste → **diziboxen.top** ve **sprbox.top**. İmzalı POST +
  katmanlı çözüm PS'te elle koşturuldu; "Ulusal" 42 kanal döndü.
- InatBox v18: hardcoded alan adları canlılara güncellendi (diziboxen.top / sprbox.top)
  + neowatch'un dinamik kategori dizini portu: kategori URL'leri 30 dk cache'li
  dizinden yol-eşleşmesiyle çözülür (kategoriAdresi), alan adı değişince eklenti
  güncellemesi gerekmez. 4K Filmler (4k.filmizleeeee.cfd) dizinde yok ve bu makineden
  erişilemiyor; satır boş düşüyor, dokunulmadı.
- Build temiz, plugins.json 32 kayıt, InatBox v18 artifact üretildi.
- **Push onayı bekliyor** (InatBox v18 + bu kayıt).

## 2026-11-25 — DiziPal v45 "no links found" onarımı (neowatch Kasım 2026 portu)

- Kullanıcı raporu: DiziPal sayfalar açılıyor ama oynatma "no links found" veriyor.
  Canlı site bu makinadan geo-bloklu; neowatch'un Kasım 2026 güncellemesi referans alındı
  (DiziPalGuncel + ContentXGenel + NpKapi).
- Kök nedenler (üçü birden onarıldı):
  1. `parseEnc` kırılgandı: `"enc":{...},"k":"..."` bitişikliğini zorunlu kılıyordu;
     site araya tek anahtar eklese çözüm null düşüyor → kaynak tamamen kayboluyordu.
     Yeni `decryptPlayer(raw)`: parantez eşleştirmeli enc nesnesi + konum-kapsamlı "k"
     + DpRmk.PAROLA sabit yedeği (neowatch oynaticiCoz portu).
  2. `parseSources` `],"language"` bitişikliği ve sabit anahtar sırası istiyordu;
     anahtar sırasından bağımsız nesne taramasına çevrildi.
  3. NP_CONFIG'e Kasım 2026'da "gate" PoW eklendi: `{"n","b"}` varken master/altyazı
     adreslerine `&p=<çözüm>` eklenmezse CDN 404 veriyor. `DpNpGate` portu eklendi:
     sayfa başına üretilen `window.NP_GATE` fonksiyonu çözümlenir, yoksa
     `sha256(n:i)[0] >>> (32-b) == 0` yedeği kullanılır (InatBox x-st PoW ailesi).
- `DpRmk.decryptFromPage` şabona dayalı çift desen (attribute + element metni).
- `DpContentX`'e eski tip oynatıcı yedeği (openPlayer yoksa gömülü `file:`).
- loadLinks dispatch artık emisyon-farkında: DpContentX bağlantı üretmezse extractor
  kayıt defterine düşülüyor (daha önce sessizce "kaynak yok" bitiriyordu).
- Sentetik RSC testiyle anahtar sırası/bitşiklik senaryoları doğrulandı; build temiz.
- **Dikkat:** gate aynı ContentX/dplayer oynatıcı ailesini kullanan SelcukFlix
  (SlcContentX) ve Dizilla (ContentXExtractor/Hotlinger) için de risk taşıyor;
  kullanıcı geri bildirimi gelirse aynı port oraya da yapılmalı.
- **Push onayı bekliyor** (DiziPal v45 commit'i; önceki 4 commit'lik batch push edildi).

## 2026-11-25 — DiziPal/Dizilla runtime domain fallback + README rewrite

- DiziPal v44: `ensureDomain()` — aday listesi (1595…1592 + 1596/1597) ilk istekte
  8sn timeout'la probe edilir, ana sayfasında `/series/` geçen ilk aday `mainUrl` olur;
  `mainPage` URL'leri init'te ilk adayla piştiği için `getMainPage`'te remap yapılır.
  Park edilmiş alan adları içerik kontrolüyle elenir. Hiçbiri cevap vermezse mevcut
  davranış (ilk aday) korunur. NiceHttp parametreleri: `timeout` var, `cache` yok
  (doğrusu `cacheTime`/`cacheUnit`).
- Dizilla v36: aynı desen, tld adayları (la/org/co/tv/net), `/dizi/` içerik kontrolü,
  probe interceptorsuz (CF challenge gelirse aday elenir, hepsi elenirse ilk aday kalır).
- README.md upstream template'ten gerçek 32 sağlayıcılı repo readme'ine çevrildi
  (kurulum URL'i `builds` dalı + durum göstergeleri + geliştirici komutları). CI `*.md`'yi
  yok sayar, tetiklenmez.
- DiziBOX → DiziBox klasör adı Windows diskine düzeltildi (git index zaten doğruydu).
- Build + `makePluginsJson` temiz, 32 kayıt. **Push onayı bekliyor** (3 commit).

## 2026-11-06 — 32 sağlayıcıya inildi, DiziMom/Watch2Movies yenilendi

- SinemaCX, NetflixMirror, UgurFilm, WebteIzle modülleri tamamen silindi (36 → 32).
- RecTV v4, `status = 0` (API Cloudflare Access arkasında; `/api` 404/401).
- DiziMom v3: `ddizimom.net` Tailwind teması, pilavyer oynatıcı, ld+json detay,
  `?page=N` sayfalama. `DzmFirePlayer` kaldırıldı.
- Watch2Movies v3 (beta): `movies2watch.watch`, `h3.film-name a` kartları,
  `pl_url` → sunucu listesi → `W2mExtractor` hostları yeni alan adlarıyla.
- Build + `makePluginsJson` temiz, 32 kayıt.
- Push: `fork/master` `be9b4d7..3dfd715`; CI run `42248902009` **success**.
- `builds` branch doğrulaması: `plugins.json` 32 kayıt, 32 `.cs3` dosyası 200;
  silinen dört modülün artifact'ları (SinemaCX, NetflixMirror, UgurFilm, WebteIzle) 404.

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

## Status at last review (2026-11-15, HEAD `534ce24`)

- 32 sağlayıcı; DiziPal 43, CizgiMax 4 (dzen/tau-video), Dizilla 35, SelcukFlix 9.
- HEAD == `fork/master` (pushed, CI `43734774890` success); `builds` verified: 32 modules.

## Previous review (2026-10-29, HEAD `dc598d9`, push edildi + CI green)

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

## 2026-11-06 — bağlantı denetimi

- 36 sağlayıcının tamamı prob edildi; 26 sağlıklı, 4 kısmen, 5 ölü.
- Dizilla `dizilla.la` (v35), DiziPal `dizipal1594.com` (v42) — yeni alan adları + Dizilla
  için RSC `seasonUrls` bölüm ayrıştırması.
- RecTV `status = 1` yapıldı (v3) ama API Cloudflare Access arkasında, çalışmıyor.
- `./gradlew make makePluginsJson` → 36 sağlayıcı, build temiz.
