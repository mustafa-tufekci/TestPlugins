package com.panates

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import java.util.regex.Pattern

class DiskYandexComTr : ExtractorApi() {
    override val name: String = "DiskYandexComTr"
    override val mainUrl: String = "https://disk.yandex.com.tr"
    override val requiresReferer: Boolean = false

    private val masterPlaylistRegex = Pattern.compile("https?://[^\\s\"]*?master-playlist\\.m3u8")

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val request = app.get(
            url = url,
            referer = "https://disk.yandex.com.tr/",
            headers = mapOf("X-Requested-With" to "XMLHttpRequest")
        )

        if (!request.isSuccessful) {
            throw Exception("Failed to fetch URL: ${request.code}")
        }

        val matcher = masterPlaylistRegex.matcher(request.text)
        if (matcher.find()) {
            callback.invoke(
                ExtractorLink(
                    source = "Yandex Disk",
                    name = "Yandex Disk",
                    url = matcher.group(),
                    referer = referer ?: "",
                    quality = Qualities.Unknown.value,
                    type = ExtractorLinkType.M3U8
                )
            )
        } else {
            throw Exception("No master-playlist.m3u8 URL found in the response")
        }
    }
}
