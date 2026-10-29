// Extra DiziPal player decoders, ported from the neowatch DiziPal family.
// Bundled here so DiziPal playback has no hidden cross-plugin dependency.
//
//   PalPlayer   – jwplayer("…").setup({sources:[…]}) hosts (formationfeed.net et al.)
//   StreamCore  – `const src = '/play.m3u8?id=…'` HLS + tracksData.subtitles
//   VideoPlays  – /player.html?video=<id> token flow used by Dizipal Orijinal

package com.panates

import android.util.Log
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.newSubtitleFile
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.getAndUnpack
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.json.JSONObject
import java.net.URLEncoder

private val dpMapper by lazy { ObjectMapper() }

private fun dpBaseOf(url: String, fallback: String): String =
    Regex("""^(https?://[^/]+)""").find(url)?.groupValues?.get(1) ?: fallback

/** jwplayer("…").setup({ sources: [{file:"…"}], tracks: […] }) pages, P.A.C.K.E.R aware. */
open class DpJwKaynak : ExtractorApi() {
    override val name = "PalPlayer"
    override val mainUrl = "https://formationfeed.net"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val base = dpBaseOf(url, mainUrl)
        val page = app.get(
            url,
            referer = referer ?: "$base/",
            headers = mapOf("Sec-Fetch-Dest" to "iframe")
        ).text

        val unpacked = if (page.contains("eval(function(p,a,c,k,e")) {
            runCatching { getAndUnpack(page) }.getOrNull()?.let { "$page\n$it" } ?: page
        } else {
            page
        }

        val sources = Regex("""sources\s*:\s*\[(.*?)]""", RegexOption.DOT_MATCHES_ALL)
            .find(unpacked)?.groupValues?.get(1)

        if (sources == null) {
            Log.d("DiziPal", "PalPlayer kaynak bulunamadi » $url")
            return
        }

        Regex("""file\s*:\s*["']([^"']+)["']""").findAll(sources).forEach { match ->
            val file = match.groupValues[1].replace("\\/", "/")
            callback(
                newExtractorLink(
                    source = this.name,
                    name = this.name,
                    url = file,
                    type = if (file.contains(".m3u8")) ExtractorLinkType.M3U8 else null
                ) {
                    this.referer = "$base/"
                    this.quality = Qualities.Unknown.value
                    this.headers = mapOf("Origin" to base)
                }
            )
        }

        Regex("""tracks\s*:\s*\[(.*?)]""", RegexOption.DOT_MATCHES_ALL).find(unpacked)
            ?.groupValues?.get(1)?.let { tracks ->
                Regex("""\{[^}]*?file\s*:\s*["']([^"']+)["'][^}]*}""").findAll(tracks).forEach { track ->
                    val block = track.value
                    if (block.contains("thumbnails")) return@forEach
                    val file = track.groupValues[1].replace("\\/", "/")
                    val label = Regex("""label\s*:\s*["']([^"']+)["']""")
                        .find(block)?.groupValues?.get(1) ?: "Türkçe"
                    subtitleCallback(
                        newSubtitleFile(if (label.equals("Turkish", true)) "Türkçe" else label, file)
                    )
                }
            }
    }
}

/** streamcorecdn.com: `const src = '/play.m3u8?id=…&m=master&token=…'` + `tracksData = {subtitles:[…]}`. */
open class DpStreamCore : ExtractorApi() {
    override val name = "StreamCore"
    override val mainUrl = "https://streamcorecdn.com"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val base = dpBaseOf(url, mainUrl)
        val page = app.get(url, referer = referer ?: "$base/").text

        val src = Regex("""const\s+src\s*=\s*['"]([^'"]+)['"]""").find(page)?.groupValues?.get(1)
        if (src == null) {
            Log.d("DiziPal", "StreamCore kaynak bulunamadi » $url")
            return
        }

        val m3u8 = when {
            src.startsWith("http") -> src
            src.startsWith("/") -> base + src
            else -> "$base/$src"
        }
        Log.d("DiziPal", "StreamCore m3u8 » $m3u8")

        callback(
            newExtractorLink(
                source = this.name,
                name = this.name,
                url = m3u8,
                type = ExtractorLinkType.M3U8
            ) {
                this.referer = url
                this.quality = Qualities.Unknown.value
                this.headers = mapOf("Origin" to base)
            }
        )

        val token = Regex("""tokenParams\s*=\s*["']([^"']*)["']""").find(page)?.groupValues?.get(1).orEmpty()
        val id = Regex("""[?&]id=(\d+)""").find(src)?.groupValues?.get(1)
        val tracks = Regex("""tracksData\s*=\s*(\{.*?});""").find(page)?.groupValues?.get(1) ?: return

        runCatching {
            val subs = JSONObject(tracks).optJSONArray("subtitles") ?: return@runCatching
            for (i in 0 until subs.length()) {
                val sub = subs.optJSONObject(i) ?: continue
                val path = sub.optString("url").takeIf { it.isNotBlank() } ?: continue
                val address = when {
                    path.startsWith("http") -> path
                    id != null -> "$base/play.m3u8?id=$id&p=${URLEncoder.encode(path, "UTF-8")}$token"
                    else -> continue
                }
                val label = sub.optString("name").ifBlank { "Türkçe" }
                subtitleCallback(newSubtitleFile(label, address))
            }
        }
    }
}

/** videoplays.cfd: /api/videos/{id}/token?embed_referrer=<site> → playlist_url + /api/videos/{id} → subtitles. */
open class DpVideoPlays : ExtractorApi() {
    override val name = "VideoPlays"
    override val mainUrl = "https://videoplays.cfd"
    override val requiresReferer = true

    data class DpVideoInfo(
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("subtitles") val subtitles: List<DpVideoSub>? = null
    )

    data class DpVideoSub(
        @JsonProperty("lang") val lang: String? = null,
        @JsonProperty("url") val url: String? = null
    )

    data class DpVideoToken(
        @JsonProperty("token") val token: String? = null,
        @JsonProperty("playlist_url") val playlistUrl: String? = null
    )

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val base = Regex("""^(https?://[^/]+)""").find(url)?.groupValues?.get(1) ?: return
        val videoId = Regex("""video=(\d+)""").find(url)?.groupValues?.get(1) ?: return
        val extRef = referer ?: "$base/"

        runCatching {
            val info = dpMapper.readValue(
                app.get("$base/api/videos/$videoId", referer = url).text,
                DpVideoInfo::class.java
            )
            info.subtitles?.forEach { sub ->
                val subUrl = sub.url ?: return@forEach
                subtitleCallback(newSubtitleFile(sub.lang ?: "Türkçe", subUrl))
            }
        }

        val token = runCatching {
            dpMapper.readValue(
                app.get(
                    "$base/api/videos/$videoId/token",
                    params = mapOf("embed_referrer" to extRef),
                    referer = url
                ).text,
                DpVideoToken::class.java
            )
        }.getOrNull() ?: return

        val playlist = token.playlistUrl?.takeIf { it.isNotBlank() }
            ?: token.token?.let { "$base/api/videos/$videoId/master.m3u8?token=$it" }
            ?: return
        Log.d("DiziPal", "VideoPlays playlist » $playlist")

        callback(
            newExtractorLink(
                source = this.name,
                name = this.name,
                url = playlist,
                type = ExtractorLinkType.M3U8
            ) {
                this.referer = "$base/"
                this.quality = Qualities.Unknown.value
            }
        )
    }
}