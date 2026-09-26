# AGENTS.md — TestPlugins (Cloudstream plugin repo)

Cloudstream3 plugin monorepo. Each plugin = one Gradle module producing a `.cs3` artifact, published to the `builds` branch (`plugins.json` + `*.cs3`).

## Workflow rules (user-mandated)

- **Never push without user approval.** Commit freely; accumulate changes, commit locally, build locally, then batch-push only when the user approves.
- Test every change locally with gradlew (build success = test; there are no unit tests).
- Push target is the `fork` remote (mustafa-tufekci/TestPlugins), branch `master`. `origin` = upstream recloudstream/TestPlugins — do NOT push there.
- Bump `version = N` (top line of the module's `build.gradle.kts`) for every change meant for users — no version bump, no update.
- Commit style: `feat(<module>): … (vN)` / `fix(<module>): … (vN)`.

## Commands

```sh
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export ANDROID_HOME=$HOME/Android/Sdk

./gradlew :Dizilla:assembleDebug      # single module compile check
./gradlew make makePluginsJson        # all artifacts + build/plugins.json
```

- Outputs: `<Module>/build/<Module>.cs3` + `build/plugins.json` (both gitignored via `**/build` — never commit).
- CI (`.github/workflows/build.yml`): push to `master` (ignores `*.md`) → runs the same `./gradlew make makePluginsJson`, wipes `builds/*.cs3`, copies fresh artifacts + plugins.json, force-pushes `builds`.
- After pushing: `gh run watch --exit-status $(gh run list --limit 1 --json databaseId --jq '.[0].databaseId')`, then verify the entry in `plugins.json` on the `builds` branch.

## Architecture

- `settings.gradle.kts` auto-includes **any** root dir containing a `build.gradle.kts` — no registration needed. To hide a module, add its name to the `disabled` list.
- Root `build.gradle.kts` applies `com.android.library` + `kotlin-android` + `com.lagradost.cloudstream3.gradle` to all subprojects: minSdk 21, compileSdk 35, **jvmTarget 1.8** (required by the cloudstream plugin).
- Every module: `<Name>/build.gradle.kts` (`version` + `cloudstream { authors, language, description, status, tvTypes, iconUrl }`) and `src/main/kotlin/com/panates/*.kt` — **all modules share the flat package `com.panates`**. Entry point is `@CloudstreamPlugin class <Name>Plugin : Plugin()` calling `registerMainAPI(...)` / `registerExtractorAPI(...)` in `load()`.
- `gradle/libs/recloudstream-gradle-plugin.jar` is **vendored and must stay committed** — JitPack `-SNAPSHOT` resolution for the upstream plugin is broken (see comment in root `build.gradle.kts`).
- `.cs3` contains only `manifest.json` + `classes.dex`; external `implementation` deps are provided by the app, not bundled.
- `README.md` is upstream template boilerplate (mentions a nonexistent `ExampleProvider`) — trust gradle files instead.

## Compile-time gotchas (all hit before, will hit again)

- **Never use top-level `tryParseJson`** — it's inline from cloudstream.jar built with JVM 11, fails under jvmTarget 1.8 ("Cannot inline bytecode built with JVM target 11…"). Use `AppUtils.tryParseJson` (non-inline) or Jackson `ObjectMapper().readValue` (`jackson-module-kotlin` is on the classpath). org.json is also available.
- **Jackson must stay 2.13.1** (root declares it) — newer breaks old Android devices.
- **kotlinx-coroutines is NOT on the compile classpath** by default. If a module needs `Mutex`/`withContext`/`delay`, add `implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")` to that module's `build.gradle.kts` (see InatBox).
- `fixUrl`/`fixUrlNull` need an explicit `import com.lagradost.cloudstream3.fixUrl` — the `utils.*` star import does not provide them.
- Nullable types don't accept `.ifBlank` directly — normalize with `?.trim().orEmpty()` first.
- Available helper APIs (verified against `~/.gradle/caches/cloudstream/cloudstream/cloudstream.jar`): `Score.from10`, `parsedSafe`, `mainPageOf`, `newTvSeriesLoadResponse`, `newExtractorLink`, `loadExtractor`, `getAndUnpack`, `base64Decode`, NiceHttp `post(data = mapOf(...))`. Distrust docs; confirm with `javap -cp <jar> <class>`.

## Extractor registry is global

`registerExtractorAPI` in any *installed* plugin is callable from every other plugin via `loadExtractor` by class name. Cross-plugin reuse works but creates a hidden dependency (SezonlukDizi v11 used InatBox's `Dzen`, then v12 bundled its own `DzenSez` to be self-contained). Rule of thumb: if a source's core playback needs an extractor, bundle it in the same module; register-only for shared/optional hosts.

## Porting/adding a provider (house pattern)

1. Verify the site is actually live (`curl -L` with a browser UA) and trace the load/stream flow before writing code. `status = 1` in `cloudstream {}` **only** if a full flow was verified; otherwise 3 (beta) or 0 (down).
2. Copy the reference source into the module, rename package to `com.panates`, keep class names unless they collide with another module's top-level class (models like `SearchItem` must be unique *across modules* since the package is shared — prefix them, e.g. `DzpSearchItem`).
3. Prefer null-safe code (`?: emptyList()`, `orEmpty()`), `runCatching` per source link, and per-item `continue` over whole-load failure.
4. Build the module, then `make makePluginsJson`, commit locally, and wait for push approval.
