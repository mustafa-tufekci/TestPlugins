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

37 modules (all `makePluginsJson`-built). Status: 1=Ok, 3=Beta.

| Module | Version | Status | Notes |
|---|---|---|---|
| Animecix | 8 | 1 | animecix.tv secure API, Tau Video + Sibnet links |
| Anizium | 3 | 3 | Anime API (`api.anizium.co` + `Cf-Control`), genre names + epoch years |
| BelesTivi | 7 | 1 | Live TV from the Beleş TiVi app; dual M3U + 10 Turkuvaz channels, `quickSearch` |
| BelgeselX | 4 | 3 | belgeselx.com docs; v4 adds ok.ru/odnoklassniki extractors |
| CanliTV | 7 | 1 | Live TV & sports; v7 adds runtime iptv-org `tr.m3u` rows + `#EXTVLCOPT` headers |
| CizgiMax | 3 | 3 | cizgimax.online; v3 adds SibNet/Drive/CizgiDuo/CizgiPass extractors |
| DiziBal | 4 | 1 | dizibal.org; v4 platform/genre rows + JSON-LD metadata |
| DiziBox | 1 | 3 | dizibox.live (ported from neowatch) |
| DiziFilm | 5 | 1 | dizifilmizle.to Next.js RSC flight, vidmixi.com bePlayer |
| DiziKorea | 1 | 3 | dizikorea3.com + FirePlayer (neowatch port) |
| Dizilla | 32 | 1 | dizilla.now JSON-LD; v32 actor roles, episode dates, runtime, genres |
| DiziMom | 1 | 3 | dizidad.com + FirePlayer (neowatch port) |
| DiziPal | 38 | 1 | dizipal1592.com RSC/PBKDF2; v38 PalPlayer/StreamCore/VideoPlays + data-rm-k |
| DiziYou | 1 | 3 | diziyou3.com (neowatch port) |
| FilmMakinesi | 1 | 3 | filmmakinesi.to, Rhino/PACKER JS player |
| FilmModu | 1 | 3 | filmmodu.news + Pilavyer (neowatch port) |
| FullHDFilm | 4 | 1 | fullhdfilmizlesene.now; v4 adds TurboImgz extractor |
| HDFilmCehennemi | 38 | 1 | v38 generic Rhino/PACKER fallback decoder |
| HDFilmCehennemi2 | 13 | 1 | hdfilmcehennemi2.biz, vidload JSON config |
| InatBox | 17 | 1 | v17 403 `x-st` clock-skew retry; CF IP ban is external |
| JetFilmizle | 1 | 3 | jetfilmizle.io + PixelDrain (neowatch port) |
| KoreanTurk | 1 | 3 | koreanturk.tv (neowatch port) |
| KultFilmler | 1 | 3 | kultfilmler.net + YildizKisaFilm (neowatch port) |
| NetflixMirror | 1 | 3 | iosmirror.cc + PrimeVideoMirror (neowatch port) |
| RareFilmm | 1 | 3 | rarefilmm.com + ok.ru family (neowatch port) |
| RecTV | 1 | 3 | psrectv (runtime mainUrl) (neowatch port) |
| SelcukFlix | 11 | 3 | selcukflix.app encrypted API, 5 rows, 5 `Slc*` extractors |
| SetFilmIzle | 1 | 3 | setfilmizle.ltd + SetPlay (neowatch port) |
| SezonlukDizi | 15 | 1 | DzenSez + FilemoonSez (Byse PoW + AES-GCM) |
| SinemaCX | 1 | 3 | sinema.cx (neowatch port) |
| SuperFilmGeldi | 1 | 3 | superfilmizle.org + MixPlayHD/MixTiger (neowatch port) |
| TurkAnime | 1 | 3 | turkanime.co (neowatch port) |
| UgurFilm | 1 | 3 | ugurfilm8.com + MailRu/Odnoklassniki (neowatch port) |
| Watch2Movies | 1 | 3 | watch2movies.net + W2MExtractor (neowatch port) |
| WebDramaTurkey | 1 | 3 | webdramaturkey2.com + FirePlayer (neowatch port) |
| WebteIzle | 1 | 3 | webteizle.click (neowatch port) |
| Youtube | 2 | 1 | NewPipe-based, Turkish localization, channels/playlists/live |
