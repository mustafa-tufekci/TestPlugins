# Tech Context — TestPlugins

## Stack

| Piece | Version / detail |
|---|---|
| Language | Kotlin (kotlin-gradle-plugin 2.4.0) |
| JVM target | **1.8** (required by the cloudstream plugin) |
| AGP | 8.7.3 |
| JDK | 17 (CI uses temurin 17; local `JAVA_HOME=/usr/lib/jvm/java-17-openjdk`) |
| Android | minSdk 21, compileSdk/targetSdk 35 |
| JSON | jackson-module-kotlin **2.13.1 (do not bump)** |
| HTTP | NiceHttp 0.4.11 |
| HTML | jsoup 1.18.3 |
| Null annotations | org.jspecify:jspecify:1.0.0 (Cloudstream pre-release compat) |
| ASM | 9.9.1 (classpath, used by the packaging plugin) |

## Commands

```sh
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export ANDROID_HOME=$HOME/Android/Sdk

./gradlew :Dizilla:assembleDebug     # compile one module
./gradlew make makePluginsJson       # all .cs3 + build/plugins.json
```

There are **no unit tests** — "build succeeds" is the test.

## CI (`.github/workflows/build.yml`)

On push to `master`/`main` (ignoring `*.md`): checkout repo + `builds` branch, delete old
`builds/*.cs3`, run `./gradlew make makePluginsJson`, copy every `*.cs3` and `plugins.json`
into the `builds` checkout, then `git commit --amend` + `git push --force` (the `builds`
branch is always a single amended commit). Concurrency group `build`.

Push target after a change: `fork` remote, branch `master`. Verify with
`gh run watch --exit-status $(gh run list --limit 1 --json databaseId --jq '.[0].databaseId')`,
then confirm the new entry in `plugins.json` on the `builds` branch.

## Hard constraints / gotchas

- **Never use top-level `tryParseJson`** — it is inline, built with JVM 11 bytecode, and
  fails under jvmTarget 1.8 ("Cannot inline bytecode built with JVM target 11…"). Use
  `AppUtils.tryParseJson` (non-inline) or Jackson `ObjectMapper().readValue`.
- `fixUrl` / `fixUrlNull` need an explicit `import com.lagradost.cloudstream3.fixUrl`
  (the `utils.*` star import does not supply them).
- Nullable types reject `.ifBlank` directly — normalize with `?.trim().orEmpty()` first.
- `kotlinx-coroutines` is **not** on the compile classpath by default. Modules needing
  `Mutex`/`withContext`/`delay` must add
  `implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")` (see InatBox).
- Helper APIs available (verified against `~/.gradle/caches/cloudstream/…/cloudstream.jar`
  via `javap -cp <jar> <class>`): `Score.from10`, `parsedSafe`, `mainPageOf`,
  `newTvSeriesLoadResponse`, `newExtractorLink`, `loadExtractor`, `getAndUnpack`,
  `base64Decode`, NiceHttp `post(data = mapOf(...))`. Distrust docs; confirm with `javap`.

## Repo layout extras

- `repo.json` → points Cloudstream at
  `https://raw.githubusercontent.com/mustafa-tufekci/TestPlugins/builds/plugins.json`.
- `panates-logo.png/svg` → repo icon referenced by `repo.json`.
- `README.md` is upstream template boilerplate (mentions a nonexistent `ExampleProvider`) —
  trust the gradle files instead.
- Stray `t.vtt` / `t2.vtt` at repo root are leftovers from subtitle URL testing.
