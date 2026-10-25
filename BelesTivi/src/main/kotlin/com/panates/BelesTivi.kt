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

class BelesTivi : MainAPI() {
    override var mainUrl = "https://belestivi.local"
    override var name = "BelesTivi"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = true
    override val supportedTypes = setOf(TvType.Live)

    override val mainPage = mainPageOf(
        "ulusal" to "Ulusal Kanallar",
        "haber" to "Haber Kanalları",
        "spor" to "Spor Kanalları",
        "belgesel" to "Belgesel Kanalları",
        "cocuk" to "Çocuk Kanalları",
        "muzik" to "Müzik Kanalları",
        "dunya" to "Dünya Kanalları",
        "dizi" to "7/24 Dizi/Film",
        "dini" to "Dini Kanallar"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val category = request.data

        val items = BelesTiviChannels.kanallar
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
        val channelMatches = BelesTiviChannels.kanallar
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

        return channelMatches
    }

    override suspend fun load(url: String): LoadResponse {
        if (url.contains("/kanal/")) {
            val kanalId = url.substringAfterLast("/kanal/").substringBefore("?")
            val kanal = BelesTiviChannels.findById(kanalId) ?: return newLiveStreamLoadResponse("Bilinmeyen", url, url) {}

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
            val kanalId = data.substringAfterLast("/kanal/").substringBefore("?")
            val kanal = BelesTiviChannels.findById(kanalId) ?: return false

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