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
import java.net.URLDecoder
import java.net.URLEncoder

class BelesTivi : MainAPI() {
    override var mainUrl = "https://belestivi.local"
    override var name = "BelesTivi"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = true
    override val supportedTypes = setOf(TvType.Live)

    companion object {
        private const val M3U_URL = "https://raw.githubusercontent.com/sunarproje/support/refs/heads/main/belesplus.m3u"
        private const val PLAYLIST_M3U_URL = "https://raw.githubusercontent.com/sunarproje/support/refs/heads/main/playlist.m3u"
        private const val TAG = "BelesTivi"
        private const val CACHE_TTL_MS = 10 * 60 * 1000L // 10 minutes


        // Embedded channels from Beleş TiVi APK (not in M3U playlist)
        private val EMBEDDED_CHANNELS = listOf(
            BelesKanal("atv", "ATV", "ULUSAL", "https://trkvz.daioncdn.net/atv/atv.m3u8?ce=3&app=d1ce2d40-5256-4550-b02e-e73c185a314e", "https://www.google.com/s2/favicons?domain=atv.com.tr&sz=128", "ATV Canlı Yayın"),
            BelesKanal("a2tv", "A2 TV", "7/24 Dizi/Film", "https://trkvz.daioncdn.net/a2tv/a2tv.m3u8?ce=3&app=59363a60-be96-4f73-9eff-355d0ff2c758", "https://www.google.com/s2/favicons?domain=a2tv.com.tr&sz=128", "A2 TV Canlı Yayın"),
            BelesKanal("ahaber", "A Haber", "HABER", "https://trkvz.daioncdn.net/ahaber/ahaber.m3u8?app=web", "https://www.google.com/s2/favicons?domain=ahaber.com.tr&sz=128", "A Haber Canlı Yayın"),
            BelesKanal("apara", "A Para", "HABER", "https://trkvz.daioncdn.net/apara/apara.m3u8?app=web", "https://www.google.com/s2/favicons?domain=apara.com.tr&sz=128", "A Para Canlı Yayın"),
            BelesKanal("aspor", "A Spor", "Spor", "https://trkvz.daioncdn.net/aspor/aspor.m3u8?ce=3&app=45f847c4-04e8-419a-a561-2ebf87084765", "https://www.google.com/s2/favicons?domain=aspor.com.tr&sz=128", "A Spor Canlı Yayın"),
            BelesKanal("minikago", "Minika Go", "ÇOCUK", "https://trkvz.daioncdn.net/minikago/minikago.m3u8?app=web&ce=3", "https://www.google.com/s2/favicons?domain=minika.com.tr&sz=128", "Minika Go Canlı Yayın"),
            BelesKanal("minikago-cocuk", "Minika Çocuk", "ÇOCUK", "https://trkvz.daioncdn.net/minikago_cocuk/minikago_cocuk.m3u8?app=web&ce=3", "https://www.google.com/s2/favicons?domain=minika.com.tr&sz=128", "Minika Çocuk Canlı Yayın"),
            BelesKanal("anews-hd", "A News HD", "HABER", "https://trkvz-live.ercdn.net/anewshd/anewshd.m3u8", "https://www.google.com/s2/favicons?domain=anews.com.tr&sz=128", "A News HD Canlı Yayın"),
            BelesKanal("atv-avrupa", "ATV Avrupa", "Ulusal - Yurt Dışı", "https://trkvz-live.ercdn.net/atvavrupa/atvavrupa.m3u8", "https://www.google.com/s2/favicons?domain=atv.com.tr&sz=128", "ATV Avrupa Canlı Yayın"),
            BelesKanal("vav-tv", "Vav TV", "Dünya", "https://trkvz-live.ercdn.net/vavtv/vavtv.m3u8", "https://www.google.com/s2/favicons?domain=vavtv.com.tr&sz=128", "Vav TV Canlı Yayın")
        )
    }

    override val mainPage = mainPageOf(
        "ULUSAL" to "Ulusal Kanallar",
        "Ulusal - Yurt Dışı" to "Ulusal (Yurt Dışı)",
        "HABER" to "Haber Kanalları",
        "Spor" to "Spor Kanalları",
        "BELGESEL" to "Belgesel Kanalları",
        "Belgesel" to "Belgesel Kanalları",
        "ÇOCUK" to "Çocuk Kanalları",
        "Çocuk" to "Çocuk Kanalları",
        "Müzik" to "Müzik Kanalları",
        "7/24 Dizi/Film" to "7/24 Dizi/Film",
        "Dünya" to "Dünya Kanalları",
        "AZERBAYCAN" to "Azerbaycan Kanalları",
        "YOUTUBE" to "Canlı (YouTube)",
        "ALL" to "Tüm Kanallar"
    )

    // In-memory cache with expiry (10 minutes for YouTube URL freshness)
    @Volatile
    private var cachedChannels: List<BelesKanal>? = null
    @Volatile
    private var cacheTimestamp: Long = 0

    private suspend fun fetchChannels(): List<BelesKanal> {
        val now = System.currentTimeMillis()
        if (cachedChannels != null && (now - cacheTimestamp) < CACHE_TTL_MS) {
            return cachedChannels!!
        }
        // Clear stale cache
        cachedChannels = null

        // Fetch both M3U sources concurrently
        val belesChannels = fetchM3uSource(M3U_URL, "belesplus")
        val playlistChannels = fetchM3uSource(PLAYLIST_M3U_URL, "playlist")

        // Merge all channels
        val allChannels = (belesChannels + playlistChannels + EMBEDDED_CHANNELS).distinctBy { it.id }

        if (allChannels.isNotEmpty()) {
            cachedChannels = allChannels
            cacheTimestamp = System.currentTimeMillis()
        }
        return allChannels
    }

    private suspend fun fetchM3uSource(url: String, sourceName: String): List<BelesKanal> {
        var m3uText = ""
        var attempts = 0
        while (m3uText.isBlank() && attempts < 3) {
            attempts++
            try {
                val response = app.get(
                    url,
                    headers = mapOf(
                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                        "Accept" to "text/plain, */*",
                        "Accept-Language" to "tr-TR,tr;q=0.9"
                    )
                )
                m3uText = response.text
            } catch (e: Exception) {
                Log.e(TAG, "$sourceName fetch attempt $attempts failed: ${e.message}")
            }
        }
        if (m3uText.isBlank()) {
            Log.e(TAG, "$sourceName M3U response is blank after $attempts attempts")
            return emptyList()
        }

        val parsed = parseM3u(m3uText)

        // For playlist.m3u (YouTube), apply category mapping and add YouTube suffix
        return if (sourceName == "playlist") {
            parsed.map { channel ->
                channel.copy(
                    category = "YOUTUBE",
                    id = "yt-${channel.id}",
                    name = channel.name.trim()
                )
            }
        } else {
            parsed
        }
    }

    private fun parseM3u(text: String): List<BelesKanal> {
        val channels = mutableListOf<BelesKanal>()
        val lines = text.lines()
        var i = 0
        var currentExtinf: String? = null

        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF")) {
                currentExtinf = line
            } else if (line.isNotBlank() && !line.startsWith("#") && currentExtinf != null) {
                val url = line
                val channel = parseExtinf(currentExtinf, url, channels.size)
                if (channel != null) {
                    channels.add(channel)
                }
                currentExtinf = null
            }
            i++
        }
        return channels
    }

    private fun parseExtinf(extinf: String, url: String, index: Int): BelesKanal? {
        // Extract name (after last comma)
        val name = extinf.substringAfterLast(",").trim()
        if (name.isBlank()) return null

        // Extract group-title
        val groupMatch = Regex("""group-title="([^"]*)"""").find(extinf)
        val groupTitle = groupMatch?.groupValues?.get(1)?.trim() ?: "Diğer"

        // Extract tvg-logo
        val logoMatch = Regex("""tvg-logo="([^"]*)"""").find(extinf)
        val logo = logoMatch?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }

        // Extract tvg-id
        val idMatch = Regex("""tvg-id="([^"]*)"""").find(extinf)
        val tvgId = idMatch?.groupValues?.get(1)?.trim()

        // Generate stable ID from name
        val id = name.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "kanal-$index" }

        val category = groupTitle

        return BelesKanal(
            id = id,
            name = name,
            category = category,
            streamUrl = url,
            posterUrl = logo,
            description = null
        )
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val category = request.data
        val channels = fetchChannels()

        val filtered = if (category.equals("ALL", ignoreCase = true)) {
            channels
        } else {
            channels.filter { it.category.equals(category, ignoreCase = true) }
        }

        val items = filtered.map { kanal ->
            newLiveSearchResponse(
                name = kanal.name,
                url = "${mainUrl}/kanal/${URLEncoder.encode(kanal.id, "UTF-8")}",
                type = TvType.Live
            ) {
                this.posterUrl = kanal.posterUrl
            }
        }

        return newHomePageResponse(request.name, items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val q = query.trim().lowercase()
        val channels = fetchChannels()

        return channels
            .filter { it.name.lowercase().contains(q) || it.category.lowercase().contains(q) }
            .map { kanal ->
                newLiveSearchResponse(
                    name = kanal.name,
                    url = "${mainUrl}/kanal/${URLEncoder.encode(kanal.id, "UTF-8")}",
                    type = TvType.Live
                ) {
                    this.posterUrl = kanal.posterUrl
                }
            }
    }

    override suspend fun load(url: String): LoadResponse {
        if (url.contains("/kanal/")) {
            val kanalId = URLDecoder.decode(url.substringAfterLast("/kanal/").substringBefore("?"), "UTF-8")
            var kanal = fetchChannels().firstOrNull { it.id == kanalId }
            if (kanal == null) {
                cachedChannels = null
                cacheTimestamp = 0
                kanal = fetchChannels().firstOrNull { it.id == kanalId }
            }
            if (kanal == null) return newLiveStreamLoadResponse("Bilinmeyen", url, url) {}

            return newLiveStreamLoadResponse(kanal.name, url, url) {
                this.posterUrl = kanal.posterUrl
                this.plot = kanal.description
            }
        }

        return newLiveStreamLoadResponse("Bilinmeyen", url, url) {}
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        if (data.contains("/kanal/")) {
            val kanalId = URLDecoder.decode(data.substringAfterLast("/kanal/").substringBefore("?"), "UTF-8")
            var kanal = fetchChannels().firstOrNull { it.id == kanalId }
            if (kanal == null) {
                // Force refresh cache and retry
                cachedChannels = null
                cacheTimestamp = 0
                kanal = fetchChannels().firstOrNull { it.id == kanalId }
            }
            if (kanal == null) return false

            val streamUrl = kanal.streamUrl

            // YouTube streams need specific headers
            val headers = if (streamUrl.contains("googlevideo.com", ignoreCase = true) || streamUrl.contains("manifest.googlevideo", ignoreCase = true)) {
                mapOf(
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                    "Referer" to "https://www.youtube.com/",
                    "Origin" to "https://www.youtube.com"
                )
            } else {
                emptyMap()
            }

            callback(
                ExtractorLink(
                    source = "BelesTivi",
                    name = kanal.name,
                    url = streamUrl,
                    referer = if (streamUrl.contains("googlevideo.com", ignoreCase = true)) "https://www.youtube.com/" else mainUrl,
                    quality = Qualities.Unknown.value,
                    type = if (streamUrl.contains(".m3u8", ignoreCase = true)) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO,
                    headers = headers
                )
            )
            return true
        }

        return false
    }
}
