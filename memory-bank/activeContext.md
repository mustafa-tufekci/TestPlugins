# Active Context — TestPlugins

## Current focus

**neowatch karşılaştırması tamamlandı ve yayınlandı (2026-10-29).** Referans repo
`neoser1984/neowatch` incelendi, 13 ortak modül karşılaştırıldı, eksikler kapatıldı:
20 yeni sağlayıcı modülü eklendi + 9 özellik portu yapıldı. `fork/master`'a push edildi
(31 commit, `418a8e1..dc598d9`), CI run `40906447013` **success**, `builds/plugins.json`
37 sağlayıcı ile doğrulandı. Açık iş: 20 beta modülün (`status = 3`) canlı oynatma
doğrulaması → `status = 1`.

## Recent changes — neowatch portu (2026-10-29)

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

- HEAD = `dc598d9` (memory bank senkronu). `fork/master` = `dc598d9` → **push edildi**,
  CI run `40906447013` **success**; `builds` branch'inde 37 sağlayıcı doğrulandı
  (Dizilla v32, DiziPal v38, HDFilmCehennemi v38, InatBox v17, CanliTV v7, DiziBal v4,
  BelgeselX v4, CizgiMax v3, FullHDFilm v4, 20 yeni modül v1).
- `./gradlew make makePluginsJson` → **BUILD SUCCESSFUL**, 37 sağlayıcı üretildi
  (`build/plugins.json`).
- Working tree temiz. Memory bank bu duruma göre güncellendi (2026-10-29).
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