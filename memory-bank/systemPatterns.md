# System Patterns — TestPlugins

## Build architecture

```
settings.gradle.kts  ── auto-includes every root dir that has build.gradle.kts
        │               (disabled = listOf<String>() hides a module)
        ▼
build.gradle.kts (root)  ── applies to ALL subprojects:
        com.android.library + kotlin-android + com.lagradost.cloudstream3.gradle
        minSdk 21 / compileSdk 35 / targetSdk 35 / jvmTarget 1.8
        dependencies: kotlin-stdlib, NiceHttp 0.4.11, jsoup 1.18.3,
                      jackson-module-kotlin 2.13.1, jspecify 1.0.0
        ▼
<Module>/build.gradle.kts  ── version + cloudstream { authors, language,
                               description, status, tvTypes, iconUrl }
<Module>/src/main/kotlin/com/panates/*.kt
```

- `gradle/libs/recloudstream-gradle-plugin.jar` is **vendored and must stay committed** —
  JitPack `-SNAPSHOT` resolution for the upstream plugin is broken (documented in root
  `build.gradle.kts`).
- The `cloudstream` configuration resolves `com.lagradost:cloudstream3:pre-release` stubs.
- Packaging: `./gradlew make makePluginsJson` → `<Module>/build/<Module>.cs3` + `build/plugins.json`.
  A `.cs3` contains only `manifest.json` + `classes.dex`; external deps are provided by the app.

## Code conventions

- **All modules share the flat package `com.panates`** — so top-level class names must be
  unique across the whole repo (e.g. a `SearchItem` model in two modules would collide).
  Prefix model classes per source: `DzpSearchItem`, `DzenSez`, …
- Entry point: `@CloudstreamPlugin class <Name>Plugin : Plugin()` calling
  `registerMainAPI(...)` / `registerExtractorAPI(...)` in `load()`.
  Exception: SezonlukDizi's entry class is named `PanatesPlugin`.
- Provider classes implement `MainAPI` (`name`, `mainUrl`, `hasMainPage`, `search`,
  `latestRequests`, `load`), returning `newMovieLoadResponse` / `newTvSeriesLoadResponse`.

## Extractor / playback pattern

- `registerExtractorAPI` is a **global registry**: any installed plugin's extractor is
  callable from any other plugin via `loadExtractor("ClassName")`. This creates hidden
  cross-plugin dependencies — bundle an extractor in the same module if that module's core
  playback needs it (precedent: SezonlukDizi v11 used InatBox's `Dzen`, v12 bundled its own
  `DzenSez` to be self-contained).
- Standard flow: page HTML → find player embed URL → `loadExtractor(url, referer, subtitleCallback)`
  → `newExtractorLink(...)` with `ExtractorLinkType` and m3u8/mp4 URL.
- Subtitles: parse player `Tracks` JSON / page script for `.vtt`; keep **absolute** URLs.
- Obfuscation: HDFilmCehennemi needs a splice-family decoder for packed player scripts.

## Error-handling pattern

Prefer null-safety and isolation: `?: emptyList()`, `orEmpty()`, `runCatching` per source
link, and per-item `continue` so one bad item never fails a whole page load.
