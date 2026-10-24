package com.panates

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.JsUnpacker
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newExtractorLink
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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
        // Sentinelled endpoint: getMainPage() POSTs an empty body (site requires Content-Length: 0)
        "$mainUrl/ajax/dataDefaultSonCikan.asp?d=1&k=0&s=" to "Son Bölümler",
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
        if (request.data.contains("dataDefaultSonCikan")) {
            return newHomePageResponse(request.name, parseLatestEpisodes(page))
        }

        val doc = try {
            app.get("${request.data}${page}").document
        } catch (_: Exception) {
            return newHomePageResponse(request.name, emptyList())
        }

        return newHomePageResponse(request.name, parseShowCards(doc))
    }

    // ── Latest Episodes ("Son Bölümler") ─────────────────────────────────

    /**
     * POST /ajax/dataDefaultSonCikan.asp?d=1&k=0&s=<page> — the site only serves pages 1..5,
     * 15 cards each. Each card is an episode, so it links to the series detail page and
     * carries "Series - 2.Sezon 6.Bölüm" as its title (MainAPI has no episode-search response).
     */
    private suspend fun parseLatestEpisodes(page: Int): List<SearchResponse> {
        if (page < 1 || page > 5) return emptyList()

        val html = try {
            app.post(
                "$mainUrl/ajax/dataDefaultSonCikan.asp?d=1&k=0&s=$page",
                headers = mapOf(
                    "X-Requested-With" to "XMLHttpRequest",
                    "Referer" to "$mainUrl/"
                ),
                requestBody = ByteArray(0).toRequestBody("application/x-www-form-urlencoded".toMediaType())
            ).text
        } catch (_: Exception) {
            return emptyList()
        }

        val doc = Jsoup.parse(html)
        return doc.select("div.ui.card.golgever").mapNotNull { card ->
            val link = card.selectFirst("a[href]") ?: return@mapNotNull null
            val href = link.attr("href")
            // /mobland/2-sezon-6-bolum.html -> slug "mobland" -> series page
            val slug = Regex("""/?([^/]+)/\d+-sezon-\d+-bolum\.html""")
                .find(href)?.groupValues?.get(1) ?: return@mapNotNull null

            val seriesTitle = card.selectFirst("div.box-title span.title")?.text()?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: link.attr("title").removeSuffix(" izle").trim().takeIf { it.isNotBlank() }
                ?: return@mapNotNull null

            val seep = card.selectFirst("div.box-title span.seep")?.text()?.trim().orEmpty()
            val title = if (seep.isBlank()) seriesTitle else "$seriesTitle - $seep"

            val posterUrl = card.selectFirst("img")?.let { img ->
                val src = img.attr("data-src").ifEmpty { img.attr("src") }
                when {
                    src.isBlank() || src.startsWith("data:") -> null
                    src.startsWith("/") -> "$mainUrl$src"
                    else -> src
                }
            }

            newTvSeriesSearchResponse(title, "$mainUrl/diziler/$slug.html", TvType.TvSeries) {
                this.posterUrl = posterUrl
            }
        }
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
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        // The site's search only matches short phrases: 4+ words return nothing
        // ("regular show the lost tapes" -> empty, "regular show" -> results).
        var results = searchOnce(trimmed)
        if (results.isEmpty()) {
            val words = trimmed.split(Regex("\\s+"))
            for (n in minOf(words.size - 1, 3) downTo 1) {
                results = searchOnce(words.take(n).joinToString(" "))
                if (results.isNotEmpty()) break
            }
        }
        return results
    }

    private suspend fun searchOnce(query: String): List<SearchResponse> {
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

        // Trailer: the series JSON-LD may nest the VideoObject under "@graph",
        // e.g. {"@graph":[{"@type":"TVSeries","trailer":[{"embedUrl":...}]}]}
        val trailerUrl = runCatching {
            doc.select("script[type=application/ld+json]")
                .mapNotNull { script -> script.data().takeIf { it.contains("\"trailer\"") } }
                .firstOrNull()
                ?.let { ObjectMapper().readTree(it) }
                ?.let { findTrailerUrl(it) }
        }.getOrNull()

        val response = newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
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

        if (!trailerUrl.isNullOrBlank()) {
            runCatching { response.addTrailer(trailerUrl) }
        }

        return response
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

        // Hosts/names to skip (pixel, netu as requested; dzen resolved via bundled DzenSez extractor)
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

    /** Walks the JSON-LD tree (including "@graph") for the first non-empty `trailer` array. */
    private fun findTrailerUrl(root: JsonNode): String? {
        val queue = ArrayDeque<JsonNode>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.isObject) {
                val trailer = node.path("trailer")
                if (trailer.isArray && trailer.size() > 0) {
                    val item = trailer.first()
                    val url = item.path("embedUrl").asText(null)?.takeIf { it.isNotBlank() }
                        ?: item.path("url").asText(null)?.takeIf { it.isNotBlank() }
                    if (!url.isNullOrBlank()) return url
                }
            }
            node.forEach { queue.add(it) }
        }
        return null
    }

    private fun renameLink(link: ExtractorLink, newName: String): ExtractorLink = ExtractorLink(
        // source must be the label too: the player groups links by source, keeping the
        // extractor's generic name would merge Altyazı/Dublaj entries of the same host.
        source = newName,
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
