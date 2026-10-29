// Self-contained Odnoklassniki / OK.ru extractors for BelgeselX.
// The site serves some documentaries from ok.ru / odnoklassniki.ru iframes;
// bundled here so playback needs no other installed plugin.
// Ported from neowatch (com.neowatch) OdnoklassnikiExtractor.

package com.panates

import com.lagradost.cloudstream3.ErrorLoadingException
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.*
import org.json.JSONArray

private val BEL_OK_UA = mapOf(
    "User-Agent" to "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
)

open class BelOdnoklassniki : ExtractorApi() {
    override val name            = "Odnoklassniki"
    override val mainUrl         = "https://odnoklassniki.ru"
    override val requiresReferer = false

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val text = app.get(url, headers = BEL_OK_UA).text
            .replace("\\&quot;", "\"")
            .replace("\\\\", "\\")
            .replace(Regex("""\\u([0-9A-Fa-f]{4})""")) { m ->
                m.groupValues[1].toInt(16).toChar().toString()
            }

        val videosStr = Regex(""""videos":(\[[^]]*])""").find(text)?.groupValues?.get(1)
            ?: throw ErrorLoadingException("Video not found")

        val arr = JSONArray(videosStr)
        for (i in 0 until arr.length()) {
            val video = arr.optJSONObject(i) ?: continue
            val rawName = video.optString("name")
            val rawUrl  = video.optString("url")
            if (rawUrl.isBlank()) continue

            val videoUrl = if (rawUrl.startsWith("//")) "https:$rawUrl" else rawUrl

            val quality = rawName.uppercase()
                .replace("MOBILE", "144p")
                .replace("LOWEST", "240p")
                .replace("LOW", "360p")
                .replace("SD", "480p")
                .replace("HD", "720p")
                .replace("FULL", "1080p")
                .replace("QUAD", "1440p")
                .replace("ULTRA", "4k")

            callback.invoke(
                newExtractorLink(source = name, name = name, url = videoUrl) {
                    this.referer = url
                    this.quality = getQualityFromName(quality)
                    this.headers = BEL_OK_UA
                }
            )
        }
    }
}

// OK.ru is served from both hosts; the page markup is identical.
class BelOkRuSsl : BelOdnoklassniki() {
    override var name    = "OkRuSSL"
    override var mainUrl = "https://ok.ru"
}

class BelOkRuHttp : BelOdnoklassniki() {
    override var name    = "OkRuHTTP"
    override var mainUrl = "http://ok.ru"
}
