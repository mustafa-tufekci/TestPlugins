package com.panates

import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities

class VidMolyExtractor : ExtractorApi() {
    override val name = "VidMoly"
    override val mainUrl = "https://vidmoly.net"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val langTag = url.substringAfterLast("#", "")
        val cleanUrl = url.substringBefore("#")

        val dilLabel = when (langTag) {
            "dublaj" -> " - Türkçe Dublaj"
            "altyazi" -> " - Türkçe Altyazılı"
            else -> ""
        }
        val displayName = "$name$dilLabel"

        val response = app.get(cleanUrl, referer = referer).text

        val hlsRegexes = listOf(
            Regex("""sources\s*:\s*\[\s*\{\s*file\s*:\s*['"]([^'"]+)['"]"""),
            Regex("""file\s*:\s*['"](https?://[^'"]+\.m3u8[^'"]*)['"]"""),
            Regex("""file\s*:\s*['"](https?://[^'"]+urlset/master[^'"]*)['"]""")
        )

        for (regex in hlsRegexes) {
            val match = regex.find(response)
            if (match != null) {
                callback.invoke(
                    ExtractorLink(
                        source = name,
                        name = displayName,
                        url = match.groupValues[1],
                        referer = referer ?: cleanUrl,
                        quality = Qualities.Unknown.value,
                        type = ExtractorLinkType.M3U8
                    )
                )
                return
            }
        }
    }
}
