package com.panates

import android.util.Log
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newLiveSearchResponse
import com.lagradost.cloudstream3.newLiveStreamLoadResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.jsoup.Jsoup
import java.net.URLDecoder
import java.net.URLEncoder

class CanliTV : MainAPI() {
    override var mainUrl = "https://canlitv.local"
    override var name = "CanliTV"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = true
    override val supportedTypes = setOf(TvType.Live)

    override val mainPage = mainPageOf(
        "spor" to "Canlı Spor",
        "tabii" to "tabii Spor (UEFA)",
        "ulusal" to "Ulusal Kanallar",
        "haber" to "Haber & Belgesel",
        "cocuk" to "Çocuk Kanalları",
        "dini" to "Dini Kanallar",
        "kultur" to "Kültür & Belgesel",
        "radyo" to "Radyo & Müzik",
        "sporekrani" to "Günün Maçları (Spor Ekranı)",
        "iptv" to "IPTV Türkiye",
        "iptv-spor" to "IPTV Spor",
        "iptv-haber" to "IPTV Haber",
        "iptv-cocuk" to "IPTV Çocuk",
        "iptv-muzik" to "IPTV Müzik"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val category = request.data
        if (category == "sporekrani") {
            val matches = fetchSporekraniMatches()
            return newHomePageResponse(request.name, matches)
        }

        if (category.startsWith("iptv")) {
            val tokens = when (category) {
                "iptv-spor" -> listOf("sports")
                "iptv-haber" -> listOf("news")
                "iptv-cocuk" -> listOf("kids", "animation")
                "iptv-muzik" -> listOf("music")
                else -> emptyList()
            }

            val items = CanliTVIptv.kanallar()
                .filter { CanliTVIptv.inGroups(it, tokens) }
                .map { kanal ->
                    newLiveSearchResponse(
                        name = kanal.name,
                        url = iptvUrl(kanal),
                        type = TvType.Live
                    ) {
                        this.posterUrl = kanal.logo
                    }
                }

            return newHomePageResponse(request.name, items)
        }

        val items = CanliTVChannels.kanallar
            .filter { it.category.equals(category, ignoreCase = true) }
            .map { kanal ->
                newLiveSearchResponse(
                    name = kanal.name,
                    url = "${mainUrl}/kanal/${kanal.id}",
                    type = TvType.Live
                ) {
                    this.posterUrl = kanal.posterUrl
                }
            }

        return newHomePageResponse(request.name, items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val q = query.trim().lowercase()
        val channelMatches = CanliTVChannels.kanallar
            .filter { it.name.lowercase().contains(q) || it.category.lowercase().contains(q) }
            .map { kanal ->
                newLiveSearchResponse(
                    name = kanal.name,
                    url = "${mainUrl}/kanal/${kanal.id}",
                    type = TvType.Live
                ) {
                    this.posterUrl = kanal.posterUrl
                }
            }

        val matchResults = runCatching {
            fetchSporekraniMatches().filter { it.name.lowercase().contains(q) }
        }.getOrDefault(emptyList())

        val iptvMatches = runCatching {
            CanliTVIptv.kanallar()
                .filter { it.name.lowercase().contains(q) }
                .map { kanal ->
                    newLiveSearchResponse(
                        name = kanal.name,
                        url = iptvUrl(kanal),
                        type = TvType.Live
                    ) {
                        this.posterUrl = kanal.logo
                    }
                }
        }.getOrDefault(emptyList())

        return (channelMatches + iptvMatches).distinctBy { it.url } + matchResults
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        if (url.contains("/kanal/")) {
            val kanalId = url.substringAfterLast("/kanal/").substringBefore("?")
            val kanal = CanliTVChannels.findById(kanalId) ?: return null

            return newLiveStreamLoadResponse(
                name = kanal.name,
                url = url,
                dataUrl = url
            ) {
                this.posterUrl = kanal.posterUrl
                this.plot = kanal.description
            }
        }

        if (url.contains("/mac/")) {
            val title = runCatching {
                URLDecoder.decode(url.substringAfterLast("/mac/").substringBefore("?"), "UTF-8")
            }.getOrDefault("Canlı Maç")

            val chName = runCatching {
                Regex("""[?&]ch=([^&]+)""").find(url)?.groupValues?.get(1)?.let {
                    URLDecoder.decode(it, "UTF-8")
                }
            }.getOrNull()

            val poster = runCatching {
                Regex("""[?&]poster=([^&]+)""").find(url)?.groupValues?.get(1)?.let {
                    URLDecoder.decode(it, "UTF-8")
                }
            }.getOrNull()

            return newLiveStreamLoadResponse(
                name = title,
                url = url,
                dataUrl = url
            ) {
                this.posterUrl = poster
                this.plot = if (!chName.isNullOrBlank()) "Yayın Kanalı: $chName" else null
            }
        }

        if (url.contains("/iptv/play")) {
            val params = iptvParams(url)
            val streamUrl = params["u"]?.takeIf { it.isNotBlank() } ?: return null
            val title = params["n"].orEmpty().ifBlank { "IPTV Kanalı" }
            val group = params["g"].orEmpty().ifBlank { "Undefined" }
            val logo = params["l"]?.takeIf { it.isNotBlank() }

            val recommendations = runCatching {
                CanliTVIptv.kanallar()
                    .filter { it.group == group && it.url != streamUrl }
                    .take(12)
                    .map { kanal ->
                        newLiveSearchResponse(kanal.name, iptvUrl(kanal), TvType.Live) {
                            this.posterUrl = kanal.logo
                        }
                    }
            }.getOrDefault(emptyList())

            return newLiveStreamLoadResponse(
                name = title,
                url = url,
                dataUrl = url
            ) {
                this.posterUrl = logo
                this.plot = "${CanliTVIptv.groupTr(group)} · iptv-org canlı yayın listesi"
                this.tags = listOf(CanliTVIptv.groupTr(group), "TR")
                this.recommendations = recommendations
            }
        }

        return null
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        if (data.contains("/kanal/")) {
            val kanalId = data.substringAfterLast("/kanal/").substringBefore("?")
            val kanal = CanliTVChannels.findById(kanalId) ?: return false

            val streamUrl = resolveStream(kanal)

            callback.invoke(
                newExtractorLink(
                    source = this.name,
                    name = kanal.name,
                    url = streamUrl,
                    type = ExtractorLinkType.M3U8
                ) {
                    this.referer = ""
                    this.quality = Qualities.Unknown.value
                }
            )
            return true
        }

        if (data.contains("/iptv/play")) {
            val params = iptvParams(data)
            val streamUrl = params["u"]?.takeIf { it.isNotBlank() } ?: return false
            val kanalName = params["n"].orEmpty().ifBlank { "IPTV Kanalı" }
            val headers = params["h"].orEmpty()
                .split("|")
                .mapNotNull { part ->
                    val key = part.substringBefore("=", "").trim()
                    val value = part.substringAfter("=", "").trim()
                    if (key.isBlank() || value.isBlank()) null else key to value
                }
                .toMap()

            callback.invoke(
                newExtractorLink(
                    source = this.name,
                    name = kanalName,
                    url = streamUrl,
                    type = ExtractorLinkType.M3U8
                ) {
                    this.referer = headers["referrer"] ?: ""
                    this.quality = Qualities.Unknown.value
                    if (headers.isNotEmpty()) this.headers = headers
                }
            )
            return true
        }

        if (data.contains("/mac/")) {
            // Find linked channel or stream
            val chName = runCatching {
                Regex("""[?&]ch=([^&]+)""").find(data)?.groupValues?.get(1)?.let {
                    URLDecoder.decode(it, "UTF-8")
                }
            }.getOrNull()

            val directStream = runCatching {
                Regex("""[?&]stream=([^&]+)""").find(data)?.groupValues?.get(1)?.let {
                    URLDecoder.decode(it, "UTF-8")
                }
            }.getOrNull()

            if (!directStream.isNullOrBlank()) {
                callback.invoke(
                    newExtractorLink(
                        source = this.name,
                        name = chName ?: "Canlı Yayın",
                        url = directStream,
                        type = ExtractorLinkType.M3U8
                    ) {
                        this.quality = Qualities.Unknown.value
                    }
                )
                return true
            }

            if (!chName.isNullOrBlank()) {
                val kanal = CanliTVChannels.findByName(chName)
                if (kanal != null) {
                    val streamUrl = resolveStream(kanal)
                    callback.invoke(
                        newExtractorLink(
                            source = this.name,
                            name = kanal.name,
                            url = streamUrl,
                            type = ExtractorLinkType.M3U8
                        ) {
                            this.quality = Qualities.Unknown.value
                        }
                    )
                    return true
                }
            }
        }

        return false
    }

    private suspend fun resolveStream(kanal: CanliKanal): String {
        if (kanal.dynamicResolver == "nowtv") {
            try {
                val html = app.get(
                    "https://www.nowtv.com.tr/canli-yayin",
                    headers = mapOf("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                ).text
                val m = Regex("""https?://nowtv\.daioncdn\.net/nowtv/nowtv\.m3u8[^"'\s<>]*""").find(html)
                if (m != null) {
                    return m.value
                }
            } catch (e: Exception) {
                Log.e("CanliTV", "Failed resolving NOW TV stream: ${e.message}")
            }
        }
        return kanal.streamUrl
    }

    private fun iptvUrl(kanal: CanliM3uKanal): String {
        val headerChain = kanal.headers.entries.joinToString("|") { "${it.key}=${it.value}" }
        val logo = kanal.logo?.takeIf { it.isNotBlank() }?.let { "&l=${enc(it)}" } ?: ""
        val headers = headerChain.takeIf { it.isNotBlank() }?.let { "&h=${enc(it)}" } ?: ""
        return "$mainUrl/iptv/play?u=${enc(kanal.url)}&n=${enc(kanal.name)}&g=${enc(kanal.group)}$logo$headers"
    }

    private fun enc(value: String): String = URLEncoder.encode(value, "UTF-8")

    private fun iptvParams(data: String): Map<String, String> {
        val query = data.substringAfter("?", "")
        if (query.isBlank()) return emptyMap()
        return query.split("&").mapNotNull { part ->
            if (part.isBlank()) return@mapNotNull null
            val key = part.substringBefore("=", "")
            val raw = part.substringAfter("=", "")
            val value = runCatching { URLDecoder.decode(raw, "UTF-8") }.getOrDefault(raw)
            key to value
        }.toMap()
    }

    private suspend fun fetchSporekraniMatches(): List<SearchResponse> {
        return try {
            val html = app.get(
                "https://www.sporekrani.com/",
                headers = mapOf("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            ).text
            val soup = Jsoup.parse(html)
            val results = mutableListOf<SearchResponse>()

            for (item in soup.select("div.event-list__info")) {
                val matchTitle = item.text().trim()
                if (matchTitle.isBlank()) continue

                val parent = item.parent()
                val timeStr = parent?.selectFirst("div.event-list__time-col")?.text()?.trim().orEmpty()
                val channelImg = parent?.selectFirst("div.event-list__channel img")
                val channelName = channelImg?.attr("alt")?.trim().orEmpty()
                val channelPoster = channelImg?.attr("src")?.takeIf { it.isNotBlank() }

                val displayTitle = if (timeStr.isNotBlank()) "[$timeStr] $matchTitle" else matchTitle

                val kanal = if (channelName.isNotBlank()) CanliTVChannels.findByName(channelName) else null
                val encTitle = URLEncoder.encode(displayTitle, "UTF-8")
                val encCh = if (channelName.isNotBlank()) "&ch=${URLEncoder.encode(channelName, "UTF-8")}" else ""
                val encPoster = if (!channelPoster.isNullOrBlank()) "&poster=${URLEncoder.encode(channelPoster, "UTF-8")}" else ""
                val encStream = if (kanal != null) "&stream=${URLEncoder.encode(kanal.streamUrl, "UTF-8")}" else ""

                val url = "${mainUrl}/mac/${encTitle}?${encCh}${encPoster}${encStream}"

                results.add(
                    newLiveSearchResponse(
                        name = "$displayTitle${if (channelName.isNotBlank()) " ($channelName)" else ""}",
                        url = url,
                        type = TvType.Live
                    ) {
                        this.posterUrl = kanal?.posterUrl ?: channelPoster
                    }
                )
            }
            results
        } catch (e: Exception) {
            Log.e("CanliTV", "Failed fetching Sporekrani: ${e.message}")
            emptyList()
        }
    }
}
