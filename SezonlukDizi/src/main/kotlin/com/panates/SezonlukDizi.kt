package com.panates

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.JsUnpacker
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.jsoup.Jsoup

class SezonlukDizi : MainAPI() {
    override var mainUrl = "https://sezonlukdizi.cc"
    override var name = "SezonlukDizi"
    override val supportedTypes = setOf(TvType.TvSeries, TvType.Anime)
    override var lang = "tr"
    override val hasMainPage = true

    private val ajaxHeaders = mapOf(
        "X-Requested-With" to "XMLHttpRequest",
        "Content-Type" to "application/x-www-form-urlencoded"
    )

    override val mainPage = mainPageOf(
        "$mainUrl/diziler.asp?siralama_tipi=id&s=" to "Son Eklenenler",
        "$mainUrl/diziler.asp?siralama_tipi=id&tur=mini&s=" to "Mini Diziler",
        "$mainUrl/diziler.asp?siralama_tipi=id&kat=2&s=" to "Yerli Diziler",
        "$mainUrl/diziler.asp?siralama_tipi=id&kat=1&s=" to "Yabancı Diziler",
        "$mainUrl/diziler.asp?siralama_tipi=id&kat=3&s=" to "Asya Dizileri",
        "$mainUrl/diziler.asp?siralama_tipi=id&kat=4&s=" to "Animasyonlar",
        "$mainUrl/diziler.asp?siralama_tipi=id&kat=5&s=" to "Animeler",
        "$mainUrl/diziler.asp?siralama_tipi=id&kat=6&s=" to "Belgeseller"
    )

    // ── Main Page ────────────────────────────────────────────────────────

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = try {
            app.get("${request.data}${page}").document
        } catch (_: Exception) {
            return newHomePageResponse(request.name, emptyList())
        }

        return newHomePageResponse(request.name, parseShowCards(doc))
    }

    // ── Card Parser ──────────────────────────────────────────────────────

    /**
     * Parse series cards from listing pages.
     * Confirmed selector from live HTML: href attribute comes before class in the tag,
     * so we match both orderings.
     * Card structure: <a href="..." class="column" title="..."><div class="ui card">...
     */
    private fun parseShowCards(doc: org.jsoup.nodes.Document): List<SearchResponse> {
        return doc.select("a.column[title]").mapNotNull { link ->
            val card = link.selectFirst("div.ui.card") ?: return@mapNotNull null
            val href = link.attr("href")
            if (href.isBlank()) return@mapNotNull null
            val fullUrl = fixUrl(href)

            val title = card.selectFirst(".content .description")?.text()
                ?: link.attr("title").removeSuffix(" izle").trim()

            val img = card.selectFirst("img[data-src], img[src]")
            val posterUrl = img?.let {
                val src = it.attr("data-src").ifEmpty { it.attr("src") }
                when {
                    src.startsWith("data:") -> null
                    src.startsWith("/") -> "$mainUrl$src"
                    src.isBlank() -> null
                    else -> src
                }
            }

            // <span class="imdbp"><b>IMDb</b> 6,9</span>
            val scoreText = card.selectFirst("span.imdbp")?.text()
                ?.replace("IMDb", "")?.trim()
                ?.replace(",", ".")
                ?.replace(Regex("[^0-9.]"), "")
                ?.trim()

            newTvSeriesSearchResponse(title, fullUrl, TvType.TvSeries) {
                this.posterUrl = posterUrl
                if (!scoreText.isNullOrBlank()) {
                    this.score = runCatching { Score.from10(scoreText) }.getOrNull()
                }
            }
        }
    }

    // ── Search ───────────────────────────────────────────────────────────

    override suspend fun search(query: String): List<SearchResponse> {
        return try {
            val response = app.post(
                "$mainUrl/ajax/arama.asp",
                data = mapOf("q" to query),
                headers = ajaxHeaders
            ).parsedSafe<SearchApiResponse>()

            response?.results?.diziler?.results?.mapNotNull { item ->
                val href = item.url ?: return@mapNotNull null
                val title = item.title ?: return@mapNotNull null
                val posterUrl = item.image?.let { img ->
                    if (img.startsWith("/")) "$mainUrl$img" else img
                }
                newTvSeriesSearchResponse(title, fixUrl(href), TvType.TvSeries) {
                    this.posterUrl = posterUrl
                }
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ── Load ─────────────────────────────────────────────────────────────

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document

        // Title: confirmed selector ".content .header" from live test
        val title = doc.selectFirst(".content .header")?.text()?.trim()
            ?: doc.selectFirst("h1")?.text()?.trim()
            ?: "Unknown"

        val img = doc.selectFirst("img[data-src]")
        val posterUrl = img?.let {
            val src = it.attr("data-src")
            if (src.startsWith("/")) "$mainUrl$src" else src
        }

        val plot = doc.selectFirst("#tartismayorum-konu blockquote")?.text()

        // Year: confirmed "1999" appears in ".extra.content .right.floated" text
        val yearText = doc.selectFirst(".extra.content .right.floated")?.text()
        val year = yearText?.split("-")?.firstOrNull()?.trim()?.toIntOrNull()

        val genres = doc.select("a.ui.blue.label.golge").map { it.text() }.filter { it.isNotBlank() }

        // IMDB: "9,0" — replace comma with dot before parsing
        val imdbText = doc.selectFirst(".ui.label.imdb .detail")?.text()
            ?.replace(",", ".")?.replace(Regex("[^0-9.]"), "")

        // Duration: <span class="ui orange label golge">47 Dk.</span>
        val duration = doc.selectXpath("//span[contains(text(), 'Dk.')]").text().trim()
            .substringBefore(" Dk.").trim().toIntOrNull()

        val dataDizi = doc.selectFirst("#dizidetay")?.attr("data-dizi")
            ?: doc.selectFirst("[data-dizi]")?.attr("data-dizi")

        // Actors live on a separate page: /oyuncular/{endpoint}.html
        val actors = dataDizi?.let { slug ->
            try {
                app.get("$mainUrl/oyuncular/$slug.html").document
                    .select("div.ui.card.golgever")
                    .mapNotNull { card ->
                        val name = card.selectFirst("div.header")?.text()?.trim()
                            ?: return@mapNotNull null
                        val photo = card.selectFirst("img")?.let { i ->
                            val src = i.attr("src").ifEmpty { i.attr("data-src") }
                            when {
                                src.isBlank() || src.startsWith("data:") -> null
                                src.startsWith("/") -> "$mainUrl$src"
                                else -> src
                            }
                        }
                        Actor(name, photo)
                    }
                    .filter { it.name.isNotBlank() }
            } catch (_: Exception) {
                emptyList()
            }
        } ?: emptyList()

        val episodes = mutableListOf<Episode>()

        if (dataDizi != null) {
            val episodesUrl = "$mainUrl/bolumler/$dataDizi.html"
            try {
                val episodesDoc = app.get(episodesUrl).document

                episodesDoc.select("table[sid]").forEach { table ->
                    table.select("tr").forEach { row ->
                        val link = row.selectFirst("a[href*='-sezon-']") ?: return@forEach
                        val href = link.attr("href")
                        val fullUrl = fixUrl(href)

                        // Prefer the href: server-side encoding mangles "Bölüm" -> "Blm"
                        val match = Regex("(\\d+)-sezon-(\\d+)-bolum").find(href)
                            ?: return@forEach
                        val s = match.groupValues[1].toIntOrNull() ?: return@forEach
                        val e = match.groupValues[2].toIntOrNull() ?: return@forEach

                        val tds = row.select("td")
                        val epTitle = tds.getOrNull(3)?.text()?.trim()
                            ?: "$s. Sezon $e. Bölüm"

                        episodes.add(newEpisode(fullUrl) {
                            name = epTitle
                            season = s
                            episode = e
                            this.posterUrl = posterUrl
                        })
                    }
                }
            } catch (_: Exception) {}
        }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = posterUrl
            this.plot = plot
            this.year = year
            this.tags = genres
            this.duration = duration
            if (!imdbText.isNullOrBlank()) {
                this.score = try {
                    Score.from10(imdbText)
                } catch (_: Exception) {
                    null
                }
            }
            addActors(actors)
        }
    }

    // ── Load Links ───────────────────────────────────────────────────────

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document

        val episodeId = doc.selectFirst("#dilsec")?.attr("data-id") ?: return false

        val aspData = runCatching { getAspData() }
            .getOrElse { AspData(alternatif = "22", embed = "22") }

        val languages = listOf(
            "1" to "AltYazı",
            "0" to "Dublaj"
        )

        // Hosts/names to skip (pixel, netu as requested; dzen now resolved via global Dzen extractor)
        val skipHosts = listOf("reCAPTCHADATA")
        val skipNames = listOf("pixel", "netu")

        var found = false
        for ((dilCode, prefix) in languages) {
            try {
                val response = app.post(
                    "$mainUrl/ajax/dataAlternatif${aspData.alternatif}.asp",
                    data = mapOf("bid" to episodeId, "dil" to dilCode),
                    headers = ajaxHeaders
                ).parsedSafe<Kaynak>() ?: continue

                if (response.status != "success") continue

                for (veri in response.data) {
                    if (veri.baslik.lowercase() in skipNames) continue

                    try {
                        val embedHtml = app.post(
                            "$mainUrl/ajax/dataEmbed${aspData.embed}.asp",
                            data = mapOf("id" to veri.id.toString()),
                            headers = ajaxHeaders
                        ).text

                        if (skipHosts.any { embedHtml.contains(it, ignoreCase = true) }) continue

                        val embedDoc = Jsoup.parse(embedHtml)
                        val iframe = embedDoc.selectFirst("iframe") ?: continue
                        var src = iframe.attr("src")
                        if (src.isBlank()) continue
                        if (src.startsWith("//")) src = "https:$src"
                        if (src.startsWith("/")) src = fixUrl(src)

                        // vidmoly.net → vidmoly.biz (confirmed: site returns .net, .biz needed for playback)
                        if (src.contains("vidmoly.net")) {
                            src = src.replace("vidmoly.net", "vidmoly.biz")
                        }

                        val label = "$prefix - ${veri.baslik}"

                        if (src.contains("ruby", ignoreCase = true) &&
                            extractRuby(src, callback, veri, prefix)
                        ) {
                            found = true
                            continue
                        }

                        if (loadExtractor(src, mainUrl, subtitleCallback) { link ->
                                runCatching { callback(renameLink(link, label)) }
                            }
                        ) {
                            found = true
                        }
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }

        return found
    }

    // ── Private Helpers ──────────────────────────────────────────────────

    private fun renameLink(link: ExtractorLink, newName: String): ExtractorLink = ExtractorLink(
        source = link.source ?: newName,
        name = newName,
        url = link.url ?: "",
        referer = link.referer ?: mainUrl,
        quality = link.quality,
        headers = link.headers ?: emptyMap(),
        extractorData = link.extractorData,
        type = link.type,
        audioTracks = link.audioTracks ?: emptyList()
    )

    private suspend fun extractRuby(
        iframe: String,
        callback: (ExtractorLink) -> Unit,
        veri: Veri,
        dil: String
    ): Boolean {
        return try {
            val header = mapOf(
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0",
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                "Referer" to "$mainUrl/"
            )
            val son = app.get(iframe, referer = "$mainUrl/", headers = header)
                .document.select("script")
                .find { it.data().contains("function(p,a,c,k,e") }?.data()
                ?: return false

            val unPacked = JsUnpacker(son).unpack() ?: return false
            val file = unPacked.substringAfter("sources:[", "")
                .substringBefore("],")
                .addMarks("file")
            if (file.isBlank()) return false

            val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            val sonFile = objectMapper.readValue<Ruby>(file)
            if (sonFile.file.isBlank()) return false

            callback(
                newExtractorLink(
                    source = "$dil - ${veri.baslik}",
                    name = "$dil - ${veri.baslik}",
                    url = sonFile.file,
                    ExtractorLinkType.M3U8
                ) {
                    this.referer = "$mainUrl/"
                    this.quality = Qualities.Unknown.value
                }
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun String.addMarks(str: String): String {
        return this.replace(Regex("\"?$str\"?"), "\"$str\"")
    }

    //Helper function for getting the number (probably some kind of version?) after the dataAlternatif and dataEmbed
    private suspend fun getAspData(): AspData {
        val websiteCustomJavascript = app.get("$mainUrl/js/site.min.js").text
        val dataAlternatifAsp =
            Regex("""dataAlternatif(.*?)\.asp""").find(websiteCustomJavascript)?.groupValues?.get(1)
        val dataEmbedAsp =
            Regex("""dataEmbed(.*?)\.asp""").find(websiteCustomJavascript)?.groupValues?.get(1)
        return AspData(
            alternatif = dataAlternatifAsp?.takeIf { it.isNotBlank() } ?: "22",
            embed = dataEmbedAsp?.takeIf { it.isNotBlank() } ?: "22"
        )
    }
}
