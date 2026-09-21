package com.panates

import android.util.Base64
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.loadExtractor
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DiziFilm : MainAPI() {
    override var mainUrl = "https://dizifilmizle.to"
    override var name = "DiziFilm"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override var lang = "tr"
    override val hasMainPage = true

    companion object {
        private val defaultHeaders = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36",
            "Accept-Language" to "tr-TR,tr;q=0.9,en;q=0.8"
        )
    }

    // ── Main Page ───────────────────────────────────────────────────────

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val allPages = mutableListOf<HomePageList>()

        val doc = try {
            app.get(mainUrl, headers = defaultHeaders).document
        } catch (_: Exception) {
            return newHomePageResponse(allPages)
        }

        // Parse sections: Popüler Filmler, HD Film izle, Yabancı Diziler, Efsane Diziler, etc.
        val sectionHeaders = doc.select("h2[id^='section-'], h2.text-lg, h2.font-bold")
        for (header in sectionHeaders) {
            val title = header.text().trim()
            if (title.isBlank() || title.equals("DiziFilm", ignoreCase = true)) continue

            // Traverse parent container to find swiper-wrapper or cards
            val container = header.parents().firstOrNull { it.tagName() == "section" || it.children().any { c -> c.hasClass("swiper") || c.hasClass("relative") } }
                ?: header.parent()?.parent() ?: continue

            val cards = container.select("a[href*='/film/'], a[href*='/dizi/']")
            val items = cards.mapNotNull { a ->
                val href = a.attr("href")
                if (href.isBlank() || href.contains("/sezon-") || href.contains("/bolum-")) return@mapNotNull null

                val img = a.selectFirst("img") ?: a.parent()?.selectFirst("img")
                val poster = img?.attr("src")?.takeIf { it.isNotBlank() }
                    ?: img?.attr("data-src")?.takeIf { it.isNotBlank() }
                    ?: img?.attr("srcSet")?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() }

                val rawName = a.attr("aria-label").takeIf { it.isNotBlank() }
                    ?: a.selectFirst(".sr-only")?.text()
                    ?: img?.attr("alt")?.takeIf { it.isNotBlank() }
                    ?: a.text()

                val cardTitle = rawName.replace(Regex("""(?i)\s*(izle\d*|dizi izle|film izle)"""), "").trim()
                if (cardTitle.isBlank()) return@mapNotNull null

                val cardScore = a.parent()?.selectFirst(".dynamic-island.bg-yellow-500\\/20, [class*='bg-yellow-500']")?.text()?.let {
                    Regex("""(\d+(?:\.\d+)?)""").find(it)?.groupValues?.get(1)?.toDoubleOrNull()
                }

                val fullUrl = fixUrl(href)
                if (fullUrl.contains("/film/")) {
                    newMovieSearchResponse(cardTitle, fullUrl, TvType.Movie) {
                        this.posterUrl = poster?.let { fixUrl(it) }
                        if (cardScore != null) this.score = Score.from10(cardScore)
                    }
                } else {
                    newTvSeriesSearchResponse(cardTitle, fullUrl, TvType.TvSeries) {
                        this.posterUrl = poster?.let { fixUrl(it) }
                        if (cardScore != null) this.score = Score.from10(cardScore)
                    }
                }
            }.distinctBy { it.url }

            if (items.isNotEmpty()) {
                allPages.add(HomePageList(title, items))
            }
        }

        return newHomePageResponse(allPages)
    }

    // ── Search ──────────────────────────────────────────────────────────

    override suspend fun search(query: String): List<SearchResponse> {
        val searchApi = "$mainUrl/api/search?q=${URLEncoder.encode(query.trim(), "UTF-8")}"

        val jsonStr = try {
            app.get(searchApi, headers = defaultHeaders).text
        } catch (_: Exception) {
            return emptyList()
        }

        val json = try {
            JSONObject(jsonStr)
        } catch (_: Exception) {
            return emptyList()
        }

        val results = json.optJSONArray("results") ?: return emptyList()
        val list = mutableListOf<SearchResponse>()

        for (i in 0 until results.length()) {
            val item = results.optJSONObject(i) ?: continue
            val slug = item.optString("slug").takeIf { it.isNotBlank() } ?: continue
            val title = item.optString("title").takeIf { it.isNotBlank() }
                ?: item.optString("original_title").takeIf { it.isNotBlank() } ?: continue
            val posterUrl = item.optString("poster_url").takeIf { it.isNotBlank() }?.let { fixUrl(it) }
            val contentType = item.optString("content_type")
            val year = item.optInt("year", 0).takeIf { it > 0 }

            if (contentType == "movie") {
                val fullUrl = "$mainUrl/film/$slug"
                list.add(newMovieSearchResponse(title, fullUrl, TvType.Movie) {
                    this.posterUrl = posterUrl
                    this.year = year
                })
            } else {
                val fullUrl = "$mainUrl/dizi/$slug"
                list.add(newTvSeriesSearchResponse(title, fullUrl, TvType.TvSeries) {
                    this.posterUrl = posterUrl
                    this.year = year
                })
            }
        }

        return list
    }

    // ── Details / Load ──────────────────────────────────────────────────

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url, headers = defaultHeaders).document
        val html = doc.html()

        val rscPayload = parseRscPayload(html)

        val title = doc.selectFirst("h1")?.text()?.trim()
            ?: doc.selectFirst("meta[property='og:title']")?.attr("content")?.trim()
            ?: "DiziFilm"

        val poster = doc.selectFirst("meta[property='og:image']")?.attr("content")?.let { fixUrl(it) }
            ?: doc.selectFirst("img.object-cover, img[src*='/poster/']")?.attr("src")?.let { fixUrl(it) }

        val plot = doc.selectFirst("meta[name='description']")?.attr("content")?.trim()
            ?: doc.selectFirst("p.text-gray-300, p.text-gray-400")?.text()?.trim()

        val year = Regex("""(20\d\d|19\d\d)""").find(html)?.groupValues?.get(1)?.toIntOrNull()

        val scoreText = Regex("""(?:imdb_rating|tmdb_rating)["']?\s*:\s*(\d+(?:\.\d+)?)""").find(rscPayload)?.groupValues?.get(1)
            ?: Regex("""(\d+(?:\.\d+)?)\s*(?:/10|IMDb)""").find(html)?.groupValues?.get(1)
        val score = scoreText?.toDoubleOrNull()

        val tags = doc.select("a[href*='/tur/']").mapNotNull {
            it.text().trim().takeIf { t -> t.isNotBlank() }
        }.distinct()

        val isMovie = url.contains("/film/")
        if (isMovie) {
            return newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = plot
                this.year = year
                this.tags = tags
                if (score != null) this.score = Score.from10(score)
            }
        }

        // TV Series: Find all season and episode links
        val episodeMatches = Regex("""href=["'](/dizi/[^/]+/sezon-(\d+)/bolum-(\d+))["']""").findAll(html).toList()
        val episodes = if (episodeMatches.isNotEmpty()) {
            episodeMatches.map { m ->
                val epUrl = fixUrl(m.groupValues[1])
                val sNum = m.groupValues[2].toIntOrNull() ?: 1
                val eNum = m.groupValues[3].toIntOrNull() ?: 1
                newEpisode(epUrl) {
                    this.name = "$sNum. Sezon $eNum. Bölüm"
                    this.season = sNum
                    this.episode = eNum
                    this.posterUrl = poster
                }
            }.distinctBy { "${it.season}-${it.episode}" }.sortedWith(compareBy({ it.season }, { it.episode }))
        } else {
            emptyList()
        }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
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
        var found = false

        // Case 1: data is already an embed URL (e.g. from movie parts)
        if (data.contains("/embed/") || data.contains("/video/")) {
            found = extractDirectEmbed(data, "$mainUrl/", subtitleCallback, callback) || found
            if (found) return true
        }

        // Case 2: data is a page URL (episode page or movie page)
        val html = try {
            app.get(data, headers = defaultHeaders).text
        } catch (_: Exception) {
            return false
        }

        val rscPayload = parseRscPayload(html)

        // Find embeds from episode payload
        val embedUrls = mutableListOf<String>()
        val embed1 = Regex(""""embed_player_url_1"\s*:\s*"(https?:[^"]+)"""").find(rscPayload)?.groupValues?.get(1)
        val embed2 = Regex(""""embed_player_url_2"\s*:\s*"(https?:[^"]+)"""").find(rscPayload)?.groupValues?.get(1)
        if (!embed1.isNullOrBlank()) embedUrls.add(embed1.replace("\\/", "/"))
        if (!embed2.isNullOrBlank()) embedUrls.add(embed2.replace("\\/", "/"))

        // Also check parts from payload
        for (part in parseMovieParts(rscPayload)) {
            if (!embedUrls.contains(part.url)) embedUrls.add(part.url)
        }

        // Also fallback to scanning html for iframe/embeds
        if (embedUrls.isEmpty()) {
            val iframeMatches = Regex("""(?:src|data-src)=["'](https?://[^"']*(?:vidmixi|vidlop|embed|video)[^"']*)["']""").findAll(html)
            for (m in iframeMatches) {
                val u = m.groupValues[1].replace("\\/", "/")
                if (!embedUrls.contains(u)) embedUrls.add(u)
            }
        }

        for (embedUrl in embedUrls) {
            found = extractDirectEmbed(embedUrl, data, subtitleCallback, callback) || found
        }

        return found
    }

    // ── Extractors ──────────────────────────────────────────────────────

    private suspend fun extractDirectEmbed(
        embedUrl: String,
        referer: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return when {
            isVidmixiUrl(embedUrl) -> extractVidmixi(embedUrl, referer, subtitleCallback, callback)
            embedUrl.contains("vidlop.com/video/") -> extractVidlop(embedUrl, referer, subtitleCallback, callback)
            else -> loadExtractor(embedUrl, referer, subtitleCallback, callback)
        }
    }

    private fun isVidmixiUrl(url: String): Boolean {
        return url.contains("vidmixi.com") || Regex("""/embed/[0-9a-f]{16,}""", RegexOption.IGNORE_CASE).containsMatchIn(url)
    }

    private suspend fun extractVidmixi(
        embedUrl: String,
        referer: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return try {
            val origin = getOrigin(embedUrl)
            val html = app.get(embedUrl, headers = mapOf(
                "User-Agent" to defaultHeaders["User-Agent"]!!,
                "Referer" to referer
            )).text

            val match = Regex("""bePlayer\(\s*'([^']+)'\s*,\s*'([\s\S]*?)'\s*\)""").find(html) ?: return false
            val passphrase = match.groupValues[1]
            val setJson = match.groupValues[2]

            val decrypted = decryptBePlayer(passphrase, setJson) ?: return false
            val settings = JSONObject(decrypted)

            val streamUrl = settings.optString("video_location").replace("\\/", "/")
            if (streamUrl.isBlank() || !streamUrl.startsWith("http")) return false

            // Subtitles
            val subsArray = settings.optJSONArray("strSubtitles")
            if (subsArray != null) {
                for (i in 0 until subsArray.length()) {
                    val subObj = subsArray.optJSONObject(i) ?: continue
                    var subFile = subObj.optString("file").replace("\\/", "/")
                    if (subFile.isBlank()) continue
                    if (subFile.startsWith("/")) subFile = "$origin$subFile"
                    val subLabel = subObj.optString("label").takeIf { it.isNotBlank() }
                        ?: subObj.optString("language").takeIf { it.isNotBlank() } ?: "Türkçe"
                    subtitleCallback(
                        SubtitleFile(
                            lang = subLabel,
                            url = subFile
                        )
                    )
                }
            }

            callback(
                ExtractorLink(
                    source = "Vidmixi",
                    name = "DiziFilm (Vidmixi HLS)",
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

    private suspend fun extractVidlop(
        videoUrl: String,
        referer: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return try {
            val videoId = Regex("""vidlop\.com/video/([^/?#]+)""").find(videoUrl)?.groupValues?.get(1) ?: return false
            val vidlopOrigin = "https://vidlop.com"
            val pageUrl = "$vidlopOrigin/video/$videoId"

            val body = mapOf("hash" to videoId, "r" to referer)
            val jsonStr = app.post(
                "$vidlopOrigin/player/index.php?data=$videoId&do=getVideo",
                data = body,
                headers = mapOf(
                    "User-Agent" to defaultHeaders["User-Agent"]!!,
                    "Referer" to pageUrl,
                    "X-Requested-With" to "XMLHttpRequest"
                )
            ).text

            val json = JSONObject(jsonStr)
            val streamUrl = json.optString("securedLink").takeIf { it.isNotBlank() }
                ?: json.optString("videoSource").takeIf { it.isNotBlank() } ?: return false

            callback(
                ExtractorLink(
                    source = "Vidlop",
                    name = "DiziFilm (Vidlop HLS)",
                    url = streamUrl.replace("\\/", "/"),
                    referer = pageUrl,
                    quality = Qualities.P1080.value,
                    type = ExtractorLinkType.M3U8,
                    headers = mapOf(
                        "Referer" to pageUrl,
                        "Origin" to vidlopOrigin,
                        "User-Agent" to defaultHeaders["User-Agent"]!!
                    )
                )
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun parseRscPayload(html: String): String {
        val sb = StringBuilder()
        val regex = Regex("""self\.__next_f\.push\(\[1,"((?:\\.|[^"\\])*)"\]\)""")
        for (m in regex.findAll(html)) {
            val chunk = m.groupValues[1]
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
            sb.append(chunk)
        }
        return sb.toString()
    }

    private data class MoviePart(val title: String, val url: String, val language: String, val quality: String)

    private fun parseMovieParts(payload: String): List<MoviePart> {
        val list = mutableListOf<MoviePart>()
        val partsJson = Regex(""""parts"\s*:\s*(\[[^\]]*\])""").find(payload)?.groupValues?.get(1)
        if (!partsJson.isNullOrBlank()) {
            try {
                val arr = JSONArray(partsJson)
                for (i in 0 until arr.length()) {
                    val p = arr.optJSONObject(i) ?: continue
                    val url = p.optString("url").replace("\\/", "/")
                    if (url.isNotBlank() && url.startsWith("http")) {
                        list.add(
                            MoviePart(
                                title = p.optString("title", "Tek Part").trim(),
                                url = url,
                                language = p.optString("language", "Türkçe").trim(),
                                quality = p.optString("quality", "HD").trim()
                            )
                        )
                    }
                }
            } catch (_: Exception) {}
        }
        return list
    }

    private fun getOrigin(url: String): String {
        val match = Regex("""^(https?://[^/]+)""").find(url)
        return match?.groupValues?.get(1) ?: "https://vidmixi.com"
    }

    private fun md5(bytes: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("MD5")
        return md.digest(bytes)
    }

    private fun evpBytesToKey(pass: ByteArray, salt: ByteArray, keyLen: Int, ivLen: Int): Pair<ByteArray, ByteArray> {
        val target = keyLen + ivLen
        var derived = ByteArray(0)
        var prev = ByteArray(0)
        while (derived.size < target) {
            val combined = prev + pass + salt
            prev = md5(combined)
            derived += prev
        }
        val key = derived.copyOfRange(0, keyLen)
        val iv = derived.copyOfRange(keyLen, keyLen + ivLen)
        return Pair(key, iv)
    }

    private fun decryptBePlayer(passphrase: String, setJson: String): String? {
        return try {
            val obj = JSONObject(setJson)
            val ctB64 = obj.getString("ct").replace("\\/", "/").replace("\\", "")
            val saltHex = obj.getString("s")
            val ct = Base64.decode(ctB64, Base64.DEFAULT)
            val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            val passBytes = passphrase.toByteArray(Charsets.UTF_8)
            val (key, iv) = evpBytesToKey(passBytes, salt, 32, 16)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
            val decrypted = cipher.doFinal(ct)
            String(decrypted, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }
}
