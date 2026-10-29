package com.panates

import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.newExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import java.net.URLEncoder

/**
 * StreamCore (streamcorecdn.com) oynatıcısı.
 *
 * Oynatıcı sayfası stream bilgisini JS sabitleri olarak gömüyor:
 *   const src = '/play.m3u8?id=3608&m=master&token=...&expires=...';
 *   const videoId = 3608;
 *   const tokenParams = "&token=...&expires=...";
 *   const tracksData = {"audio":[...],"subtitles":[{"name":"Türkçe","lang":"tr","url":"tracks/sub_15/subtitles.vtt"}]};
 *
 * Hem master hem de altyazı istekleri Referer istiyor (Referer yoksa 403).
 */
open class TkaStreamCore : ExtractorApi() {
    override var name = "StreamCore"
    override var mainUrl = "https://streamcorecdn.com"
    override val requiresReferer = true

    companion object {
        private val masterRegex = Regex("""const\s+src\s*=\s*['"]([^'"]+)['"]""")
        private val videoIdRegex = Regex("""const\s+videoId\s*=\s*(\d+)""")
        private val tokenRegex = Regex("""const\s+tokenParams\s*=\s*["']([^"']*)["']""")
        private val tracksRegex = Regex("""const\s+tracksData\s*=\s*(\{.*?\});""", RegexOption.DOT_MATCHES_ALL)
        private val kullaniciAjan =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

        fun uygunMu(url: String): Boolean = url.contains("streamcorecdn.", ignoreCase = true)

        private fun origin(url: String): String {
            val parca = url.substringAfter("://", "")
            val host = parca.substringBefore("/").substringBefore("?")
            return if (host.isBlank()) "https://streamcorecdn.com" else "https://$host"
        }

        private fun tamAdres(adres: String, base: String): String = when {
            adres.isBlank() -> adres
            adres.startsWith("http") -> adres
            adres.startsWith("//") -> "https:$adres"
            adres.startsWith("/") -> "$base$adres"
            else -> "$base/$adres"
        }
    }

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val base = origin(url)
        val siteReferer = referer?.takeIf { it.startsWith("http") } ?: "$base/"
        val basliklar = mapOf(
            "Referer" to siteReferer,
            "User-Agent" to kullaniciAjan
        )

        val html = app.get(
            url,
            referer = siteReferer,
            headers = mapOf("User-Agent" to kullaniciAjan)
        ).text

        val master = masterRegex.find(html)?.groupValues?.get(1)
            ?: Regex("""['"](/play\.m3u8\?[^'"]+)['"]""").find(html)?.groupValues?.get(1)

        if (master.isNullOrBlank()) {
            // Son çare: sayfadaki doğrudan m3u8 bağlantıları
            Regex("""https?://[^'"\s]+\.m3u8[^'"\s]*""").findAll(html).map { it.value }.distinct()
                .forEach { link ->
                    callback(
                        newExtractorLink(
                            source = name,
                            name = name,
                            url = link,
                            type = ExtractorLinkType.M3U8
                        ) {
                            this.referer = siteReferer
                            this.quality = Qualities.Unknown.value
                            this.headers = basliklar
                        }
                    )
                }
            return
        }

        val masterUrl = tamAdres(master, base)
        callback(
            newExtractorLink(
                source = name,
                name = name,
                url = masterUrl,
                type = ExtractorLinkType.M3U8
            ) {
                this.referer = siteReferer
                this.quality = Qualities.Unknown.value
                this.headers = basliklar
            }
        )

        // Altyazılar: url göreli ise /play.m3u8?id=<videoId>&p=<url>&token=... şeklinde istenir
        val videoId = videoIdRegex.find(html)?.groupValues?.get(1)
        val tokenParams = tokenRegex.find(html)?.groupValues?.get(1) ?: ""
        val tracksJson = tracksRegex.find(html)?.groupValues?.get(1) ?: return

        runCatching {
            val subtitles = org.json.JSONObject(tracksJson).optJSONArray("subtitles") ?: return@runCatching
            for (i in 0 until subtitles.length()) {
                val alt = subtitles.optJSONObject(i) ?: continue
                val ham = alt.optString("url").ifBlank { alt.optString("file") }
                if (ham.isBlank()) continue
                val tam = if (ham.startsWith("http")) {
                    ham
                } else {
                    val kodlu = URLEncoder.encode(ham, "UTF-8")
                    "$base/play.m3u8?id=${videoId ?: ""}&p=$kodlu$tokenParams"
                }
                val etiket = alt.optString("name")
                    .ifBlank { alt.optString("label") }
                    .ifBlank { alt.optString("lang") }
                    .ifBlank { "Altyazı" }
                subtitleCallback(
                    SubtitleFile(etiket, tam).apply { headers = basliklar }
                )
            }
        }
    }
}
