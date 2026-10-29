package com.panates

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.loadExtractor
import org.json.JSONObject
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URLEncoder

class DiziBal : MainAPI() {
    override var mainUrl = "https://dizibal.org"
    override var name = "DiziBal"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries, TvType.Anime)
    override var lang = "tr"
    override val hasMainPage = true

    // Platform + genre rows (each page ships 48 poster cards, paginated via ?page=N)
    override val mainPage = mainPageOf(
        "/diziler" to "Diziler",
        "/filmler" to "Filmler",
        "/tur/animasyon" to "Animasyon",
        "/platform/netflix" to "Netflix",
        "/platform/disney" to "Disney+",
        "/platform/amazon-prime" to "Amazon Prime",
        "/platform/hbo-max" to "HBO Max",
        "/platform/exxen" to "Exxen",
        "/platform/tabii" to "tabii",
        "/tur/aksiyon" to "Aksiyon",
        "/tur/bilim-kurgu-fantastik" to "Bilim Kurgu & Fantastik",
        "/tur/belgesel" to "Belgesel",
        "/tur/aile" to "Aile"
    )

    companion object {
        private const val DEFAULT_PLAYER_BASE = "https://play2.pilavyerplay.top"

        private val defaultHeaders = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            "Referer" to "https://dizibal.org/"
        )
    }

    // ── Main Page ───────────────────────────────────────────────────────

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val sep = if (request.data.contains("?")) "&" else "?"
        val url = "$mainUrl${request.data}$sep" + "page=$page"

        val doc = try {
            app.get(url, headers = defaultHeaders).document
        } catch (_: Exception) {
            return newHomePageResponse(request.name, emptyList())
        }

        // Poster cards: a.group.block > img[poster], span.badge-rating, h3 (title), p (year)
        val cards = doc.select("a.group.block")
            .mapNotNull { it.toSearchResult() }
            .distinctBy { it.url }

        val items = if (cards.isNotEmpty()) cards else parseJsonLd(doc)
        val hasNext = cards.isNotEmpty() && doc.select("a[href*='page=']").any {
            Regex("""[?&]page=${page + 1}$""").containsMatchIn(it.attr("href"))
        }

        return newHomePageResponse(request.name, items, hasNext)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val href = attr("href").trim()
        if (href.isBlank()) return null
        val fullUrl = fixUrl(href)
        if (!fullUrl.contains("/movie/") && !fullUrl.contains("/series/") && !fullUrl.contains("/anime/")) return null

        val img = selectFirst("img")
        val rawTitle = selectFirst("h3")?.text()?.trim()?.takeIf { it.isNotBlank() }
            ?: img?.attr("alt")?.replace(Regex("(?i)\\s*(film|dizi|anime)\\s*izle\\s*$"), "")?.trim()
            ?: return null
        if (rawTitle.isBlank()) return null

        val poster = img?.attr("src")?.trim()?.takeIf { it.isNotBlank() }?.let { fixUrl(it) }
        val year = select("p").lastOrNull()?.text()
            ?.let { Regex("""(19|20)\d\d""").find(it)?.value?.toIntOrNull() }

        return when {
            fullUrl.contains("/movie/") -> newMovieSearchResponse(rawTitle, fullUrl, TvType.Movie) {
                this.posterUrl = poster
            }
            fullUrl.contains("/anime/") -> newTvSeriesSearchResponse(rawTitle, fullUrl, TvType.Anime) {
                this.posterUrl = poster
            }
            else -> newTvSeriesSearchResponse(rawTitle, fullUrl, TvType.TvSeries) {
                this.posterUrl = poster
                this.year = year
            }
        }
    }

    /** Fallback: Schema.org ItemList nodes (homepage "Trend Filmler" row). */
    private fun parseJsonLd(doc: Document): List<SearchResponse> {
        val out = mutableListOf<SearchResponse>()
        for (script in doc.select("script[type=application/ld+json]")) {
            val jsonText = script.html().trim()
            if (jsonText.isBlank()) continue
            try {
                collectItemLists(JSONObject(jsonText), out)
            } catch (_: Exception) {}
        }
        return out.distinctBy { it.url }
    }

    private fun collectItemLists(node: JSONObject, out: MutableList<SearchResponse>) {
        if (node.optString("@type") == "ItemList") {
            val items = node.optJSONArray("itemListElement")
            if (items != null) {
                for (j in 0 until items.length()) {
                    val item = items.optJSONObject(j) ?: continue
                    val title = item.optString("name").trim()
                    val url = item.optString("url").trim()
                    if (title.isBlank() || url.isBlank()) continue
                    val fullUrl = fixUrl(url)
                    val poster = item.optString("image").trim().takeIf { it.isNotBlank() }?.let { fixUrl(it) }
                    when {
                        fullUrl.contains("/movie/") -> out.add(
                            newMovieSearchResponse(title, fullUrl, TvType.Movie) { this.posterUrl = poster }
                        )
                        fullUrl.contains("/anime/") -> out.add(
                            newTvSeriesSearchResponse(title, fullUrl, TvType.Anime) { this.posterUrl = poster }
                        )
                        else -> out.add(
                            newTvSeriesSearchResponse(title, fullUrl, TvType.TvSeries) { this.posterUrl = poster }
                        )
                    }
                }
            }
        }
        val keys = node.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            when (val value = node.opt(key)) {
                is JSONObject -> collectItemLists(value, out)
                is org.json.JSONArray -> for (i in 0 until value.length()) {
                    value.optJSONObject(i)?.let { collectItemLists(it, out) }
                }
            }
        }
    }

    // ── Search ──────────────────────────────────────────────────────────

    override suspend fun search(query: String): List<SearchResponse> {
        val searchUrl = "$mainUrl/ara/oneri?q=${URLEncoder.encode(query.trim(), "UTF-8")}"

        val responseText = try {
            app.get(searchUrl, headers = defaultHeaders).text
        } catch (_: Exception) {
            return emptyList()
        }

        val results = mutableListOf<SearchResponse>()

        try {
            val json = JSONObject(responseText)

            // Movies
            val movies = json.optJSONArray("movies")
            if (movies != null) {
                for (i in 0 until movies.length()) {
                    val item = movies.optJSONObject(i) ?: continue
                    val title = item.optString("title").trim()
                    val url = item.optString("url").trim()
                    val poster = item.optString("poster").trim()
                    if (title.isBlank() || url.isBlank()) continue

                    val fullUrl = fixUrl(url)
                    results.add(
                        newMovieSearchResponse(title, fullUrl, TvType.Movie) {
                            this.posterUrl = poster.takeIf { it.isNotBlank() }?.let { fixUrl(it) }
                        }
                    )
                }
            }

            // Series
            val series = json.optJSONArray("series")
            if (series != null) {
                for (i in 0 until series.length()) {
                    val item = series.optJSONObject(i) ?: continue
                    val title = item.optString("title").trim()
                    val url = item.optString("url").trim()
                    val poster = item.optString("poster").trim()
                    if (title.isBlank() || url.isBlank()) continue

                    val fullUrl = fixUrl(url)
                    val tvType = if (fullUrl.contains("/anime/")) TvType.Anime else TvType.TvSeries
                    results.add(
                        newTvSeriesSearchResponse(title, fullUrl, tvType) {
                            this.posterUrl = poster.takeIf { it.isNotBlank() }?.let { fixUrl(it) }
                        }
                    )
                }
            }
        } catch (_: Exception) {}

        return results.distinctBy { it.url }
    }

    // ── Load Details ────────────────────────────────────────────────────

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url, headers = defaultHeaders).document
        val ld = findSchemaNode(doc)

        val rawTitle = ld?.optString("name")?.trim()?.takeIf { it.isNotBlank() }
            ?: doc.selectFirst("h1")?.text()?.trim()
            ?: doc.selectFirst("meta[property='og:title']")?.attr("content")?.trim()
            ?: "İçerik"
        val title = cleanTitle(rawTitle)

        val poster = ld?.optString("image")?.trim()?.takeIf { it.isNotBlank() }?.let { fixUrl(it) }
            ?: doc.selectFirst("meta[property='og:image']")?.attr("content")?.takeIf { it.isNotBlank() }?.let { fixUrl(it) }
            ?: doc.selectFirst(".tb-slot img, img[src*='/posters/']")?.attr("src")?.takeIf { it.isNotBlank() }
            ?.let { fixUrl(it) }

        val plot = ld?.optString("description")?.trim()?.takeIf { it.isNotBlank() }
            ?: doc.selectFirst("meta[name='description']")?.attr("content")?.trim()
            ?: doc.selectFirst("p.text-content-secondary")?.text()?.trim()

        // Movie -> datePublished, series -> startDate
        val dateText = ld?.optString("datePublished")?.takeIf { it.isNotBlank() }
            ?: ld?.optString("startDate")?.takeIf { it.isNotBlank() }
        val year = dateText?.let { Regex("""(19|20)\d\d""").find(it)?.value?.toIntOrNull() }
            ?: Regex("""(19|20)\d\d""").find(doc.text())?.value?.toIntOrNull()

        val duration = parseIsoDuration(ld?.optString("duration"))

        val tags = ld?.optJSONArray("genre")?.let { arr ->
            (0 until arr.length()).mapNotNull { arr.optString(it).trim().takeIf { g -> g.isNotBlank() } }
        }?.takeIf { it.isNotEmpty() }
            ?: doc.select("a[href*='/tur/']").mapNotNull { it.text().trim().takeIf { t -> t.isNotBlank() } }

        val rating = ld?.optJSONObject("aggregateRating")
        val score = rating?.optDouble("ratingValue", 0.0)?.takeIf { it > 0 }
            ?: Regex("""(\d+(?:[.,]\d+)?)""")
                .find(doc.selectFirst(".text-star")?.parent()?.text().orEmpty())
                ?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()

        // Backdrop + trailer from the JSON-LD VideoObject / trailer node
        val trailerNode = ld?.optJSONObject("trailer")
        val background = trailerNode?.optString("thumbnailUrl")?.trim()?.takeIf { it.isNotBlank() }?.let { fixUrl(it) }
        val trailerUrl = trailerNode?.optString("embedUrl")?.trim()?.takeIf { it.isNotBlank() }

        // Cast photos live outside the JSON-LD (img[src*='/storage/tmdb/profiles'])
        val actors = doc.select("img[src*='/storage/tmdb/profiles']").mapNotNull {
            val actorName = it.attr("alt").trim()
            if (actorName.isBlank()) return@mapNotNull null
            ActorData(Actor(actorName, fixUrl(it.attr("src"))))
        }.distinctBy { it.actor?.name }

        val isMovie = url.contains("/movie/")

        if (isMovie) {
            val watchUrl = if (url.endsWith("/izle")) url else "${url.removeSuffix("/")}/izle"
            val episodes = listOf(
                newEpisode(watchUrl) {
                    this.name = title
                    this.season = 1
                    this.episode = 1
                    this.posterUrl = poster
                }
            )

            return newMovieLoadResponse(title, url, TvType.Movie, episodes) {
                this.posterUrl = poster
                this.backgroundPosterUrl = background
                this.plot = plot
                this.year = year
                this.tags = tags
                this.duration = duration
                this.actors = actors
                this.score = score?.let { Score.from10(it) }
                trailerUrl?.let { this.trailers = mutableListOf(TrailerData(it, mainUrl, false)) }
            }
        }

        // TV Series or Anime: Extract all seasons and episodes
        val cleanBaseUrl = url.split("?").first().removeSuffix("/")

        // Discover available seasons from season tabs (e.g. ?sezon=2#bolumler)
        val seasonNumbers = doc.select("a[href*='sezon=']").mapNotNull {
            Regex("""sezon=(\d+)""").find(it.attr("href"))?.groupValues?.get(1)?.toIntOrNull()
        }.distinct().sorted()

        val seasonsToFetch = if (seasonNumbers.isEmpty()) listOf(1) else seasonNumbers
        val episodes = mutableListOf<Episode>()

        for (s in seasonsToFetch) {
            val seasonDoc = if (s == 1) {
                doc
            } else {
                try {
                    app.get("$cleanBaseUrl?sezon=$s", headers = defaultHeaders).document
                } catch (_: Exception) {
                    continue
                }
            }

            // Cards with episode links
            val epCards = seasonDoc.select("a[href*='/season/$s/episode/']")
            val distinctCards = if (epCards.isNotEmpty()) {
                epCards
            } else if (s == 1) {
                seasonDoc.select("a[href*='/episode/']")
            } else {
                emptyList()
            }

            for (card in distinctCards) {
                val epHref = fixUrl(card.attr("href"))
                val epImg = card.selectFirst("img")?.attr("src")?.let { fixUrl(it) }

                val sNum = Regex("""/season/(\d+)/""").find(epHref)?.groupValues?.get(1)?.toIntOrNull() ?: s
                val epNum = Regex("""/episode/(\d+)""").find(epHref)?.groupValues?.get(1)?.toIntOrNull() ?: 1

                val epNameText = card.selectFirst("p.truncate")?.text()?.trim()
                val epName = if (!epNameText.isNullOrBlank()) {
                    "$epNum. Bölüm - $epNameText"
                } else {
                    "$sNum. Sezon $epNum. Bölüm"
                }

                episodes.add(newEpisode(epHref) {
                    this.name = epName
                    this.season = sNum
                    this.episode = epNum
                    this.posterUrl = epImg ?: poster
                })
            }
        }

        val type = if (url.contains("/anime/")) TvType.Anime else TvType.TvSeries

        return newTvSeriesLoadResponse(title, url, type, episodes.distinctBy { it.data }) {
            this.posterUrl = poster
            this.backgroundPosterUrl = background
            this.plot = plot
            this.year = year
            this.tags = tags
            this.duration = duration
            this.actors = actors
            this.score = score?.let { Score.from10(it) }
            trailerUrl?.let { this.trailers = mutableListOf(TrailerData(it, mainUrl, false)) }
        }
    }

    /** Finds the Movie / TVSeries / Anime node inside the page's JSON-LD graph. */
    private fun findSchemaNode(doc: Document): JSONObject? {
        for (script in doc.select("script[type=application/ld+json]")) {
            val jsonText = script.html().trim()
            if (jsonText.isBlank()) continue
            val found = try {
                findSchemaIn(JSONObject(jsonText), 0)
            } catch (_: Exception) {
                null
            }
            if (found != null) return found
        }
        return null
    }

    private fun findSchemaIn(node: JSONObject, depth: Int): JSONObject? {
        if (depth > 6) return null
        val type = node.optString("@type")
        if (type == "Movie" || type == "TVSeries" || type == "TVShow" || type == "AnimeSeries") return node

        val graph = node.optJSONArray("@graph")
        if (graph != null) {
            for (i in 0 until graph.length()) {
                graph.optJSONObject(i)?.let { findSchemaIn(it, depth + 1)?.let { hit -> return hit } }
            }
        }
        val keys = node.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            when (val value = node.opt(key)) {
                is JSONObject -> findSchemaIn(value, depth + 1)?.let { return it }
                is org.json.JSONArray -> for (i in 0 until value.length()) {
                    value.optJSONObject(i)?.let { findSchemaIn(it, depth + 1)?.let { hit -> return hit } }
                }
            }
        }
        return null
    }

    /** "PT110M" / "PT1H42M" -> minutes */
    private fun parseIsoDuration(iso: String?): Int? {
        val raw = iso?.trim()?.uppercase()?.takeIf { it.startsWith("PT") } ?: return null
        val h = Regex("""(\d+)H""").find(raw)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val m = Regex("""(\d+)M""").find(raw)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        return (h * 60 + m).takeIf { it > 0 }
    }

    // ── Load Links ──────────────────────────────────────────────────────

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val watchPage = try {
            app.get(data, headers = defaultHeaders).document
        } catch (_: Exception) {
            return false
        }

        var found = false

        // 1. Pilavyer player integration (data-pv attribute)
        val dataPv = watchPage.selectFirst("div[data-pv]")?.attr("data-pv")
        val coreScriptSrc = watchPage.selectFirst("script[src*='pilavyerplay'], script[src*='/assets/js/core.js']")?.attr("src")

        val playerBase = if (!coreScriptSrc.isNullOrBlank()) {
            coreScriptSrc.substringBefore("/assets/js/core.js").substringBefore("/e/c.js")
        } else {
            DEFAULT_PLAYER_BASE
        }

        if (!dataPv.isNullOrBlank()) {
            val iframeUrl = "$playerBase/assets/js/s.php?s=${URLEncoder.encode(dataPv, "UTF-8")}"
            try {
                val iframeHtml = app.get(
                    iframeUrl,
                    headers = mapOf(
                        "User-Agent" to defaultHeaders["User-Agent"]!!,
                        "Referer" to "$mainUrl/"
                    )
                ).text

                val playerMatch = Regex("""window\.__PLAYER__\s*=\s*(\{.*?\});""").find(iframeHtml)
                if (playerMatch != null) {
                    val playerJson = JSONObject(playerMatch.groupValues[1])
                    val streamUrl = playerJson.optString("stream")

                    if (!streamUrl.isNullOrBlank()) {
                        // Extract subtitles
                        val subsArray = playerJson.optJSONArray("subs")
                        if (subsArray != null) {
                            for (i in 0 until subsArray.length()) {
                                val sub = subsArray.optJSONObject(i) ?: continue
                                val subSrc = sub.optString("src")
                                val subLabel = sub.optString("label", "Türkçe")
                                if (!subSrc.isNullOrBlank()) {
                                    subtitleCallback(SubtitleFile(subLabel, subSrc))
                                }
                            }
                        }

                        // Extract available audio tracks
                        val audiosArray = playerJson.optJSONArray("audios")
                        val audioLabels = mutableListOf<String>()
                        if (audiosArray != null) {
                            for (i in 0 until audiosArray.length()) {
                                val audio = audiosArray.optJSONObject(i) ?: continue
                                val label = audio.optString("label")
                                if (!label.isNullOrBlank()) {
                                    audioLabels.add(label)
                                }
                            }
                        }

                        val streamName = if (audioLabels.isNotEmpty()) {
                            "DiziBal (${audioLabels.joinToString(" / ")})"
                        } else {
                            "DiziBal (HLS)"
                        }

                        callback(
                            ExtractorLink(
                                source = "Pilavyer",
                                name = streamName,
                                url = streamUrl,
                                referer = iframeUrl,
                                quality = Qualities.P1080.value,
                                type = ExtractorLinkType.M3U8,
                                headers = mapOf(
                                    "Referer" to iframeUrl,
                                    "User-Agent" to defaultHeaders["User-Agent"]!!
                                )
                            )
                        )
                        found = true
                    }
                }
            } catch (_: Exception) {}
        }

        // 2. Fallback: inspect any embedded iframes or video tags
        if (!found) {
            val iframes = watchPage.select("iframe[src]").map { it.attr("src") }
            for (iframe in iframes) {
                val fullIframe = fixUrl(iframe)
                if (fullIframe.contains("youtube.com") || fullIframe.contains("google")) continue

                if (loadExtractor(fullIframe, data, subtitleCallback) { link ->
                    callback(
                        ExtractorLink(
                            link.source ?: "DiziBal",
                            "DiziBal - ${link.name}",
                            link.url ?: "",
                            link.referer ?: mainUrl,
                            link.quality,
                            link.headers ?: emptyMap(),
                            link.extractorData,
                            link.type,
                            link.audioTracks ?: emptyList()
                        )
                    )
                }) {
                    found = true
                }
            }
        }

        return found
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun cleanTitle(raw: String): String {
        return raw.replace(Regex("(?i)\\s*(?:dizi|film|anime)\\s*izle"), "")
            .replace(Regex("(?i)\\s*izle.*"), "")
            .replace(Regex("(?i)\\s*\\(\\d{4}\\).*"), "")
            .trim()
    }
}