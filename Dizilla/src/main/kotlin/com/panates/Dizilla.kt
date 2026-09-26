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
import com.lagradost.cloudstream3.ErrorLoadingException
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
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.fixUrlNull
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.newTvSeriesSearchResponse
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.syncproviders.SyncIdName
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URLEncoder
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Dizilla : MainAPI() {
    override var mainUrl = "https://dizilla.now"
    override var name = "Dizilla"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = true
    override val supportedTypes = setOf(TvType.TvSeries)

    override var sequentialMainPage = true
    override var sequentialMainPageDelay = 150L
    override var sequentialMainPageScrollDelay = 150L

    private val cloudflareKiller by lazy { CloudflareKiller() }
    private val interceptor by lazy { CloudflareInterceptor(cloudflareKiller) }

    class CloudflareInterceptor(private val cloudflareKiller: CloudflareKiller): Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)
            val doc = Jsoup.parse(response.peekBody(1024 * 1024).string())

            if (doc.html().contains("verifying")) {
                return cloudflareKiller.intercept(chain)
            }

            return response
        }
    }

    override val supportedSyncNames = setOf(
        SyncIdName.Simkl
    )

    override val mainPage = mainPageOf(
        "${mainUrl}/" to "Anasayfa",
        "${mainUrl}/arsiv" to "Yeni Eklenen Diziler",
        "${mainUrl}/dizi-turu/aile" to "Aile",
        "${mainUrl}/dizi-turu/aksiyon" to "Aksiyon",
        "${mainUrl}/dizi-turu/bilim-kurgu" to "Bilim Kurgu",
        "${mainUrl}/dizi-turu/dram" to "Dram",
        "${mainUrl}/dizi-turu/fantastik" to "Fantastik",
        "${mainUrl}/dizi-turu/gerilim" to "Gerilim",
        "${mainUrl}/dizi-turu/komedi" to "Komedi",
        "${mainUrl}/dizi-turu/korku" to "Korku",
        "${mainUrl}/dizi-turu/macera" to "Macera",
        "${mainUrl}/dizi-turu/romantik" to "Romantik",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val home = if (request.data.contains("dizi-turu")) {
            val document = app.get(request.data, interceptor = interceptor).document
            document.select("span.watchlistitem-").mapNotNull { it.diziler() }
        } else if (request.data.contains("/arsiv")) {
            val yil = Calendar.getInstance().get(Calendar.YEAR)
            val sayfa = "?page=$page&tab=1&sort=date_desc&filterType=2&imdbMin=5&imdbMax=10&yearMin=1900&yearMax=$yil"
            val document = app.get("${request.data}${sayfa}", interceptor = interceptor).document
            document.select("""a.w-full[href^="/dizi/"]""").mapNotNull { it.kart() }
        } else {
            val document = app.get(request.data, interceptor = interceptor).document
            document.select("""a.w-full[href^="/dizi/"]""").mapNotNull { it.kart() }
        }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.kart(): SearchResponse? {
        val href = fixUrlNull(this.attr("href")) ?: return null
        val title = this.attr("title").removeSuffix(" izle").trim()
            .ifBlank { this.selectFirst("img")?.attr("alt")?.trim() ?: "" }
        if (title.isBlank()) return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))
        val score = Regex("""\b(\d{1,2}\.\d)\b""").find(this.text())?.groupValues?.get(1)

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
            this.posterUrl = posterUrl
            this.score = Score.from10(score)
        }
    }

    private fun Element.diziler(): SearchResponse? {
        val a = this.selectFirst("a[href]") ?: return null
        val href = fixUrlNull(a.attr("href")) ?: return null
        val img = this.selectFirst("img")
        val alt = img?.attr("alt")?.trim().orEmpty()
        val title = if (alt.isNotBlank()) alt else a.attr("title").removeSuffix(" izle").trim()
        if (title.isBlank()) return null
        val posterUrl = fixUrlNull(img?.attr("src"))
        val score = Regex("""\b(\d{1,2}\.\d)\b""").find(this.text())?.groupValues?.get(1)

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
            this.posterUrl = posterUrl
            this.score = Score.from10(score)
        }
    }

    private fun SearchItem.toSearchResponse(): SearchResponse? {
        return newTvSeriesSearchResponse(
            title ?: return null,
            "${mainUrl}/${slug ?: return null}",
            TvType.TvSeries,
        ) {
            this.posterUrl = poster
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchReq = app.post(
            "${mainUrl}/api/bg/searchcontent?searchterm=${URLEncoder.encode(query, "UTF-8")}",
            headers = mapOf(
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0",
                "Accept" to "application/json, text/plain, */*",
                "Accept-Language" to "en-US,en;q=0.5",
                "X-Requested-With" to "XMLHttpRequest",
                "Sec-Fetch-Site" to "same-origin",
                "Sec-Fetch-Mode" to "cors",
                "Sec-Fetch-Dest" to "empty",
                "Referer" to "${mainUrl}/"
            ),
            referer = "${mainUrl}/",
        )
        val searchResult: SearchResult = objectMapper.readValue(searchReq.text)
        val decrypted = decryptDizillaResponse(searchResult.response ?: return emptyList())
            ?: return emptyList()
        val contentJson: SearchData = objectMapper.readValue(decrypted)
        if (contentJson.state != true) {
            throw ErrorLoadingException("Invalid Json response")
        }
        return contentJson.result?.mapNotNull { it.toSearchResponse() } ?: emptyList()
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val page = fetchSecurePage(url) ?: return null
        val secure = page.secure
        val contentItem = secure.path("contentItem")
        val title = contentItem.path("original_title").asText("").ifBlank { return null }
        val poster = contentItem.path("poster_url").asText(null)
        val heroSrc = page.document.selectFirst("div.w-full.page-top.relative img")
            ?.attr("src")?.trim().orEmpty()
        val hero = if (heroSrc.isBlank()) null
            else if (heroSrc.startsWith("http")) heroSrc else fixUrl(heroSrc)
        val year = contentItem.path("release_year")
            .takeIf { !it.isMissingNode && !it.isNull }?.asInt()
        val description = contentItem.path("description").asText(null)
        val rating = contentItem.path("imdb_point")
            .takeIf { !it.isMissingNode && !it.isNull }?.asDouble()?.toString()

        val related = secure.path("RelatedResults")
        val tags = related.path("getSerieCategoriesById").path("result")
            .mapNotNull { it.path("name").asText(null) }
        val actors = related.path("getSerieCastsById").path("result").mapNotNull {
            val actorName = it.path("name").asText(null) ?: return@mapNotNull null
            Actor(actorName, it.path("cast_image").asText(null))
        }

        val episodeses = mutableListOf<Episode>()
        related.path("getSerieSeasonAndEpisodes").path("result").forEach { season ->
            val seasonNo = season.path("season_no")
                .takeIf { !it.isMissingNode && !it.isNull }?.asInt() ?: 1
            season.path("episodes").forEach { ep ->
                val slug = ep.path("used_slug").asText(null) ?: return@forEach
                val epText = ep.path("episode_text").asText("")
                val epSubtitle = ep.path("episode_subtitle").asText("")
                val epName = if (epSubtitle.isNotBlank()) "$epText - $epSubtitle" else epText
                episodeses.add(newEpisode("${mainUrl}/${slug}") {
                    this.name = epName.ifBlank { null }
                    this.season = seasonNo
                    this.episode = ep.path("episode_no")
                        .takeIf { !it.isMissingNode && !it.isNull }?.asInt()
                })
            }
        }

        if (episodeses.isEmpty()) return null

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodeses) {
            this.posterUrl = poster ?: hero
            this.backgroundPosterUrl = hero
            this.year = year
            this.plot = description
            this.tags = tags
            this.score = Score.from10(rating)
            addActors(actors)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val secure = fetchSecureData(data) ?: return false
        var found = false
        for (source in secure.path("RelatedResults").path("getEpisodeSources").path("result")) {
            val html = source.path("source_content").asText("")
            if (html.isBlank()) continue
            val iframe = Jsoup.parse(html).selectFirst("iframe")?.attr("src")?.trim()
            if (iframe.isNullOrBlank()) continue
            val iframeUrl = if (iframe.startsWith("//")) "https:${iframe}" else fixUrl(iframe)
            loadExtractor(iframeUrl, "${mainUrl}/", subtitleCallback, callback)
            found = true
        }
        return found
    }

    private val objectMapper by lazy {
        ObjectMapper().registerModule(KotlinModule.Builder().build()).apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    private data class SecurePage(val secure: JsonNode, val document: Document)

    private suspend fun fetchSecurePage(url: String): SecurePage? {
        val document = app.get(url, interceptor = interceptor).document
        val script = document.selectFirst("script#__NEXT_DATA__")?.data() ?: return null
        val secureData = objectMapper.readTree(script)?.path("props")?.path("pageProps")
            ?.path("secureData")?.asText(null) ?: return null
        val decrypted = decryptDizillaResponse(secureData) ?: return null
        return SecurePage(objectMapper.readTree(decrypted), document)
    }

    private suspend fun fetchSecureData(url: String): JsonNode? = fetchSecurePage(url)?.secure

    private val privateAESKey = "9bYMCNQiWsXIYFWYAu7EkdsSbmGBTyUI"

    private fun decryptDizillaResponse(response: String): String? {
        try {
            val algorithm = "AES/CBC/PKCS5Padding"
            val keySpec = SecretKeySpec(privateAESKey.toByteArray(), "AES")

            val iv = ByteArray(16)
            val ivSpec = IvParameterSpec(iv)

            val cipher1 = Cipher.getInstance(algorithm)
            cipher1.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val firstIterationData =
                cipher1.doFinal(Base64.decode(response, Base64.DEFAULT))

            return String(firstIterationData)
        } catch (e: Exception) {
            Log.e("Dizilla", "Decryption failed: ${e.message}")
            return null
        }
    }
}
