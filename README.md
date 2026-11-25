# TestPlugins — Cloudstream Türkçe Eklenti Deposu

[Cloudstream](https://github.com/recloudstream/cloudstream) için Türkçe film/dizi/canlı yayın sağlayıcıları üreten eklenti monoreposu. Her modül ayrı bir `.cs3` eklentisi olarak derlenir ve `builds` dalında yayınlanır.

> **Uyarı:** Depo aktif geliştirme altındadır; kaynak siteler sık sık tasarım/alan adı değiştirdiği için eklentiler de sık güncellenir.

## Kurulum

1. Cloudstream'i açın: **Ayarlar → Eklentiler → Depo ekle**
2. Repo adresi olarak şunu girin:

   ```
   https://raw.githubusercontent.com/mustafa-tufekci/TestPlugins/builds/plugins.json
   ```

3. Listeden istediğiniz sağlayıcıları kurun. Yeni sürümler `builds` dalına otomatik düşer, uygulama güncellemeyi kendisi algılar.

## Sağlayıcılar (31)

Durum göstergeleri eklenti manifest'indeki `status` alanını yansıtır:
✅ = tam akış doğrulandı · 🧪 = beta, oynatma testi sürüyor · ⛔ = geçici olarak devre dışı

| Sağlayıcı | Kaynak | Durum |
|---|---|---|
| Animecix | animecix.tv | ✅ |
| Anizium | anizium.co | 🧪 |
| BelgeselX | belgeselx.com | 🧪 |
| CanliTV | iptv-org TR listesi + canlı kanallar | ✅ |
| CizgiMax | cizgimax.online | 🧪 |
| DiziBal | dizibal.org | ✅ |
| DiziBox | dizibox.live | 🧪 |
| DiziFilm | dizifilmizle.to | ✅ |
| DiziKorea | dizikorea3.com | 🧪 |
| Dizilla | dizilla.la *(alan adı otomatik döner)* | ✅ |
| DiziPal | dizipal159x.com *(alan adı otomatik döner)* | ✅ |
| DiziYou | diziyou.net | ✅ |
| FilmMakinesi | filmmakinesi.to | 🧪 |
| FilmModu | filmmodu.news | 🧪 |
| FullHDFilm | fullhdfilmizlesene.now | ✅ |
| HDFilmCehennemi | hdfilmcehennemi.nl | ✅ |
| HDFilmCehennemi2 | hdfilmcehennemi2.biz | ✅ |
| InatBox | diziboxen.help | ✅ |
| JetFilmizle | jetfilmizle.app | 🧪 |
| KoreanTurk | koreanturk.tv | 🧪 |
| KultFilmler | kultfilmler.net | 🧪 |
| RareFilmm | rarefilmm.com | 🧪 |
| RecTV | psrectv | ⛔ (API erişimi kapalı) |
| SelcukFlix | selcukflix.app | 🧪 |
| SetFilmIzle | setfilmizle.ltd | 🧪 |
| SezonlukDizi | sezonlukdizi.cc | ✅ |
| SuperFilmGeldi | superfilmizle.icu | 🧪 |
| TurkAnime | turkanime.live | ✅ |
| Watch2Movies | movies2watch.watch | 🧪 |
| WebDramaTurkey | webdramaturkey2.com | 🧪 |
| Youtube | youtube.com (TR yerelleştirmeli) | ✅ |

## Geliştiriciler için

Her modül bir Gradle alt projesidir (ayar kaydı gerekmez; kök dizinde `build.gradle.kts` olan her klasör otomatik dahil olur). Tüm sınıflar paylaşımlı `com.panates` paketindedir; bu yüzden modüller arası top-level sınıf adları benzersiz olmalıdır.

```sh
# Windows
.\gradlew.bat :Dizilla:assembleDebug        # tek modül derleme kontrolü
.\gradlew.bat make makePluginsJson          # tüm .cs3'ler + build/plugins.json

# Linux / Mac
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export ANDROID_HOME=$HOME/Android/Sdk
./gradlew make makePluginsJson
```

- Çıktılar `<Modül>/build/<Modül>.cs3` ve `build/plugins.json` — ikisi de `.gitignore`'ladır, commit edilmez.
- `master`'a push, CI'ı tetikler; CI tüm eklentileri derleyip `builds` dalına `.cs3` + `plugins.json` olarak zorla iter.
- Kullanıcıya yönelik her değişiklikte modülün `build.gradle.kts` içindeki `version` sayısı artırılır; sürüm artırılmadan güncelleme yayınlanmaz.
- `status` alanı: `1` = doğrulanmış, `3` = beta, `0` = kapalı, `2` = yavaş.
- Önemli mimari notlar ve derleme tuzakları için bkz. [`AGENTS.md`](AGENTS.md) ve [`memory-bank/`](memory-bank/).

## Sorumluluk reddi

Bu eklentiler hiçbir içeriği barındırmaz; halka açık web sitelerindeki bağlantıları ayrıştırıp oynatıcıya iletir. İlgili sitelerin içerik politikalarından bu depo sorumlu değildir.
