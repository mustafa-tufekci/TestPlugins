# Active Context — TestPlugins

## Current focus

**Bozuk kaynak onarım turu (2026-10-29/30) — kod tamam, push onayı bekliyor.**
Kullanıcı istekleri: BelesTivi'yi tamamen kaldır, CanliTV'yi neowatch'in birebir kopyası
yap, neowatch'te afişlerin neden daha düzgün olduğunu bul ve tüm repoya uygula, DiziBal /
DiziBox / DiziMom'u düzelt, "link bulunamadı" veren Dizilla, FilmMakinesi, FilmModu,
FullHDFilm, HDFilmCehennemi, SelcukFlix, SetFilmIzle'yi düzelt, DiziPal şema hatasını
çöz, ölü kaynakları (DiziYou, JetFilmizle, KultFilmler, SinemaCX, SuperFilmGeldi,
TurkAnime, UgurFilm, WebDramaTurkey, WebteIzle) canlandır.

Sonuç: 5 commit yerel (`cd352e5`, `958a3da`, `335666d`, `9947727`, `10d58c9`+`4d13691`,
`c671a52`), `./gradlew make makePluginsJson` **BUILD SUCCESSFUL**, `build/plugins.json`
**36 sağlayıcı**. Push edilmedi — kullanıcı onayı bekleniyor.

## 2026-10-30 turu — kök nedenler

- **ContentX ailesi (Dizilla/SelcukFlix/DiziPal):** oynatıcı `window.openPlayer('...')`
  yerine tek bir `window.NP_CONFIG = {...}` JSON'u basıyor; eski ayrıştırıcı
  `iExtract is null` atıyordu → "link bulunamadı". Üç modüle NP_CONFIG dalı eklendi
  (`sources[].master`, `subtitles[].url`, `track`). Referer **tam olarak oynatıcı
  origin'i** olmalı (`scheme://host/`) ve tarayıcı UA şart: referer yok / contentx.me /
  sorgulu iframe URL'si → 404; origin → 200.
- **Pilavyer (DiziBal/FilmModu):** host `play2.pilavyerplay.top` → `pilavyerplay.top`;
  `api/stream.php` + `api/sub.php` jeton uçları; altyazılara Referer/UA başlığı eklendi.
  Zincir referersiz de 200 dönüyor, yani DiziBal'ın "video oynamıyor" şikayeti
  seçici/akış kaynaklı değildi — yine de sağlamlaştırıldı (v6).
- **FilmMakinesi + HDFilmCehennemi (asıl büyük kazanç):** P.A.C.K.E.R. JS'i Rhino'da
  çalıştıran `GizliJs` çözücülerinin polyfill'i yalnızca `window/self/document={}` +
  atob/btoa tanımlıyordu. Yeni paketler `self.location.hostname`, `document.createElement`,
  `screen`, `navigator` çağırıyor → değerlendirme patlıyor, akış değişkeni hiç
  tanımlanmıyordu. Tam DOM tarayıcı taklidi eklendi (`location`, `screen`, `navigator`,
  `document` + `__sahteEleman()`); `degiskenCoz(html, degisken, host)` host alıyor.
  Doğrulandı: FilmMakinesi → rapidrame master m3u8; HDFilmCehennemi → cdnimages master
  (TR/Original ses).
- **Afişler (kullanıcının "resimler düzgün değil" şikayeti):** neowatch ile kod farkı
  yok; fark **görsel adreslerinde**. Siteler listede 16:9 küçük kapak / platform logosu /
  WordPress kırpıntısı veriyor. Çözüm: her modüle kendi `<Önek>Resim` yardımcısı
  (ortak `com.panates` paketinde sınıf adı çakışması olmasın diye modül başına ayrı ad):
  kart içinde en uygun `img`'i puanlar (poster/afis/kapak + dikey oran ×; logo/brand/
  platforms/backdrop −), `data-src`/`data-lazy-src`/`data-original`, en büyük
  `srcset`/`data-srcset` varyantını seçer ve adresi büyütür. Doğrulanan büyütmeler
  (hepsi 200, 2–6 kat büyük dosya):
  `macellan/cdnhipter /images/640/360|0/360/90/` → `/images/630/910/95/` (2:3 afiş),
  `dizibal /storage/tmdb/posters/w342/` → `w780`, TMDB `/t/p/w300|w185/` → `w500`,
  WordPress `-200x300` → orijinal, `fullhdfilm /poster/film/` → `poster/film-lg/`,
  `hdfilmcehennemi ...poster/x.webp` → `@2x`. 16 modüle uygulandı, `c671a52`.
- **Ölü kaynaklar:** canlandırılanlar — WebteIzle `webteizle.info`, SuperFilmGeldi
  `superfilmizle.icu` (oynatıcı `luciferplays.com`), JetFilmizle `jetfilmizle.app`
  (playerx HLS), KultFilmler `kultfilmler.net` (yeni tema + vidpapi FirePlayer),
  TurkAnime `turkanime.live` (StreamCore `play.m3u8`), DiziYou `diziyou.net`
  (WP/Wstream + NP_CONFIG), WebDramaTurkey yalnızca FirePlayer hostu `dtpasn.asia`.
  Kalıcı ölü: **DiziMom, SinemaCX, UgurFilm** → `status = 0` (tüm alan adları 000/parked).
  WebteIzle oynatması Cloudflare Turnstile + sunucu tarafı doğrulama ile kalıcı kapalı
  (liste/detay çalışıyor).

## Önceki tur — neowatch portu (2026-10-29)


Özellik portları (mevcut modüllerin sürüm bump'ları):

- `fix(InatBox): 403 x-st saat farki tekrar denemesi eklendi (v17)` — `9842798`
- `feat(Dizilla): oyuncu rolu, bolum tarihi, sure ve tur metadata'si eklendi (v32)` — `0f0964f`
- `feat(HDFilmCehennemi): genel Rhino/PACKER JS oynatici cozucusu eklendi (v38)` — `2be1285`
- `feat(DiziPal): PalPlayer/StreamCore/VideoPlays decoder'lari ve data-rm-k cozucusu eklendi (v38)` — `e3e02f7`
- `feat(CanliTV): iptv-org canli liste, EXTVLCOPT headers ve oneri satiri eklendi (v7)` — `e555c7a`
- `feat(DiziBal): platform/tur satirlari, sayfalama ve JSON-LD metadata eklendi (v4)` — `519ab50`
- `feat(FullHDFilm): TurboImgz extractor eklendi (v4)` — `34b6525`
- `feat(CizgiMax): SibNet/Drive/CizgiDuo/CizgiPass extractor'lari eklendi (v3)` — `644065b`
- `feat(BelgeselX): ok.ru/odnoklassniki extractor eklendi (v4)` — `2fcfa61`

Yeni modüller (hepsi `version = 1`, `status = 3`, tek commit/modül):
`993cfe6` DiziBox · `1a0812d` DiziKorea · `da562b2` DiziMom · `13fae2c` DiziYou ·
`8c3cb5b` FilmMakinesi · `3c4e2ae` FilmModu · `9d2b0b3` JetFilmizle · `5b8c50b` KoreanTurk ·
`a251358` KultFilmler · `4b79962` NetflixMirror · `5c0cd48` RareFilmm · `397dac8` RecTV ·
`2d0ff40` SetFilmIzle · `faa9ba8` SinemaCX · `b04ca55` SuperFilmGeldi · `6705723` TurkAnime ·
`9afbd55` UgurFilm · `98a6873` Watch2Movies · `8c159df` WebDramaTurkey · `f118ef1` WebteIzle.

Daha önceki akış (2026-10-25): BelesTivi v1→v7, CanliTV v5→v6.

## State

- Yerel `master` = `c671a52` (bu turun son commit'i). `fork/master` hâlâ `dc598d9`;
  **bu turun 6 commit'i push EDİLMEDİ** — kullanıcı onayı bekleniyor.
- `./gradlew make makePluginsJson` → **BUILD SUCCESSFUL**, **36 sağlayıcı**
  (BelesTivi kaldırıldığı için 37 → 36).
- Bu turdaki sürümler: CanliTV 8, Dizilla 34, SelcukFlix 13, DiziPal 40, DiziBal 6,
  FilmModu 3, FilmMakinesi 3, HDFilmCehennemi 40, FullHDFilm 5, SetFilmIzle 2,
  WebteIzle 3, SuperFilmGeldi 3, WebDramaTurkey 2, JetFilmizle 3, KultFilmler 3,
  TurkAnime 3, DiziYou 3, DiziBox 2, DiziMom/SinemaCX/UgurFilm 2 (`status = 0`).
- `origin/master` (upstream) ilgisiz ve bilinçli olarak senkronize edilmiyor.

## neowatch portu — kararlar ve tuzaklar

- **Kanıtlanmış kazançlar:** CanliTV artık runtime `iptv-org` listesi okuyor (~1900 satır
  gömülü kanal bayatlıyordu), DiziBal 13 satır + sayfalama + JSON-LD metadata, Dizilla
  oyuncu rolü/bolum tarihi/süre/tür, DiziPal 3 yeni decoder, HDFilmCehennemi genel JS
  çözücü, CizgiMax/FullHDFilm/BelgeselX eksik host'lar.
- **Bilinçli olarak port edilmeyen: InatBox dinamik dizin** (`InatIstek` / mtlshash cert).
  `https://raw.githubusercontent.com/mtlshash/cert/main/hash` AES-CBC ile açılıyor ama ilk
  16 byte (IV) kurtarılamıyor; neowatch'in kendi `makulMu()` kontrolü de bu çıktıyı
  reddeder. Statik yedek `ctls.js` sadece `test` içeriyor. Onun yerine 403 `x-st` saat
  farkı düzeltmesi (gerçek protokol davranışı) uygulandı.
- **Rhino iki modülde gerekli:** FilmMakinesi + HDFilmCehennemi `compileOnly` ile
  `org.mozilla:rhino:1.8.1` (app sağlıyor, jar cache'te yok).
- **Ortak `JsonOku` yardımcısı** her modülde ayrı isimle çoğaltıldı (`RfmJsonOku`,
  `WtzJsonOku`, `FmdJsonOku`) — paket `com.panates` ortak olduğu için aksi halde çakışırdı.
- Yeni modüllerin hiçbiri canlı uçtan uca doğrulanmadı → hepsi `status = 3`.

## Next steps

- ~~Push onayı bekle~~ → **push edildi** (`418a8e1..dc598d9`, 31 commit), CI
  `40906447013` success, `builds/plugins.json` 37 kayıtla doğrulandı (2026-10-29).
- 20 yeni modülü tek tek canlı doğrula (load → loadLinks), çalışanlara `status = 1` ver.
- BelesTivi v7 hâlâ uçtan uca yeniden denenmedi (2026-10-24 taramasından sonra eklendi).
- Bank'ı her anlamlı değişiklikten sonra güncelle (`activeContext.md`, `progress.md`,
  `productContext.md` sürüm tablosu).
- hdfilmcehennemi*.biz şablonu/oynatıcısı değişirse izle.

## Patterns & preferences learned

- Kullanıcı **memory-bank disiplini** istiyor: görev başında tüm bank dosyalarını oku,
  anlamlı değişiklikten sonra güncelle, "update memory bank" tetikleyicisinde hepsini gözden
  geçir.
- Kısa commit başlığı, sürüm numarası ve modül adı parantez içinde.
- Cross-plugin extractor bağımlılığı yerine kendi kendine yeten modüller.
- `status = 1` yalnızca gerçek site akışı uçtan uca doğrulandıysa.
- Push asla onaysız yapılmaz; commit serbest, push toplu.

## Insights

- `settings.gradle.kts` otomatik dahil etme: yeni modül için sadece `build.gradle.kts`
  olan bir dizin yeterli.
- Tüm modüller `com.panates` paketini paylaştığı için yeni bir top-level sınıf *başka* bir
  modülün derlemesini bozabilir; yeni tip ekledikten sonra tam `./gradlew make` şart.
- Extractor kayıt defteri global: `registerExtractorAPI` ile kaydedilen bir extractor,
  kurulu herhangi bir eklentiden `loadExtractor(url)` ile erişilebilir. Bir modülün
  kendi oynatması buna bağlıysa extractor'ı kendi içinde barındır.
- Aynı `mainUrl` öneki ile iki extractor kaydetmek çakışır — bu yüzden
  `HdcGizliJsOynatici` kaydedilmez, yalnızca doğrudan çağrılır.

## Tarihsel notlar (önceki oturumlar)

- **DiziPal SSL (2026-09-28):** `CertPathValidatorException` cihaz tarafındaydı (BTK/DNS
  hijack veya yerel TLS aracı); origin zinciri sağlamdı. Kanonik host `dizipal1592.com`.
- **DiziBOX (2026-09-28/29):** Cloudflare managed challenge tespiti genişletildi; AJAX
  `admin-ajax.php?action=dwls_search` araması eklendi. Modül sonradan kullanıcı isteğiyle
  tamamen kaldırıldı, neowatch portuyla **DiziBox v1** olarak geri geldi.
- **SelcukFlix (v1→v11):** Kekik referansından port; v2 tüm kaynaklar, v3 nik-style
  kategori ana sayfası, v4 ana sayfa 29 → 5 satır, v11 aramada `used_type`.
- **CloudStreamHub karşılaştırması (2026-10-19):** Emre-Kahveci/CloudStreamHub; BelgeselX
  seçicileri ölü, CizgiMax rotaları 404, googlevideo'ya Referer gönderiyor. Bizim sürümler
  prob edilen tüm kriterlerde doğru. Ödünç alınabilir: StreamValidator, BoundedParallelResolver.
## 2026-10-29 push öncesi çalışırlık denetimi (36 kaynak)

Kullanıcı isteği: "pushlamadan önce tüm kaynaklari kontrol et hangileri çalişiyor".
Her modülün `mainUrl`/API ucu canlı prob edildi (curl + Chrome/Firefox UA, gerekli
`X-Requested-With`/`Referer`/`Cf-Control` başlıklarıyla) ve liste seçicileri
BeautifulSoup ile modülün kendi CSS seçicisi kullanılarak sayıldı.

**Bu turda bulunan gerçek kırık: HDFilmCehennemi ana sayfa.** Site yeniden yazılmış;
`/load/page/sayfano/...` AJAX uç noktaları ve `data-pages` sayfalaması sunucuda **404**.
Yeni yapı sunucu tarafında render: `a.poster[href][title][data-token]` >
`div.poster-wrapper > img[src|srcset @2x]`, `div.poster-meta` (yıl | süre | puan).
Düzeltme (v41): `mainPage` 20 hazır rota (`/`, `/category/*`, `/dil/*`, `/tur/*`),
`getMainPage` `a.poster[href]` seçer, `page > 1` boş döner (site sayfalamıyor),
`search` `/search/?q=` (sondaki `/` 301'i kaldırır; JSON `{query,results}` aynı kaldı).
`/tur/korku-filmleri-izle-9/` 404 → doğrusu `/tur/korku-filmlerini-izle-9/`.

Ayrıca kapatılanlar: **RecTV** (Cloudflare Access "Sign in"), **NetflixMirror**
(`iosmirror.cc` DNS yok), **Watch2Movies** (`watch2movies.net` DNS yok) → `status = 0`.
Zaten kapalıydı: DiziMom, SinemaCX, UgurFilm.

Doğrulanan ve çalışan (spot): Animecix (`/secure/homepage/lists-guests` 200/490 KB),
Anizium (`api.anizium.co/page/home` — **`Cf-Control` başlığı şart**, o olmadan 401; onunla
200 ve `special_list` 8 satır), BelgeselX, CanliTV (m3u 172 kanal), CizgiMax, DiziBal
(detayda iframe yok, pilavyer `core.js`), DiziBox (CF challenge artık yok, 15 kart),
DiziFilm (ana sayfa 5 `h2` bölümü), DiziKorea, DiziPal, DiziYou, Dizilla, FilmMakinesi,
FilmModu, FullHDFilm, HDFilmCehennemi (ana sayfa v41, oynatma zaten sağlam),
HDFilmCehennemi2, JetFilmizle, KoreanTurk, KultFilmler, RareFilmm, SelcukFlix
(`api/bg/findMovies` 200, AES'li `response` — beklenen), SetFilmIzle, SezonlukDizi
(`ajax/dataDefaultSonCikan.asp` 200), SuperFilmGeldi, TurkAnime, WebDramaTurkey
(`dtpasn.asia`), WebteIzle (liste çalışır; **oynatma Cloudflare Turnstile ile kalıcı
kapalı**), Youtube (NewPipe kiosk, ağdan doğrulanamaz), InatBox (HMAC imzalı CDN;
imzasız istek 403 — uygulama içinde doğrulanamaz, `status = 1` bırakıldı).

`./gradlew make makePluginsJson` → 36 sağlayıcı. Commit'ler yerel: `57bb5ac` (HDC v41),
`658446e` (RecTV/NetflixMirror/Watch2Movies v2). Push **yapılmadı**, onay bekleniyor.

### 2026-10-30 — takip turu (JetFilmizle Trendler, DiziPal ana sayfa, RecTV)

- **JetFilmizle v4** (`e772157`): `/trendler` sayfası klasik ızgara değil, sıralama listesi
  (`a.trend-item` > `.trend-rank` / `.trend-poster img` / `.trend-name`). `getMainPage` bu
  bölüm için ayrı ayrıştırıcı (`toTrendResult()`) kullanıyor; canlı sayfada 37 kayıttan 20'si
  film/dizi (kalanı kullanıcı sıralaması). `JtfResim.buyuk()` artık `-poster-tiny|small|medium`
  varyantlarını `-poster-large`'a yükseltiyor (8 KB → 132 KB).
- **DiziPal v41** (`7c2d24c`): ana sayfanın "Excepted url schema…" hatasının kökü
  `MainPageData(name, data)` sırasının ters yazılmasıydı (data alanına "Anasayfa" gidiyordu).
  Altı bölüm düzeltildi. "Yeni Bölümler" sayfası `a.ecl` / `a.mwr` kartları kullanıyor
  (eskisi `a.sr`), `aria-label` ("Dizi, 4. Sezon 6. Bölüm, dublaj") üzerinden başlık
  çıkarılıyor — canlı sayfada 68 bölüm.
- **RecTV v3** (`ff93326`) kullanıcı isteğiyle `status = 1`. Uyarı: `a.psrectv80.xyz` kökü
  `/admin/`'e 301 (401), imzalı `/api/...` çağrıları **404**; Firebase remote config hâlâ
  aynı tabanı veriyor, `api_secure_key` (`c.prectv30.sbs`) DNS'te yok. Liste muhtemelen boş gelir.
