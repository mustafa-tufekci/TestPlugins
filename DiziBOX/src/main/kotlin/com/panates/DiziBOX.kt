package com.panates

import android.util.Base64
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.StringUtils.decodeUri
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class DiziBox : MainAPI() {
    override var mainUrl = "https://www.dizibox.live"
    override var name = "DiziBox"
    override val supportedTypes = setOf(TvType.TvSeries)
    override var lang = "tr"
    override val hasMainPage = true

    // Cloudflare bypass — request rows sequentially with a small delay
    override var sequentialMainPage = true
    override var sequentialMainPageDelay = 50L
    override var sequentialMainPageScrollDelay = 50L

    private val cloudflareKiller by lazy { CloudflareKiller() }
    private val interceptor by lazy { CloudflareInterceptor(cloudflareKiller) }

    class CloudflareInterceptor(private val cloudflareKiller: CloudflareKiller) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)
            val doc = Jsoup.parse(response.peekBody(1024 * 1024).string())

            if (doc.text().contains("Güvenlik taramasından geçiriliyorsunuz. Lütfen bekleyiniz..")) {
                return cloudflareKiller.intercept(chain)
            }

            return response
        }
    }

    private val authCookies = mapOf(
        "LockUser" to "true",
        "isTrustedUser" to "true",
        "dbxu" to "1744009162326"
    )

    private suspend fun getDoc(url: String, referer: String = "$mainUrl/"): Document =
        app.get(url, referer = referer, cookies = authCookies, interceptor = interceptor).document

    private suspend fun getText(url: String, referer: String = "$mainUrl/"): String =
        app.get(url, referer = referer, cookies = authCookies, interceptor = interceptor).text

    // ── Main Page ────────────────────────────────────────────────────────

    override val mainPage = mainPageOf(
        "$mainUrl/" to "Beklenen / Eklenen Diziler",
        "$mainUrl/efsane-diziler/" to "Efsane Diziler",
        "$mainUrl/tum-bolumler/page/SAYFA/" to "Yeni Eklenen Bölümler",
        "$mainUrl/tum-bolumler/page/SAYFA/?tip=populer" to "Popüler Dizilerden Son Bölümler",
        "$mainUrl/dizi-arsivi/page/SAYFA/" to "Dizi Arşivi",
        "$mainUrl/dizi-arsivi/page/SAYFA/?ulke%5B%5D=turkiye&yil=&imdb" to "Yerli",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=aile&yil&imdb" to "Aile",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=aksiyon&yil&imdb" to "Aksiyon",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=animasyon&yil&imdb" to "Animasyon",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=belgesel&yil&imdb" to "Belgesel",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=bilimkurgu&yil&imdb" to "Bilimkurgu",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=biyografi&yil&imdb" to "Biyografi",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=dram&yil&imdb" to "Dram",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=drama&yil&imdb" to "Drama",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=fantastik&yil&imdb" to "Fantastik",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=gerilim&yil&imdb" to "Gerilim",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=gizem&yil&imdb" to "Gizem",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=komedi&yil&imdb" to "Komedi",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=korku&yil&imdb" to "Korku",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=macera&yil&imdb" to "Macera",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=reality-tv&yil&imdb" to "Reality TV",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=romantik&yil&imdb" to "Romantik",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=savas&yil&imdb" to "Savaş",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=suc&yil&imdb" to "Suç",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=tarih&yil&imdb" to "Tarih",
        "$mainUrl/dizi-arsivi/page/SAYFA/?tur%5B0%5D=western&yil&imdb" to "Western"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = request.data.replace("SAYFA", "$page")

        val document = try {
            getDoc(url)
        } catch (_: Exception) {
            return newHomePageResponse(request.name, emptyList())
        }

        val items: List<SearchResponse> = try {
            when (request.name) {
                "Beklenen / Eklenen Diziler" -> parseRecommended(document)
                "Efsane Diziler" -> parseEfsane(document)
                "Yeni Eklenen Bölümler",
                "Popüler Dizilerden Son Bölümler" ->
                    document.select("article.article-episode-card").mapNotNull { it.toEpisodeCardResult() }

                else -> document.select("article.detailed-article").mapNotNull { it.toDetailResult() }
            }.distinctBy { it.url }
        } catch (e: Exception) {
            emptyList()
        }

        return newHomePageResponse(request.name, items)
    }

    // #recommended_series — homepage "Beklenen / Eklenen Diziler"
    private fun parseRecommended(document: Document): List<SearchResponse> {
        return document.select("#recommended_series li").mapNotNull { li ->
            val link = li.selectFirst("a[href]") ?: return@mapNotNull null
            val href = fixUrlNull(link.attr("href")) ?: return@mapNotNull null
            val title = link.selectFirst("span.baslik")?.text()?.trim()
                ?: link.attr("title").substringBefore(" ").trim()
            if (title.isBlank()) return@mapNotNull null
            val posterUrl = link.selectFirst("img")?.let {
                fixUrlNull(it.attr("data-src").ifEmpty { it.attr("src") })
            }
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
            }
        }
    }

    // article.article-series-poster — /efsane-diziler/
    private fun parseEfsane(document: Document): List<SearchResponse> {
        return document.select("article.article-series-poster").mapNotNull { article ->
            val titleLink = article.selectFirst("a.poster-title") ?: return@mapNotNull null
            val href = fixUrlNull(titleLink.attr("href")) ?: return@mapNotNull null
            val title = titleLink.text().trim()
            if (title.isBlank()) return@mapNotNull null
            val imdbRaw = article.selectFirst("div.imdb")?.text()
                ?.replace(Regex("[^0-9.]"), "")?.trim()
            val year = article.selectFirst("div.release")?.text()
                ?.replace(Regex("[^0-9]"), "")?.trim()?.toIntOrNull()
            val posterUrl = article.selectFirst("img.afis")?.let {
                fixUrlNull(it.attr("data-src").ifEmpty { it.attr("src") })
            }
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
                this.year = year
                if (!imdbRaw.isNullOrBlank()) this.score = runCatching { Score.from10(imdbRaw) }.getOrNull()
            }
        }
    }

    // article.detailed-article — /dizi-arsivi/ and search results
    private fun Element.toDetailResult(): SearchResponse? {
        val titleEl = selectFirst("h3 a") ?: return null
        val href = fixUrlNull(titleEl.attr("href")) ?: return null
        val title = titleEl.text().trim()
        if (title.isBlank()) return null

        val img = selectFirst("figure img")
        val posterUrl = img?.let { fixUrlNull(it.attr("src").ifEmpty { it.attr("data-src") }) }

        val yearText = selectFirst(".custom-field:has(i.icon-globe)")?.text()
        val year = yearText?.replace(Regex("[^0-9]"), "")?.take(4)?.toIntOrNull()

        val imdbRaw = selectFirst("span.label-imdb b, .label-imdb b")?.text()
            ?.replace(Regex("[^0-9.]"), "")
            ?.trim()

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
            this.posterUrl = posterUrl
            this.year = year
            if (!imdbRaw.isNullOrBlank()) this.score = runCatching { Score.from10(imdbRaw) }.getOrNull()
        }
    }

    // article.article-episode-card — /tum-bolumler/ (links go to the episode page,
    // load() resolves the series URL through a.archive-title)
    private fun Element.toEpisodeCardResult(): SearchResponse? {
        val link = selectFirst("a[href]") ?: return null
        val href = fixUrlNull(link.attr("href")) ?: return null

        val fullTitle = link.attr("title").trim()
            .ifEmpty { selectFirst("a[title]")?.attr("title")?.trim() ?: "" }

        val seasonMatch = Regex("""(\d+)\s*\.\s*[Ss]ezon""").find(fullTitle)
        val episodeMatch = Regex("""(\d+)\s*\.\s*[Bb][oö]l[uü]m""").find(fullTitle)

        val seriesName = if (seasonMatch != null) {
            fullTitle.substring(0, seasonMatch.range.first).trim()
        } else {
            selectFirst("b.series-name")?.text()?.trim()
        }?.trim().orEmpty()

        if (seriesName.isBlank()) return null

        val season = seasonMatch?.groupValues?.get(1)
        val episode = episodeMatch?.groupValues?.get(1)

        val title = if (season != null && episode != null) "$seriesName - ${season}x$episode"
        else seriesName

        val posterUrl = selectFirst("img[data-src], img[src]")?.let {
            fixUrlNull(it.attr("data-src").ifEmpty { it.attr("src") })
        }?.replace("220x140", "200x290")

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
            this.posterUrl = posterUrl
        }
    }

    // ── Search ───────────────────────────────────────────────────────────

    override suspend fun search(query: String): List<SearchResponse> {
        return try {
            app.post(
                "$mainUrl/",
                data = mapOf("s" to query),
                cookies = authCookies,
                interceptor = interceptor
            ).document.select("article.detailed-article").mapNotNull { it.toDetailResult() }
                .distinctBy { it.url }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ── Load ─────────────────────────────────────────────────────────────

    override suspend fun load(url: String): LoadResponse {
        var finalUrl = url
        var doc = try {
            getDoc(url)
        } catch (e: Exception) {
            throw e
        }

        // Episode pages link back to their series page through a.archive-title
        val archiveHref = doc.selectFirst("a.archive-title")?.attr("href")
        if (!archiveHref.isNullOrBlank()) {
            val seriesUrl = fixUrlNull(archiveHref)
            if (!seriesUrl.isNullOrBlank() && seriesUrl != finalUrl) {
                try {
                    doc = getDoc(seriesUrl)
                    finalUrl = seriesUrl
                } catch (_: Exception) {}
            }
        }

        val title = doc.selectFirst("div.tv-overview h1 a")?.text()?.trim()
            ?: doc.selectFirst("div.tv-overview h1, h1.cat-title")?.text()?.trim()
            ?: doc.title()?.substringBefore(" - DiziBOX")?.trim()
            ?: "Unknown"

        val posterUrl = doc.selectFirst("div.tv-overview figure img, .category-cover img")?.let {
            fixUrlNull(it.attr("src").ifEmpty { it.attr("data-src") })
        }

        val plot = doc.selectFirst("div.tv-story p, .tv-overview .text-muted-darker, .tv-overview p")
            ?.text()?.trim()

        val tags = doc.select("a[href*='/tur/']").mapNotNull { it.text().trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        val year = doc.selectFirst("a[href*='/yil/']")?.text()
            ?.replace(Regex("[^0-9]"), "")?.take(4)?.toIntOrNull()

        val rating = doc.selectFirst("span.label-imdb b, .label-imdb b")?.text()
            ?.replace(",", ".")
            ?.replace(Regex("[^0-9.]"), "")
            ?.toDoubleOrNull()

        val actors = doc.select("a[href*='/oyuncu/']").map { it.text().trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .map { Actor(it) }

        val trailer = doc.selectFirst("#trailer-box iframe")?.attr("src")
            ?: doc.selectFirst("div.tv-overview iframe")?.attr("src")
            ?.takeIf { it.contains("youtu") }

        val episodes = mutableListOf<Episode>()
        val firstSeasonEpisodes = parseEpisodesFromGrid(doc)
        episodes.addAll(firstSeasonEpisodes)

        // Season tabs — first season is on the current page, rest need separate fetches
        val seasonLinks = doc.select("#seasons-list a[href]")
            .mapNotNull { fixUrlNull(it.attr("href")) }
            .filter { it.startsWith("http") }
            .distinct()

        val remainingSeasons = if (firstSeasonEpisodes.isNotEmpty()) seasonLinks.drop(1) else seasonLinks
        for (seasonUrl in remainingSeasons) {
            try {
                episodes.addAll(parseEpisodesFromGrid(getDoc(seasonUrl)))
            } catch (_: Exception) {}
        }

        return newTvSeriesLoadResponse(title, finalUrl, TvType.TvSeries, episodes.distinctBy { it.data }) {
            this.posterUrl = posterUrl
            this.plot = plot
            this.year = year
            this.tags = tags
            this.score = rating?.let { runCatching { Score.from10(it) }.getOrNull() }
            addActors(actors)
            addTrailer(trailer)
        }
    }

    // ── Load Links ───────────────────────────────────────────────────────

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = getDoc(data)

        val options = doc.select("select.woca-linkpages-dd option")
        val sources = mutableListOf<VideoSource>()
        if (options.isEmpty()) {
            sources.add(VideoSource("Varsayılan", data))
        } else {
            for (option in options) {
                val sourceName = option.text().trim()
                if (sourceName.isEmpty()) continue
                val pageUrl = option.attr("value").trim()
                    .ifEmpty { option.attr("href").trim() }
                    .ifEmpty { data }
                sources.add(VideoSource(sourceName, fixUrl(pageUrl)))
            }
        }

        var found = false
        for (source in sources) {
            try {
                val sourceDoc = if (source.url == data) doc else getDoc(source.url)

                // Source display name from HTML comment <!--baslik:...-->
                val label = Regex("<!--baslik:(.*?)-->").find(sourceDoc.html())
                    ?.groupValues?.get(1)?.trim()
                    ?: source.name

                var iframeSrc = sourceDoc.selectFirst("#video-area iframe")?.attr("src")
                    ?: sourceDoc.select("iframe[src]").firstOrNull { el ->
                        val s = el.attr("src")
                        listOf("player", "king", "moly", "haydi", "mecnun").any { s.contains(it) }
                    }?.attr("src")

                if (iframeSrc.isNullOrBlank()) continue
                if (iframeSrc.startsWith("/")) iframeSrc = fixUrl(iframeSrc)

                if (iframeDecode(source.url, iframeSrc, label, subtitleCallback, callback)) {
                    found = true
                }
            } catch (_: Exception) {}
        }

        return found
    }

    /**
     * Resolve a dizibox player iframe to actual streams.
     * king.php  → molystream embed → CryptoJS AES → M3U8
     * moly.php  → atob(unescape(...)) → #Player iframe → loadExtractor
     * haydi.php → #Player iframe → loadExtractor
     */
    private suspend fun iframeDecode(
        data: String,
        iframeIn: String,
        label: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        var iframe = iframeIn

        if (iframe.contains("king.php")) {
            iframe = iframe.replace("king.php?v=", "king.php?wmode=opaque&v=")

            val subDoc = getDoc(iframe, referer = data)
            val subFrame = subDoc.selectFirst("div#Player iframe")?.attr("src")?.let {
                if (it.startsWith("/")) fixUrl(it) else it
            } ?: return false

            val iDoc = getText(subFrame, referer = "$mainUrl/")
            val payload = parseCryptoPayload(iDoc) ?: return false

            val decrypted = runCatching {
                CryptoJS.decrypt(payload.password, payload.cipherText)
            }.getOrNull() ?: return false

            val vidUrl = Regex("""file:\s*'([^']+)'""").find(decrypted)?.groupValues?.get(1)
                ?: Regex("""file:\s*"([^"]+)"""").find(decrypted)?.groupValues?.get(1)
                ?: return false

            callback(
                newExtractorLink(
                    source = name,
                    name = label,
                    url = vidUrl,
                    ExtractorLinkType.M3U8
                ) {
                    this.referer = "https://dbx.molystream.org/"
                    this.headers = mapOf(
                        "Referer" to "https://dbx.molystream.org/",
                        "Origin" to "https://dbx.molystream.org/"
                    )
                    this.quality = Qualities.P1080.value
                }
            )
            return true
        }

        if (iframe.contains("moly.php") || iframe.contains("haydi.php") || iframe.contains("mecnun.php")) {
            iframe = iframe
                .replace("moly.php?h=", "moly.php?wmode=opaque&h=")
                .replace("haydi.php?v=", "haydi.php?wmode=opaque&v=")
                .replace("mecnun.php?v=", "mecnun.php?wmode=opaque&v=")

            var subDoc = getDoc(iframe, referer = data)

            // moly.php wraps its HTML in document.write(atob(unescape("...")))
            val atobData = Regex("""unescape\("([^"]+)"\)""").find(subDoc.html())?.groupValues?.get(1)
            if (atobData != null) {
                runCatching {
                    val decoded = String(
                        Base64.decode(atobData.decodeUri(), Base64.DEFAULT),
                        Charsets.UTF_8
                    )
                    subDoc = Jsoup.parse(decoded)
                }
            }

            var subFrame = subDoc.selectFirst("div#Player iframe")?.attr("src")
                ?: subDoc.selectFirst("iframe[src]")?.attr("src")
            if (subFrame.isNullOrBlank()) return false
            if (subFrame.startsWith("/")) subFrame = fixUrl(subFrame)
            if (subFrame.startsWith("//")) subFrame = "https:$subFrame"
            if (subFrame.contains("vidmoly.net")) subFrame = subFrame.replace("vidmoly.net", "vidmoly.biz")

            if (loadExtractor(subFrame, "$mainUrl/", subtitleCallback) { link ->
                    callback(renameLink(link, "$label - ${link.name}"))
                }
            ) return true

            return scrapeDirectStream(subFrame, label, callback)
        }

        // Direct stream url
        if (iframe.contains(".m3u8") || iframe.contains(".mp4")) {
            val isM3u8 = iframe.contains(".m3u8")
            callback(
                ExtractorLink(
                    source = label,
                    name = "$label - Direct",
                    url = iframe,
                    referer = data,
                    quality = Qualities.P1080.value,
                    type = if (isM3u8) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                )
            )
            return true
        }

        // Generic fallback
        return loadExtractor(iframe, "$mainUrl/", subtitleCallback) { link ->
            callback(renameLink(link, "$label - ${link.name}"))
        }
    }

    private fun parseCryptoPayload(html: String): CryptoPayload? {
        val match = Regex("""CryptoJS\.AES\.decrypt\("([^"]+)","([^"]+)"\)""").find(html)
            ?: return null
        return CryptoPayload(
            cipherText = match.groupValues[1],
            password = match.groupValues[2]
        )
    }

    private suspend fun scrapeDirectStream(
        embedUrl: String,
        label: String,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return try {
            val html = app.get(embedUrl, referer = "$mainUrl/").text
            val m3u8 = Regex("""['"](https?://[^'"]+\.m3u8[^'"]*)['"]""").find(html)
                ?.groupValues?.get(1)
            if (m3u8.isNullOrBlank()) return false

            callback(
                ExtractorLink(
                    source = "Vidmoly",
                    name = "$label - Vidmoly HLS",
                    url = m3u8,
                    referer = "https://vidmoly.biz/",
                    quality = Qualities.P1080.value,
                    headers = mapOf("Referer" to "https://vidmoly.biz/"),
                    type = ExtractorLinkType.M3U8
                )
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun renameLink(link: ExtractorLink, newName: String): ExtractorLink = ExtractorLink(
        source = link.source ?: name,
        name = newName,
        url = link.url ?: "",
        referer = link.referer ?: "$mainUrl/",
        quality = link.quality,
        headers = link.headers ?: emptyMap(),
        extractorData = link.extractorData,
        type = link.type,
        audioTracks = link.audioTracks ?: emptyList()
    )

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Parse episodes from a season/series page grid.
     * Episode link text format confirmed: "1.Sezon 1.Bölüm" (mixed case, Turkish chars)
     */
    private fun parseEpisodesFromGrid(doc: Document): List<Episode> {
        return doc.select("#category-posts article.grid-box.grid-four").mapNotNull { article ->
            val link = article.selectFirst("a.season-episode") ?: return@mapNotNull null
            val href = link.attr("href")
            if (href.isBlank()) return@mapNotNull null

            val fullUrl = fixUrl(href)
            val linkText = link.text().trim()

            // Regex covers: "1.Sezon 1.Bölüm", "1.sezon 1.bolum", "1.Sezon 1.Bolüm" etc.
            val match = Regex(
                "(\\d+)\\s*\\.\\s*[Ss]ezon\\s+(\\d+)\\s*\\.\\s*[Bb][oö]l[uü]m",
                RegexOption.IGNORE_CASE
            ).find(linkText)
            val season = match?.groupValues?.get(1)?.toIntOrNull()
            val episode = match?.groupValues?.get(2)?.toIntOrNull()

            val epTitle = article.selectFirst(".post-title a:not(.season-episode)")?.text()?.trim()
                ?: linkText

            newEpisode(fullUrl) {
                this.name = epTitle
                this.season = season
                this.episode = episode
            }
        }
    }
}
