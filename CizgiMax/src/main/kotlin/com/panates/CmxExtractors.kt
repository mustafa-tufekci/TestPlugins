// Self-contained extractors for the external hosts CizgiMax embeds
// (sibnet / google drive / cizgiduo / cizgipass). Registered by CizgiMaxPlugin
// so the module's playback needs no other installed plugin.
// Ported from neowatch (com.neowatch).

package com.panates

import com.lagradost.cloudstream3.ErrorLoadingException
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.extractors.helper.AesHelper
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.StringUtils.decodeUri

open class CmxSibNet : ExtractorApi() {
    override val name            = "SibNet"
    override val mainUrl         = "https://video.sibnet.ru"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val extRef  = referer ?: ""
        val iSource = app.get(url, referer = extRef).text
        var m3uLink = Regex("""player\.src\(\[\{src: "([^"]+)"""").find(iSource)?.groupValues?.get(1)
            ?: throw ErrorLoadingException("m3u link not found")

        m3uLink = "${mainUrl}${m3uLink}"

        callback.invoke(
            newExtractorLink(source = name, name = name, url = m3uLink) {
                this.referer = url
                this.quality = Qualities.Unknown.value
            }
        )
    }
}

open class CmxDrive : ExtractorApi() {
    override val name            = "Drive"
    override val mainUrl         = "https://drive.google.com"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val extRef  = referer ?: ""

        val docId = Regex("""file/d/(.*)/preview""").find(url)?.groupValues?.get(1)
            ?: throw ErrorLoadingException("docId not found")
        val getVideoLink = "https://drive.google.com/get_video_info?docid=${docId}&drive_originator_app=303"
        val iSource      = app.get(getVideoLink, referer = extRef).text

        val raw = Regex("""&fmt_stream_map=(.*)&url_encoded_fmt_stream_map""").find(iSource)?.groupValues?.get(1)
            ?: throw ErrorLoadingException("fmt_stream_map not found")
        val m3uLink = raw.decodeUri().split("|").last()

        callback.invoke(
            newExtractorLink(source = name, name = name, url = m3uLink) {
                this.referer = url
                this.quality = Qualities.Unknown.value
            }
        )
    }
}

open class CmxCizgiDuo : ExtractorApi() {
    override val name            = "CizgiDuo"
    override val mainUrl         = "https://cizgiduo.online"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val extRef  = referer ?: ""
        val iSource = app.get(url, referer = extRef).text

        val bePlayer = Regex("""bePlayer\('([^']+)',\s*'(\{[^}]+\})'\);""").find(iSource)?.groupValues
            ?: throw ErrorLoadingException("bePlayer not found")
        val bePlayerPass = bePlayer[1]
        val bePlayerData = bePlayer[2]

        val encrypted = AesHelper.cryptoAESHandler(bePlayerData, bePlayerPass.toByteArray(), false)
            ?.replace("\\", "")
            ?: throw ErrorLoadingException("failed to decrypt")

        val m3uLink = Regex("""video_location":"([^"]+)""").find(encrypted)?.groupValues?.get(1)
            ?: throw ErrorLoadingException("m3u link not found")

        callback.invoke(
            newExtractorLink(source = name, name = name, url = m3uLink, type = ExtractorLinkType.M3U8) {
                this.referer = url
                this.quality = Qualities.Unknown.value
            }
        )
    }
}

class CmxCizgiPass : CmxCizgiDuo() {
    override var name    = "CizgiPass"
    override var mainUrl = "https://cizgipass5.online"
}
