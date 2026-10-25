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
        private const val TAG = "BelesTivi"

        // Known group titles from M3U (stable across updates)
        private val GROUP_NAMES = mapOf(
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
            "AZERBAYCAN" to "Azerbaycan Kanalları"
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
        "ALL" to "Tüm Kanallar"
    )

    // In-memory cache
    @Volatile
    private var cachedChannels: List<BelesKanal>? = null

    private suspend fun fetchChannels(): List<BelesKanal> {
        cachedChannels?.let { return it }

        val channels = try {
            var m3uText = ""
            var attempts = 0
            while (m3uText.isBlank() && attempts < 3) {
                attempts++
                try {
                    val response = app.get(
                        M3U_URL,
                        headers = mapOf(
                            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                            "Accept" to "text/plain, */*",
                            "Accept-Language" to "tr-TR,tr;q=0.9"
                        )
                    )
                    m3uText = response.text
                } catch (e: Exception) {
                    Log.e(TAG, "Fetch attempt $attempts failed: ${e.message}")
                }
            }
            if (m3uText.isBlank()) {
                Log.e(TAG, "M3U response is blank after $attempts attempts")
                emptyList()
            } else {
                parseM3u(m3uText)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch M3U: ${e.message}")
            emptyList()
        }

        if (channels.isNotEmpty()) {
            cachedChannels = channels
        }
        return channels
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
            val kanal = fetchChannels().firstOrNull { it.id == kanalId }
                ?: return newLiveStreamLoadResponse("Bilinmeyen", url, url) {}

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
            val kanal = fetchChannels().firstOrNull { it.id == kanalId } ?: return false

            val streamUrl = kanal.streamUrl

            callback(
                ExtractorLink(
                    source = "BelesTivi",
                    name = kanal.name,
                    url = streamUrl,
                    referer = mainUrl,
                    quality = Qualities.Unknown.value,
                    type = if (streamUrl.contains(".m3u8", ignoreCase = true)) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO,
                    headers = emptyMap()
                )
            )
            return true
        }

        return false
    }
}
