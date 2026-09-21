package com.panates

import android.util.Base64
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.loadExtractor
import org.json.JSONObject
import java.net.URLEncoder

class FullHDFilm : MainAPI() {
    override var mainUrl = "https://www.fullhdfilmizlesene.now"
    override var name = "FullHDFilm"
    override val supportedTypes = setOf(TvType.Movie)
    override var lang = "tr"
    override val hasMainPage = true

    companion object {
        private val defaultHeaders = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            "Accept-Language" to "tr-TR,tr;q=0.9,en;q=0.8"
        )
    }

    // ── Main Page ───────────────────────────────────────────────────────

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val allPages = mutableListOf<HomePageList>()

        val categories = listOf(
            "Son Eklenen Filmler" to "$mainUrl/",
            "En Çok İzlenen Filmler" to "$mainUrl/en-cok-izlenen-filmler",
            "Aksiyon Filmleri" to "$mainUrl/filmizle/aksiyon-filmleri",
            "Bilim Kurgu Filmleri" to "$mainUrl/filmizle/bilim-kurgu-filmleri",
            "Animasyon Filmleri" to "$mainUrl/filmizle/animasyon-filmleri",
            "Komedi Filmleri" to "$mainUrl/filmizle/komedi-filmleri"
        )

        for ((title, url) in categories) {
            try {
                val doc = app.get(url, headers = defaultHeaders).document
                val items = parseFilmCards(doc)
                if (items.isNotEmpty()) {
                    allPages.add(HomePageList(title, items))
                }
            } catch (_: Exception) {}
        }

        return newHomePageResponse(allPages)
    }

    // ── Search ──────────────────────────────────────────────────────────

    override suspend fun search(query: String): List<SearchResponse> {
        val searchUrl = "$mainUrl/arama/${URLEncoder.encode(query.trim(), "UTF-8")}"

        val doc = try {
            app.get(searchUrl, headers = defaultHeaders).document
        } catch (_: Exception) {
            return emptyList()
        }

        return parseFilmCards(doc)
    }

    private fun parseFilmCards(doc: org.jsoup.nodes.Document): List<SearchResponse> {
        val filmElements = doc.select("li.film")
        return filmElements.mapNotNull { li ->
            val link = li.selectFirst("a.tt, a[href*='/film/']") ?: return@mapNotNull null
            val href = link.attr("href").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val fullUrl = fixUrl(href)

            val title = li.selectFirst("span.film-title")?.text()?.trim()
                ?: link.text().replace(Regex("""(?i)\s*izle$"""), "").trim()
            if (title.isBlank()) return@mapNotNull null

            val poster = li.selectFirst("img.afis, img")?.let { img ->
                img.attr("data-src").takeIf { it.isNotBlank() }
                    ?: img.attr("src").takeIf { it.isNotBlank() && !it.startsWith("data:") }
                    ?: li.selectFirst("source")?.attr("data-srcset")?.split(" ")?.firstOrNull()
            }?.let { fixUrl(it) }

            val year = li.selectFirst("span.film-yil")?.text()?.trim()?.toIntOrNull()
            val scoreRaw = li.selectFirst("span.imdb")?.text()?.trim()
            val score = scoreRaw?.toDoubleOrNull()

            newMovieSearchResponse(title, fullUrl, TvType.Movie) {
                this.posterUrl = poster
                this.year = year
                if (score != null) this.score = Score.from10(score)
            }
        }.distinctBy { it.url }
    }

    // ── Load Details ────────────────────────────────────────────────────

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url, headers = defaultHeaders).document

        val title = doc.selectFirst("h1")?.text()?.trim()
            ?: doc.selectFirst("meta[property='og:title']")?.attr("content")?.trim()
            ?: "FullHDFilm"

        val poster = doc.selectFirst("meta[property='og:image']")?.attr("content")?.let { fixUrl(it) }
            ?: doc.selectFirst("img.afis, .film-afis img")?.attr("src")?.let { fixUrl(it) }

        val plot = doc.selectFirst("meta[name='description']")?.attr("content")?.trim()
            ?: doc.selectFirst("div.film-ozet, p.ozet, .ozet")?.text()?.trim()

        val year = doc.selectFirst("span.film-yil")?.text()?.trim()?.toIntOrNull()
            ?: Regex("""(20\d\d|19\d\d)""").find(doc.text())?.groupValues?.get(1)?.toIntOrNull()

        val score = doc.selectFirst("span.imdb, span.puan, .imdb-puani")?.text()?.let {
            Regex("""(\d+(?:\.\d+)?)""").find(it)?.groupValues?.get(1)?.toDoubleOrNull()
        }

        val tags = doc.select("a[href*='/filmizle/']").mapNotNull {
            it.text().trim().takeIf { t -> t.isNotBlank() }
        }.distinct()

        val episodes = listOf(
            newEpisode(url) {
                this.name = title
                this.season = 1
                this.episode = 1
                this.posterUrl = poster
            }
        )

        return newMovieLoadResponse(title, url, TvType.Movie, episodes) {
            this.posterUrl = poster
            this.plot = plot
            this.year = year
            this.tags = tags
            if (score != null) this.score = Score.from10(score)
        }
    }

    // ── Load Links ──────────────────────────────────────────────────────

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val html = try {
            app.get(data, headers = defaultHeaders).text
        } catch (_: Exception) {
            return false
        }

        var found = false

        // 1. Parse scx = {...};
        val scxMatch = Regex("""scx\s*=\s*(\{[\s\S]*?\});""").find(html)
        if (scxMatch != null) {
            try {
                val scxJson = JSONObject(scxMatch.groupValues[1])
                val keys = scxJson.keys()

                while (keys.hasNext()) {
                    val key = keys.next()
                    val keyObj = scxJson.optJSONObject(key) ?: continue
                    val sxObj = keyObj.optJSONObject("sx") ?: continue

                    // t can be JSONArray or JSONObject
                    val tArr = sxObj.optJSONArray("t")
                    if (tArr != null) {
                        for (i in 0 until tArr.length()) {
                            val enc = tArr.optString(i)
                            val embedUrl = decodeScxLink(enc) ?: continue
                            found = extractEmbed(embedUrl, data, key, subtitleCallback, callback) || found
                        }
                    } else {
                        val tObj = sxObj.optJSONObject("t")
                        if (tObj != null) {
                            val subKeys = tObj.keys()
                            while (subKeys.hasNext()) {
                                val sk = subKeys.next()
                                val enc = tObj.optString(sk)
                                val embedUrl = decodeScxLink(enc) ?: continue
                                found = extractEmbed(embedUrl, data, "$key-$sk", subtitleCallback, callback) || found
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // 2. Fallback: inspect any iframes directly
        val iframeMatches = Regex("""(?:src|data-src)=["'](https?://[^"']+)["']""").findAll(html)
        for (m in iframeMatches) {
            val u = m.groupValues[1]
            if (u.contains("google") || u.contains("facebook") || u.contains("analytics")) continue
            if (Regex("""rapidvid|vidmoxy|trplayer|turkeyplayer|sobreat|ok\.ru|boosterx|embed|vod/""", RegexOption.IGNORE_CASE).containsMatchIn(u)) {
                found = extractEmbed(u, data, "Embed", subtitleCallback, callback) || found
            }
        }

        return found
    }

    // ── Extractor Dispatcher ────────────────────────────────────────────

    private suspend fun extractEmbed(
        embedUrl: String,
        referer: String,
        label: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val url = rewriteEmbedUrl(embedUrl)
        return when {
            url.contains("rapidvid") || url.contains("/vod/") -> extractRapidVid(url, referer, label, subtitleCallback, callback)
            url.contains("trplayer") || url.contains("turkeyplayer") -> extractTRPlayer(url, referer, label, callback)
            url.contains("vidmoxy") -> extractVidMoxy(url, referer, label, subtitleCallback, callback)
            else -> loadExtractor(url, referer, subtitleCallback, callback)
        }
    }

    private fun rewriteEmbedUrl(url: String): String {
        return url
            .replace(Regex("""^(https?://)(?:www\.)?watch\.trplayer\.site""", RegexOption.IGNORE_CASE), "$1watch.trplayer.com")
            .replace(Regex("""^(https?://)(?:www\.)?trplayer\.site""", RegexOption.IGNORE_CASE), "$1watch.trplayer.com")
            .replace(Regex("""^(https?://)(?:www\.)?trplayer\.org""", RegexOption.IGNORE_CASE), "$1watch.trplayer.com")
    }

    // ── RapidVid Extractor ──────────────────────────────────────────────

    private suspend fun extractRapidVid(
        embedUrl: String,
        referer: String,
        label: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return try {
            val origin = getOrigin(embedUrl)
            val html = app.get(embedUrl, headers = mapOf(
                "User-Agent" to defaultHeaders["User-Agent"]!!,
                "Referer" to referer
            )).text

            val match = Regex("""av\('([^']+)'\)""").find(html) ?: return false
            val enc = match.groupValues[1]

            val streamUrl = decodeRapidSecret(enc) ?: return false

            // Subtitles from jwSetup.tracks
            parseJwTracks(html, subtitleCallback)

            callback(
                ExtractorLink(
                    source = "RapidVid",
                    name = "FullHDFilm ($label • RapidVid)",
                    url = streamUrl,
                    referer = "$origin/",
                    quality = Qualities.P1080.value,
                    type = ExtractorLinkType.M3U8,
                    headers = mapOf(
                        "Referer" to "$origin/",
                        "Origin" to origin,
                        "User-Agent" to defaultHeaders["User-Agent"]!!
                    )
                )
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun decodeRapidSecret(encoded: String): String? {
        return try {
            val reversed = encoded.reversed()
            val t = Base64.decode(reversed, Base64.DEFAULT)
            val key = "K9L"
            val out = ByteArray(t.size)
            for (i in t.indices) {
                val offset = (key[i % key.length].code % 5) + 1
                out[i] = (t[i] - offset).toByte()
            }
            val dec = Base64.decode(out, Base64.DEFAULT)
            String(dec, Charsets.UTF_8).takeIf { it.startsWith("http") }
        } catch (_: Exception) {
            null
        }
    }

    // ── TRPlayer Extractor ──────────────────────────────────────────────

    private suspend fun extractTRPlayer(
        embedUrl: String,
        referer: String,
        label: String,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return try {
            val origin = getOrigin(embedUrl)
            val html = app.get(embedUrl, headers = mapOf(
                "User-Agent" to defaultHeaders["User-Agent"]!!,
                "Referer" to referer
            )).text

            val rawJson = Regex("""var\s+video\s*=\s*(\{[\s\S]*?\});""").find(html)?.groupValues?.get(1) ?: return false
            val uid = Regex(""""uid"\s*:\s*"?([^",}]+)"?""").find(rawJson)?.groupValues?.get(1) ?: return false
            val md5 = Regex(""""md5"\s*:\s*"([^"]+)"""").find(rawJson)?.groupValues?.get(1) ?: return false
            val id = Regex(""""id"\s*:\s*"?([^",}]+)"?""").find(rawJson)?.groupValues?.get(1) ?: return false

            val masterUrl = "$origin/m3u8/$uid/$md5/master.txt?s=1&id=$id&cache=1"

            callback(
                ExtractorLink(
                    source = "TRPlayer",
                    name = "FullHDFilm ($label • TRPlayer)",
                    url = masterUrl,
                    referer = "$origin/",
                    quality = Qualities.P1080.value,
                    type = ExtractorLinkType.M3U8,
                    headers = mapOf(
                        "Referer" to "$origin/",
                        "Origin" to origin,
                        "User-Agent" to defaultHeaders["User-Agent"]!!
                    )
                )
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    // ── VidMoxy Extractor ───────────────────────────────────────────────

    private suspend fun extractVidMoxy(
        embedUrl: String,
        referer: String,
        label: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return try {
            val origin = getOrigin(embedUrl)
            val html = app.get(embedUrl, headers = mapOf(
                "User-Agent" to defaultHeaders["User-Agent"]!!,
                "Referer" to referer
            )).text

            val hexMatch = Regex(""""file":\s*"([^"]*\\x[^"]*)"""").find(html)?.groupValues?.get(1)
            val m3u8 = hexMatch?.let { hexToString(it) }

            if (!m3u8.isNullOrBlank() && m3u8.startsWith("http")) {
                parseJwTracks(html, subtitleCallback)
                callback(
                    ExtractorLink(
                        source = "VidMoxy",
                        name = "FullHDFilm ($label • VidMoxy)",
                        url = m3u8,
                        referer = "$origin/",
                        quality = Qualities.P1080.value,
                        type = ExtractorLinkType.M3U8,
                        headers = mapOf(
                            "Referer" to "$origin/",
                            "Origin" to origin,
                            "User-Agent" to defaultHeaders["User-Agent"]!!
                        )
                    )
                )
                true
            } else {
                loadExtractor(embedUrl, referer, subtitleCallback, callback)
            }
        } catch (_: Exception) {
            false
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun rot13(input: String): String {
        val sb = StringBuilder()
        for (c in input) {
            when (c) {
                in 'a'..'z' -> sb.append(((c - 'a' + 13) % 26 + 'a'.code).toChar())
                in 'A'..'Z' -> sb.append(((c - 'A' + 13) % 26 + 'A'.code).toChar())
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun decodeScxLink(encoded: String): String? {
        return try {
            val rot = rot13(encoded)
            val bytes = Base64.decode(rot, Base64.DEFAULT)
            String(bytes, Charsets.UTF_8).takeIf { it.startsWith("http") }
        } catch (_: Exception) {
            null
        }
    }

    private fun hexToString(hex: String): String {
        val cleaned = hex.replace("\\x", "").replace("\\", "")
        val sb = StringBuilder()
        var i = 0
        while (i + 1 < cleaned.length) {
            val code = cleaned.substring(i, i + 2).toIntOrNull(16) ?: break
            sb.append(code.toChar())
            i += 2
        }
        return sb.toString()
    }

    private fun parseJwTracks(html: String, subtitleCallback: (SubtitleFile) -> Unit) {
        val trackMatches = Regex(""""kind"\s*:\s*"(?:captions|subtitles)"\s*,\s*"file"\s*:\s*"([^"]+)"\s*,\s*"label"\s*:\s*"([^"]+)"""").findAll(html)
        for (m in trackMatches) {
            val file = m.groupValues[1].replace("\\/", "/")
            val label = m.groupValues[2].trim()
            if (file.startsWith("http")) {
                subtitleCallback(SubtitleFile(lang = label, url = file))
            }
        }
    }

    private fun getOrigin(url: String): String {
        val match = Regex("""^(https?://[^/]+)""").find(url)
        return match?.groupValues?.get(1) ?: "https://www.fullhdfilmizlesene.now"
    }
}
