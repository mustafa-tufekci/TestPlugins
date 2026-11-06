package com.panates

import android.util.Log
import org.jsoup.nodes.Element
import java.net.URLEncoder
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

class W2m : MainAPI() {
    override var mainUrl              = "https://movies2watch.watch"
    override var name                 = "Watch2Movies"
    override val hasMainPage          = true
    override var lang                 = "en"
    override val hasQuickSearch       = false
    override val supportedTypes       = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        "${mainUrl}/home?page="            to "Latest",
        "${mainUrl}/movies?page="          to "Movies",
        "${mainUrl}/tv-series?page="       to "TV Series",
        "${mainUrl}/genre/action?page="    to "Action",
        "${mainUrl}/genre/comedy?page="    to "Comedy",
        "${mainUrl}/genre/drama?page="     to "Drama",
        "${mainUrl}/genre/horror?page="    to "Horror",
        "${mainUrl}/genre/thriller?page="  to "Thriller",
        "${mainUrl}/genre/romance?page="   to "Romance",
        "${mainUrl}/genre/sci-fi-fantasy?page=" to "Sci-Fi & Fantasy",
        "${mainUrl}/genre/animation?page=" to "Animation"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}${page}").document
        val home     = document.select("div.flw-item").mapNotNull { it.toMainPageResult() }.distinctBy { it.url }

        return newHomePageResponse(request.name, home)
    }

    private fun resim(ham: String?): String? {
        var adres = ham?.trim().orEmpty()
        if (adres.isBlank()) return null
        if (adres.startsWith("//")) adres = "https:$adres"
        if (!adres.startsWith("http")) return adres
        if (adres.contains("image.tmdb.org/t/p/w")) {
            adres = Regex("/t/p/w\\d+/").replace(adres, "/t/p/w500/")
        }
        return adres
    }

    private fun Element.toMainPageResult(): SearchResponse? {
        val a         = this.selectFirst("h3.film-name a") ?: this.selectFirst("h2 a") ?: this.selectFirst("a[href]")
        val title     = (a?.text()?.trim()?.ifBlank { null }
            ?: this.selectFirst("img")?.attr("alt")?.trim()?.ifBlank { null }
            ?: return null)
        val href      = fixUrlNull(a?.attr("href") ?: this.selectFirst("a.film-poster-ahref")?.attr("href"))
            ?: return null
        val posterUrl = resim(this.selectFirst("img.film-poster-img")?.attr("src")
            ?: this.selectFirst("img")?.attr("data-src")
            ?: this.selectFirst("img")?.attr("src"))
        val dizi      = href.contains("/series/")

        return if (dizi) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("${mainUrl}/search/${URLEncoder.encode(query, "UTF-8")}").document

        return document.select("div.flw-item").mapNotNull { it.toMainPageResult() }.distinctBy { it.url }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    // ── Load ─────────────────────────────────────────────────────────────

    private fun Element.satir(etiket: String): String? =
        this.select("div.row-line")
            .firstOrNull { it.text().startsWith(etiket, ignoreCase = true) }
            ?.text()
            ?.substringAfter(":")
            ?.trim()
            ?.ifBlank { null }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title       = (document.selectFirst("h2.heading-name a")?.text()?.trim()?.ifBlank { null }
            ?: document.selectFirst("div.dp-i-content h2 a")?.text()?.trim()?.ifBlank { null }
            ?: document.selectFirst("h1")?.text()?.trim()?.ifBlank { null }
            ?: return null)

        val poster = resim(document.selectFirst("div.dp-i-c-poster img.film-poster-img")?.attr("src")
            ?: document.selectFirst("div.dp-i-c-poster img")?.attr("src")
            ?: document.selectFirst("meta[property=og:image]")?.attr("content"))

        val description = document.selectFirst("meta[property=og:description]")?.attr("content")?.trim()
            ?: document.selectFirst("div.description")?.text()?.trim()

        val year = document.selectFirst("div.row-line")
            ?.let { _ -> document.select("div.row-line").firstOrNull { it.text().startsWith("Released", ignoreCase = true) } }
            ?.text()
            ?.substringAfter(":")
            ?.trim()
            ?.take(4)
            ?.toIntOrNull()

        val sure = document.select("div.row-line")
            .firstOrNull { it.text().startsWith("Duration", ignoreCase = true) }
            ?.text()
            ?.substringAfter(":")
            ?.replace("min", "")
            ?.trim()
            ?.toIntOrNull()

        val etiketler = document.select("div.row-line a[href*='/genre/']").map { it.text().trim() }
            .ifEmpty {
                document.select("div.row-line")
                    .firstOrNull { it.text().startsWith("Genre", ignoreCase = true) }
                    ?.text()?.substringAfter(":")?.split(",")?.map { it.trim() } ?: emptyList()
            }

        val oyuncular = document.select("div.row-line a[href*='/cast/']").map { Actor(it.text()) }
            .ifEmpty {
                document.select("div.row-line")
                    .firstOrNull { it.text().startsWith("Casts", ignoreCase = true) }
                    ?.text()?.substringAfter(":")?.split(",")?.map { Actor(it.trim()) } ?: emptyList()
            }

        val puan   = document.selectFirst("button.btn-imdb")?.text()?.trim()?.split(" ")?.last()
        val fragman = document.selectFirst("iframe#iframe-trailer")?.attr("data-src")
            ?: document.selectFirst("iframe[src*=youtube]")?.attr("src")

        val oneriler = document.select("div.flw-item")
            .mapNotNull { it.toMainPageResult() }
            .filter { it.url != url }

        return if (url.contains("/series/")) {
            val bolumler = document.select("a[href*='/episode-'], a[href*='/season-']")
                .mapNotNull { a ->
                    val href = fixUrlNull(a.attr("href")) ?: return@mapNotNull null
                    val m = Regex("(?i)season[-/](\\d+)").find(href)
                    val b = Regex("(?i)episode[-/](\\d+)").find(href)
                    newEpisode(href) {
                        this.season = m?.groupValues?.get(1)?.toIntOrNull()
                        this.episode = b?.groupValues?.get(1)?.toIntOrNull()
                        this.name = a.text().trim().ifBlank { null }
                    }
                }
                .distinctBy { it.data }

            newTvSeriesLoadResponse(title, url, TvType.TvSeries, bolumler.ifEmpty { listOf(newEpisode(url) { }) }) {
                this.posterUrl       = poster
                this.plot            = description
                this.year            = year
                this.tags            = etiketler
                this.score           = Score.from10(puan)
                this.duration        = sure
                this.recommendations = oneriler
                addActors(oyuncular)
                addTrailer(fragman)
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl       = poster
                this.plot            = description
                this.year            = year
                this.tags            = etiketler
                this.score           = Score.from10(puan)
                this.duration        = sure
                this.recommendations = oneriler
                addActors(oyuncular)
                addTrailer(fragman)
            }
        }
    }

    // ── Links ───────────────────────────────────────────────────────────

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        Log.d("W2M", "data » $data")
        val sayfa = runCatching { app.get(data).text }.getOrNull() ?: return false

        // Oyuncu listesi sayfadaki inline script'te: const pl_url = '…/ajax/ajax.php?vdkz=…';
        val plUrl = Regex("""pl_url\s*=\s*['"]([^'"]+)['"]""").find(sayfa)?.groupValues?.get(1)
            ?: Regex("""['"]([^'"]*ajax/ajax\.php\?vdkz=[^'"]+)['"]""").find(sayfa)?.groupValues?.get(1)

        var found = false
        if (plUrl != null) {
            val adres = fixUrlNull(plUrl) ?: plUrl
            val html = runCatching {
                app.get(
                    adres,
                    referer = data,
                    headers = mapOf("X-Requested-With" to "XMLHttpRequest")
                ).text
            }.getOrNull()

            if (html != null) {
                val sunucular = org.jsoup.Jsoup.parse(html, adres)
                    .select("a.link-item[data-id], a[data-id]")

                for (sunucu in sunucular) {
                    val videoUrl = sunucu.attr("data-id").trim()
                    if (videoUrl.isBlank()) continue
                    Log.d("W2M", "sunucu ${sunucu.attr("data-srv")} » $videoUrl")
                    if (loadExtractor(videoUrl, "$mainUrl/", subtitleCallback, callback)) found = true
                }
            }
        }

        // Yedek: sayfadaki doğrudan iframe'ler
        if (!found) {
            val document = org.jsoup.Jsoup.parse(sayfa, data)
            for (iframe in document.select("iframe[src], iframe[data-src]").map { it.attr("src").ifBlank { it.attr("data-src") } }) {
                val adres = fixUrlNull(iframe) ?: continue
                if (loadExtractor(adres, data, subtitleCallback, callback)) found = true
            }
        }

        return found
    }
}
