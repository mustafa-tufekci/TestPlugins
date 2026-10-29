// Self-contained TurboImgz extractor for FullHDFilm (turbo.imgz.me iframes).
// Ported from neowatch (com.neowatch) TurboImgzExtractor.

package com.panates

import com.lagradost.cloudstream3.ErrorLoadingException
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.*

class FhfTurboImgz : ExtractorApi() {
    override val name            = "TurboImgz"
    override val mainUrl         = "https://turbo.imgz.me"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val extRef   = referer ?: ""
        // The embed url may carry a "||<QUALITY>" suffix appended by the site.
        val videoReq = app.get(url.substringAfter("||"), referer = extRef).text

        val videoLink = Regex("""file: "(.*)",""").find(videoReq)?.groupValues?.get(1)
            ?: throw ErrorLoadingException("File not found")

        val etiket = if (url.contains("||")) " - " + url.substringBefore("||").uppercase() else ""

        callback.invoke(
            newExtractorLink(
                source = "$name$etiket",
                name   = "$name$etiket",
                url    = videoLink,
                type   = ExtractorLinkType.M3U8
            ) {
                this.referer = extRef
                this.quality = Qualities.Unknown.value
            }
        )
    }
}
