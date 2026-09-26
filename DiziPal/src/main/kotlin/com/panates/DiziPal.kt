package com.panates

import android.util.Base64
import android.util.Log
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.lagradost.cloudstream3.Actor
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.Score
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrlNull
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.newTvSeriesSearchResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.jsoup.nodes.Element
import java.net.URI
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DiziPal : MainAPI() {
    override var mainUrl = "https://dizipal1583.com"
    override var name = "DiziPal"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override var sequentialMainPage = true
    override var sequentialMainPageDelay = 150L
    override var sequentialMainPageScrollDelay = 150L

    override val mainPage = mainPageOf(
        "${mainUrl}/yabanci-dizi-izle" to "Yabancı Diziler",
        "${mainUrl}/hd-film-izle" to "Filmler",
        "${mainUrl}/anime" to "Anime",
        "${mainUrl}/kanal/netflix" to "Netflix",
        "${mainUrl}/kanal/disney" to "Disney+",
        "${mainUrl}/kanal/exxen" to "Exxen",
        "${mainUrl}/kanal/amazon" to "Amazon Prime",
        "${mainUrl}/kanal/max" to "Max",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data).document
        val items = document.select("a[data-dizipal-pageloader]").mapNotNull { it.kart() }
        return newHomePageResponse(request.name, items)
    }

    private fun Element.kart(): SearchResponse? {
        val href = this.attr("href").trim()
        if (href.isBlank()) return null

        val title = this.attr("title").removeSuffix(" izle").trim()
            .ifBlank { this.selectFirst("img")?.attr("alt")?.trim() ?: "" }
        if (title.isBlank()) return null

        val img = this.selectFirst("img")
        val rawPoster = img?.attr("data-src")?.trim().orEmpty().ifBlank { img?.attr("src")?.trim() ?: "" }
        val posterUrl = if (rawPoster.startsWith("http")) fixUrlNull(rawPoster) else null

        val score = Regex("""\b(\d{1,2}\.\d)\b""").find(this.text())?.groupValues?.get(1)

        return when {
            "/series/" in href -> newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
                this.score = Score.from10(score)
            }
            "/movies/" in href -> newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
                this.score = Score.from10(score)
            }
            else -> null
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val cKey = app.get("${mainUrl}/").document
            .selectFirst("input[name=cKey]")?.attr("value")?.trim()
        if (cKey.isNullOrBlank()) return emptyList()

        val response = app.post(
            "${mainUrl}/bg/searchcontent",
            data = mapOf(
                "cKey" to cKey,
                "cValue" to "",
                "searchterm" to query
            ),
            headers = mapOf(
                "X-Requested-With" to "XMLHttpRequest",
                "Accept" to "application/json, text/plain, */*",
                "Referer" to "${mainUrl}/"
            ),
            referer = "${mainUrl}/"
        ).text

        val envelope: DzpSearchEnvelope = objectMapper.readValue(response)
        if (envelope.data?.state != true) return emptyList()

        return envelope.data?.result?.mapNotNull { item ->
            val slug = item.usedSlug?.trim()?.removePrefix("/") ?: return@mapNotNull null
            val title = item.objectName?.trim() ?: return@mapNotNull null
            val poster = item.poster

            if (item.usedType?.contains("Movie", true) == true) {
                newMovieSearchResponse(title, "${mainUrl}/${slug}", TvType.Movie) {
                    this.posterUrl = poster
                }
            } else {
                newTvSeriesSearchResponse(title, "${mainUrl}/${slug}", TvType.TvSeries) {
                    this.posterUrl = poster
                }
            }
        } ?: emptyList()
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        var ld: JsonNode? = null
        for (script in document.select("script[type=application/ld+json]")) {
            val content = script.data().ifBlank { script.html() }
            if (content.isBlank()) continue
            val node = try {
                objectMapper.readTree(content)
            } catch (e: Exception) {
                continue
            } ?: continue
            val type = node.path("@type").asText("")
            if (type.contains("Movie") || type.contains("Series") || type == "TVEpisode") {
                ld = node
                break
            }
        }

        val title = ld?.path("name")?.asText("")?.ifBlank { null }
            ?: document.selectFirst("h1")?.text()?.trim()?.ifBlank { null }
            ?: return null
        val poster = ld?.path("image")?.asText(null)?.takeIf { it.startsWith("http") }
        val plot = ld?.path("description")?.asText(null)
        val year = ld?.path("datePublished")?.asText("")?.take(4)?.toIntOrNull()
        val actors = ld?.path("actor")?.mapNotNull { actorNode ->
            actorNode.path("name").asText(null)?.let { Actor(it) }
        } ?: emptyList()

        return if ("/series/" in url) {
            val episodes = mutableListOf<Episode>()
            document.select("a[data-dizipal-pageloader]").forEach { element ->
                val href = element.attr("href").trim()
                if (!href.contains("/bolum/")) return@forEach
                val label = element.select("div").firstOrNull()?.text()?.trim()?.ifBlank { null }
                    ?: element.text().trim()
                val match = Regex("""(\d+)\. ?Sezon\s+(\d+)\. ?Bölüm""").find(label)
                episodes.add(newEpisode(href) {
                    this.season = match?.groupValues?.get(1)?.toIntOrNull() ?: 1
                    this.episode = match?.groupValues?.get(2)?.toIntOrNull()
                    this.name = label
                })
            }
            if (episodes.isEmpty()) return null

            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.year = year
                this.plot = plot
                addActors(actors)
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.year = year
                this.plot = plot
                addActors(actors)
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        val payloadText = document.selectFirst("div[data-rm-k]")?.text()?.trim()
        if (payloadText.isNullOrBlank()) return false

        val payload = try {
            objectMapper.readTree(payloadText)
        } catch (e: Exception) {
            Log.e("DiziPal", "payload parse failed: ${e.message}")
            null
        } ?: return false

        val ciphertext = payload.path("ciphertext").asText(null) ?: return false
        val iv = payload.path("iv").asText(null) ?: return false
        val salt = payload.path("salt").asText(null) ?: return false

        val iframeUrl = decryptPlayerPayload(ciphertext, iv, salt) ?: return false
        val absIframe = if (iframeUrl.startsWith("//")) "https:${iframeUrl}" else iframeUrl
        if (!absIframe.startsWith("http")) return false

        val iframeHtml = app.get(absIframe, referer = "${mainUrl}/").text
        val blob = Regex("""openPlayer\('([^']+)'""").find(iframeHtml)?.groupValues?.get(1)
            ?: return false
        val host = URI(absIframe).host ?: return false

        val sourceJson = app.get("https://${host}/source2.php?v=${blob}", referer = absIframe).text
        val root = try {
            objectMapper.readTree(sourceJson)
        } catch (e: Exception) {
            Log.e("DiziPal", "source2 parse failed: ${e.message}")
            null
        } ?: return false

        val file = root.path("playlist").path(0).path("sources").path(0).path("file").asText(null)
            ?: return false
        val master = file.replace("m.php", "master.m3u8")

        val subtitles = mutableListOf<Pair<String, String>>()
        collectSubtitles(root, subtitles)
        subtitles.forEach { (lang, url) ->
            subtitleCallback.invoke(SubtitleFile(lang = lang, url = url))
        }

        callback.invoke(
            newExtractorLink(
                source = name,
                name = "DiziPal",
                url = master,
                type = ExtractorLinkType.M3U8
            ) {
                headers = mapOf(
                    "Referer" to absIframe,
                    "User-Agent" to USER_AGENT
                )
                quality = Qualities.Unknown.value
            }
        )
        return true
    }

    private fun collectSubtitles(node: JsonNode, out: MutableList<Pair<String, String>>) {
        if (node.isObject) {
            val file = node.path("file").asText("")
            if (file.contains(".vtt")) {
                val lang = node.path("label").asText(null)
                    ?: node.path("language").asText(null)
                    ?: "Altyazı"
                out.add(lang to file)
            }
            node.forEach { collectSubtitles(it, out) }
        } else if (node.isArray) {
            node.forEach { collectSubtitles(it, out) }
        }
    }

    private fun decryptPlayerPayload(cipherB64: String, ivHex: String, saltHex: String): String? {
        return try {
            val key = pbkdf2Sha512(
                PLAYER_PASSWORD.toByteArray(Charsets.UTF_8),
                hexToBytes(saltHex),
                PBKDF2_ITERATIONS,
                32
            )
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(key, "AES"),
                IvParameterSpec(hexToBytes(ivHex))
            )
            String(cipher.doFinal(Base64.decode(cipherB64, Base64.DEFAULT)), Charsets.UTF_8).trim()
        } catch (e: Exception) {
            Log.e("DiziPal", "decryptPlayerPayload failed: ${e.message}")
            null
        }
    }

    // PBKDF2WithHmacSHA512 is only available from API 26; minSdk is 21, so derive manually.
    private fun pbkdf2Sha512(password: ByteArray, salt: ByteArray, iterations: Int, dkLen: Int): ByteArray {
        val mac = Mac.getInstance("HmacSHA512")
        mac.init(SecretKeySpec(password, "HmacSHA512"))
        var u = mac.doFinal(salt + byteArrayOf(0, 0, 0, 1))
        val t = u.copyOf()
        for (i in 1 until iterations) {
            u = mac.doFinal(u)
            for (j in t.indices) {
                t[j] = (t[j].toInt() xor u[j].toInt()).toByte()
            }
        }
        return t.copyOf(dkLen)
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.trim()
        return ByteArray(clean.length / 2) { clean.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }

    private val objectMapper by lazy {
        ObjectMapper().registerModule(KotlinModule.Builder().build()).apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    companion object {
        private const val PBKDF2_ITERATIONS = 999
        private const val PLAYER_PASSWORD = "3hPn4uCjTVtfYWcjIcoJQ4cL1WWk1qxXI39egLYOmNv6IblA7eKJz68uU3eLzux1biZLCms0quEjTYniGv5z1JcKbNIsDQFSeIZOBZJz4is6pD7UyWDggWWzTLBQbHcQFpBQdClnuQaMNUHtLHTpzCvZy33p6I7wFBvL4fnXBYH84aUIyWGTRvM2G5cfoNf4705tO2kv"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    }
}
