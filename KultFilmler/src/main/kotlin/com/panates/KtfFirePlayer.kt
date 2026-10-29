package com.panates

import android.util.Log
import org.json.JSONObject
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

/**
 * FirePlayer tabanlı oynatıcı (ör. vidpapi.xyz/video/<32 hex>).
 * POST <oynatıcı>/video/<hash>?do=getVideo  (hash, r, s) -> {videoSource|securedLink|videoSources}
 */
open class KtfFirePlayer : ExtractorApi() {
    override val name            = "FirePlayer"
    override val mainUrl         = "https://vidpapi.xyz"
    override val requiresReferer = true

    companion object {
        private val hashRegex = Regex("""/video/([a-fA-F0-9]{32})""")

        fun uygunMu(url: String): Boolean = hashRegex.containsMatchIn(url)
    }

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val base = Regex("""^(https?://[^/]+)""").find(url)?.groupValues?.get(1) ?: return
        val hash = hashRegex.find(url)?.groupValues?.get(1) ?: return
        val host = base.substringAfter("://")
        val sayfa = url.substringBefore("?").substringBefore("#")

        val basliklar = mapOf(
            "Content-Type"     to "application/x-www-form-urlencoded; charset=UTF-8",
            "X-Requested-With" to "XMLHttpRequest",
            "Origin"           to base
        )

        val yanit = runCatching {
            app.post(
                "$sayfa?do=getVideo",
                data    = mapOf("hash" to hash, "r" to (referer ?: "$base/"), "s" to ""),
                referer = url,
                headers = basliklar
            ).text
        }.getOrNull() ?: return

        val kok = runCatching { JSONObject(yanit) }.getOrNull() ?: return

        kok.optJSONArray("videoSubtitles")?.let { altyazilar ->
            for (i in 0 until altyazilar.length()) {
                val a = altyazilar.optJSONObject(i) ?: continue
                val dosya = a.optString("file").ifBlank { continue }
                subtitleCallback.invoke(newSubtitleFile(a.optString("label").ifBlank { "Altyazı" }, tamAdres(dosya, base)))
            }
        }

        val akislar = mutableListOf<Pair<String, String?>>()
        kok.optString("securedLink").ifBlank { null }?.let { akislar.add(it to null) }
        kok.optString("videoSource").ifBlank { null }?.let { akislar.add(it to null) }
        kok.optJSONArray("videoSources")?.let { liste ->
            for (i in 0 until liste.length()) {
                val k = liste.optJSONObject(i) ?: continue
                val dosya = k.optString("file").ifBlank { continue }.replace("GenYoutube.net", host)
                akislar.add(dosya to k.optString("label").ifBlank { null })
            }
        }

        akislar.distinctBy { it.first }.forEach { (dosya, etiket) ->
            val adres = tamAdres(dosya, base)
            Log.d("KTF_FirePlayer", "$host » $adres")
            callback.invoke(
                newExtractorLink(
                    source = host,
                    name   = if (etiket.isNullOrBlank()) host else "$host - $etiket",
                    url    = adres,
                    type   = if (adres.contains(".m3u8") || adres.contains("master.txt")) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                ) {
                    this.referer = "$base/"
                    this.quality = getQualityFromName(etiket)
                }
            )
        }

        kok.optString("videoSrc").ifBlank { null }?.let { gomulu ->
            loadExtractor(tamAdres(gomulu, base), "$base/", subtitleCallback, callback)
        }
    }

    private fun tamAdres(adres: String, base: String): String = when {
        adres.startsWith("//") -> "https:$adres"
        adres.startsWith("/")  -> "$base$adres"
        adres.startsWith("http") -> adres
        else                   -> "$base/$adres"
    }
}
