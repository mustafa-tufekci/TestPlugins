// Self-contained ContentX-family extractor for SelcukFlix (hotlinger iframes).
// Bundled in-module so core playback has no hidden cross-plugin dependency.

package com.panates

import android.util.Base64
import android.util.Log
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.lagradost.cloudstream3.ErrorLoadingException
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.*
import java.net.URLEncoder

open class DpContentX : ExtractorApi() {
    override val name            = "DpContentX"
    override val mainUrl         = "https://contentx.me"
    override val requiresReferer = true

    private val jsonMapper by lazy { ObjectMapper() }

    private val userAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val extRef   = referer ?: ""
        Log.d("SelcukFlix", "url » $url")

        // DiziPal hands us a ready-made source2.php?v=<token> URL (the player host varies:
        // sn.dplayer*.site, four.dplayer82.site, ...), so the base host comes from the URL.
        val isSource2 = url.contains("source2.php")
        val base = baseUrl(url)
        val iSource = if (isSource2) "" else app.get(url, referer = extRef).text

        // Yeni nesil oynatıcı: her şey tek bir window.NP_CONFIG JSON'unda geliyor.
        val playerOrigin = originOf(url)
        val npConfig     = if (isSource2) null else parseNpConfig(iSource)
        if (npConfig != null) {
            val npHeaders = mapOf(
                "Referer"    to playerOrigin,
                "User-Agent" to userAgent
            )

            // Kasım 2026: NP_CONFIG içinde "gate":{"n":"…","b":12} varsa master ve altyazı
            // adreslerine iş kanıtı eklenmezse CDN 404 verir. (neowatch NpKapi portu)
            val gateNode = npConfig.path("gate")
            var pow: String? = null
            if (!gateNode.isMissingNode) {
                val n = gateNode.path("n").asText(null)
                if (n != null) pow = DpNpGate.solve(iSource, n, gateNode.path("b").asInt(12))
            }
            fun gated(adres: String): String =
                if (pow == null) adres else adres + (if (adres.contains("?")) "&" else "?") + "p=$pow"

            npConfig.path("subtitles").forEach { node ->
                val raw   = node.path("url").asText("").ifBlank { node.path("file").asText("") }
                val fixed = fixSubUrl(raw, playerOrigin) ?: return@forEach
                val label = node.path("label").asText("").trim()
                    .ifBlank { node.path("lang").asText("").trim() }
                    .ifBlank { "Altyazı" }
                subtitleCallback.invoke(SubtitleFile(label, gated(fixed)).apply { headers = npHeaders })
            }

            val seen = mutableSetOf<String>()
            npConfig.path("sources").forEach { node ->
                val raw = node.path("master").asText("").ifBlank { node.path("file").asText("") }
                val fixed = fixSubUrl(raw, playerOrigin) ?: return@forEach
                if (!seen.add(fixed)) return@forEach
                val label = node.path("title").asText("").trim()

                callback.invoke(
                    newExtractorLink(
                        source = this.name,
                        name   = label.ifBlank { this.name },
                        url    = gated(fixed),
                        type   = if (fixed.contains(".mp4")) ExtractorLinkType.VIDEO else ExtractorLinkType.M3U8
                    ) {
                        this.referer = playerOrigin
                        this.headers = npHeaders
                        this.quality = Qualities.Unknown.value
                    }
                )
            }

            if (seen.isNotEmpty()) return

            Log.d("SelcukFlix", "NP_CONFIG içinde kaynak yok, eski akışa düşülüyor")
        }

        val iExtract = if (isSource2) {
            Regex("[?&]v=([^&]+)").find(url)?.groupValues?.get(1).orEmpty()
        } else {
            Regex("""window\.openPlayer\('([^']+)'""").find(iSource)?.groups?.get(1)?.value
                // Eski tip oynatıcı: openPlayer yok, dosya adresi doğrudan gömülü gelir.
                ?: return directFile(iSource, base, callback)
        }

        val subPairs = mutableListOf<Pair<String, String>>()
        fun addSub(lang: String?, raw: String?) {
            val fixed = fixSubUrl(raw ?: return, url) ?: return
            if (subPairs.none { it.second == fixed }) {
                subPairs.add((lang?.trim().orEmpty().ifBlank { "Altyazı" }) to fixed)
            }
        }

        // 1) Eski format: iframe HTML'ine gömülü altyazılar (fallback)
        Regex(""""file":"((?:\\\"|[^"])+)","label":"((?:\\\"|[^"])+)"""").findAll(iSource).forEach {
            val (subUrlRaw, subLangRaw) = it.destructured

            addSub(unescapeTr(subLangRaw), subUrlRaw)
        }

        val vidSource  = app.get("$base/source2.php?v=${iExtract}", referer=extRef).text

        // 2) source2.php JSON'u içindeki altyazı parçaları
        runCatching { collectJsonSubs(jsonMapper.readTree(vidSource)) { lang, raw -> addSub(lang, raw) } }

        // 3) Yeni player akışı: altyazılar track.php üzerinden ayrıca yükleniyor
        runCatching { fetchTrackSubs(iExtract, url, extRef.ifBlank { url }) { lang, raw -> addSub(lang, raw) } }

        Log.d("SelcukFlix", "subtitle » ${subPairs.map { it.second }}")

        // Altyazı CDN'i başlıksız isteğe 403 veriyor; video ile aynı başlıkları ekle
        val subHeaders = mapOf(
            "Referer" to url,
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36 Norton/124.0.0.0"
        )

        subPairs.forEach { (subLang, subUrl) ->
            subtitleCallback.invoke(SubtitleFile(subLang, subUrl).apply { headers = subHeaders })
        }

        val vidExtract = Regex("""file":"([^"]+)""").find(vidSource)?.groups?.get(1)?.value ?: throw ErrorLoadingException("vidExtract is null")
        val m3uLink    = vidExtract.replace("\\", "")

        callback.invoke(
            newExtractorLink(
                source  = this.name,
                name    = this.name,
                url     = m3uLink,
                type = ExtractorLinkType.M3U8

            ) {
                headers = mapOf("Referer" to url,
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36 Norton/124.0.0.0")
                quality = Qualities.Unknown.value
            }
        )

        val iDublaj = Regex(""","([^']+)","Türkçe""").find(iSource)?.groups?.get(1)?.value
        if (iDublaj != null) {
            val dublajSource  = app.get("${mainUrl}/source2.php?v=${iDublaj}", referer=extRef).text
            val dublajExtract = Regex("""file":"([^"]+)""").find(dublajSource)!!.groups[1]?.value ?: throw ErrorLoadingException("dublajExtract is null")
            val dublajLink    = dublajExtract.replace("\\", "")

            callback.invoke(
                newExtractorLink(
                    source = this.name,
                    name = this.name,
                    url = dublajLink,
                    type = ExtractorLinkType.M3U8
                ) {
                    headers = mapOf("Referer" to url,
                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36 Norton/124.0.0.0")
                    quality = Qualities.Unknown.value
                }
            )
        }
    }

    private fun baseUrl(url: String): String =
        if (url.contains("://")) url.substringAfter("://").substringBefore("/").let { "https://$it" } else mainUrl

    /** Eski tip oynatıcı: NP_CONFIG/openPlayer yok, dosya adresi sayfada gömülü. */
    private suspend fun directFile(iSource: String, base: String, callback: (ExtractorLink) -> Unit) {
        val dosya = Regex("""file\s*:\s*"([^"]+)"""").find(iSource)?.groupValues?.get(1) ?: return
        val file = dosya.replace("\\", "")
        callback.invoke(
            newExtractorLink(
                source = this.name,
                name   = this.name,
                url    = file,
                type   = if (file.contains(".m3u8")) ExtractorLinkType.M3U8 else null
            ) {
                this.referer = "$base/"
                this.quality = Qualities.Unknown.value
            }
        )
    }

    /** Oynatıcı URL'sinin şema + host kökü (yeni akış referer doğrulaması bunu bekliyor). */
    private fun originOf(url: String): String {
        val m = Regex("""^(https?://[^/]+)""").find(url) ?: return mainUrl
        return "${m.groupValues[1]}/"
    }

    /** HTML içindeki window.NP_CONFIG JSON'unu süslü parantez eşleştirerek çıkarır. */
    private fun parseNpConfig(html: String): JsonNode? {
        val marker = html.indexOf("NP_CONFIG")
        if (marker < 0) return null
        val start = html.indexOf('{', marker)
        if (start < 0) return null

        var depth = 0
        var inStr = false
        var escape = false
        var end = -1
        for (i in start until html.length) {
            val c = html[i]
            if (inStr) {
                when {
                    escape    -> escape = false
                    c == '\\' -> escape = true
                    c == '"'  -> inStr = false
                }
            } else {
                when (c) {
                    '"' -> inStr = true
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) { end = i; break }
                    }
                }
            }
        }
        if (end < 0) return null

        return runCatching { jsonMapper.readTree(html.substring(start, end + 1)) }.getOrNull()
    }

    private fun unescapeTr(raw: String): String = raw
        .replace("\\u0131", "ı")
        .replace("\\u0130", "İ")
        .replace("\\u00fc", "ü")
        .replace("\\u00e7", "ç")
        .replace("\\u011f", "ğ")
        .replace("\\u015f", "ş")

    private fun fixSubUrl(raw: String, pageUrl: String): String? {
        val u = raw.replace("\\/", "/").replace("\\u0026", "&").replace("\\", "").trim()
        if (u.isBlank()) return null
        if (u.startsWith("https://")) return u
        if (u.startsWith("http://")) return "https://" + u.removePrefix("http://")
        if (u.startsWith("//")) return "https:$u"
        if (u.startsWith("/")) {
            val origin = Regex("""^(https?://[^/]+)""").find(pageUrl)?.groupValues?.get(1) ?: return null
            return origin + u
        }
        return null
    }

    private fun collectJsonSubs(node: JsonNode, add: (String?, String?) -> Unit) {
        if (node.isObject) {
            val sub = listOf("file", "src", "url")
                .firstNotNullOfOrNull { node.path(it).asText(null) }
                ?.takeIf { it.contains(".vtt") || it.contains(".srt") || it.contains(".ass") }
                ?: node.fields().asSequence()
                    .mapNotNull { (_, v) -> if (v.isTextual) v.asText() else null }
                    .firstOrNull { it.contains(".vtt") || it.contains(".srt") || it.contains(".ass") }
            if (sub != null) {
                val lang = listOf("label", "language", "lang", "title", "name")
                    .firstNotNullOfOrNull { node.path(it).asText(null)?.trim()?.takeIf { s -> s.isNotEmpty() } }
                add(lang, sub)
            }
            node.forEach { if (!it.isTextual) collectJsonSubs(it, add) }
        } else if (node.isArray) {
            node.forEach { collectJsonSubs(it, add) }
        }
    }

    private suspend fun fetchTrackSubs(hash: String, pageUrl: String, pageReferer: String, add: (String?, String?) -> Unit) {
        val h = Base64.encodeToString(pageReferer.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val body = app.get(
            "${mainUrl}/track.php?v=${hash}&h=${URLEncoder.encode(h, "UTF-8")}",
            referer = pageUrl
        ).text
        if (body.isBlank()) return
        var found = false
        try {
            collectJsonSubs(jsonMapper.readTree(body)) { lang, raw -> found = true; add(lang, raw) }
        } catch (_: Exception) { }
        if (!found) {
            Regex("""https?:[\\/]+[^"'\s\\]+\.(?:vtt|srt|ass)[^"'\s\\]*""").findAll(body).forEach {
                add(null, it.value.replace("\\/", "/"))
            }
        }
    }
}

class DpHotlinger : DpContentX() {
    override var name    = "DpHotlinger"
    override var mainUrl = "https://hotlinger.com"
}

class DpPichive : DpContentX() {
    override var name    = "DpPichive"
    override var mainUrl = "https://pichive.online"
}

class DpFourPichive : DpContentX() {
    override var name    = "DpFourPichive"
    override var mainUrl = "https://four.pichive.online"
}

// Movie iframes are served from sn.dplayer*.site and rewritten in loadLinks to
// sn.hotlinger.com. Without a registered extractor for that host loadExtractor
// never matches (matching is url.startsWith(schemaless mainUrl)), so movie
// sources silently produced nothing while series (four.pichive) kept working.
class DpSnHotlinger : DpContentX() {
    override var name    = "DpSnHotlinger"
    override var mainUrl = "https://sn.hotlinger.com"
}

/**
 * dplayer / np.js oynatıcısının "gate" iş kanıtı (Kasım 2026). NP_CONFIG.gate = {n, b}
 * varsa master/altyazı adreslerine &p=<çözüm> eklenmeli, yoksa CDN 404 verir.
 *
 * Sayfadaki window.NP_GATE fonksiyonu her yüklemede farklı üretilir: önek (String.fromCharCode),
 * ayraç, birleştirme sırası, sayı tabanı ve denetlenen SHA-256 sözcüğü/maskesi değişir.
 * Fonksiyon metni çözümlenip aynı arama yapılır. NP_GATE yoksa np.js'teki yedek kullanılır:
 *   sha256(n + ":" + i) ilk 32-bit sözcüğünün (32-b) sağa kaydırması 0 olmalı.
 * (neowatch NpKapi portu; yedek algoritması InatBox'ın x-st PoW'suyla aynı aile.)
 */
object DpNpGate {
    private const val LIMIT = 30_000_000

    fun solve(page: String, n: String, b: Int): String? =
        runCatching { custom(page, n) ?: fallback(n, b) }.getOrNull()

    private fun custom(page: String, n: String): String? {
        val f = Regex("""NP_GATE\s*=\s*function\s*\(\s*([\w$]+)\s*,\s*([\w$]+)\s*\)\s*\{([\s\S]*?)\}\s*;""")
            .find(page) ?: return null
        val (pN, pH, body) = f.destructured

        val consts = mutableMapOf<String, String>()
        Regex("""var\s+([\w$]+)\s*=\s*String\.fromCharCode\(([\d\s,]+)\)""").findAll(body).forEach { m ->
            consts[m.groupValues[1]] =
                m.groupValues[2].split(",").mapNotNull { it.trim().toIntOrNull()?.toChar() }.joinToString("")
        }
        Regex("""var\s+([\w$]+)\s*=\s*(["'])([^"']*)\2\s*[;,]""").findAll(body).forEach { m ->
            consts[m.groupValues[1]] = m.groupValues[3]
        }

        val loop = Regex("""var\s+([\w$]+)\s*=\s*[\w$]+\.toString\(\s*(\d+)\s*\)""").find(body) ?: return null
        val counter = loop.groupValues[1]
        val base = loop.groupValues[2].toInt().takeIf { it in 2..36 } ?: return null

        val call = Regex(Regex.escape(pH) + """\(([^()]*)\)\s*\[\s*(\d+)\s*]\s*&\s*(\d+)\s*\)?\s*===?\s*0""")
            .find(body) ?: return null
        val word = call.groupValues[2].toInt().takeIf { it in 0..7 } ?: return null
        val mask = call.groupValues[3].toLong()

        // Birleştirilen parçalar: n parametresi, sayaç (null), sabit değişkenler ve tırnaklı metinler.
        val parts: List<String?> = call.groupValues[1].split("+").map { p ->
            when {
                p == pN              -> n
                p == counter         -> null
                consts.containsKey(p) -> consts.getValue(p)
                Regex("""^(["'])(.*)\1$""").matches(p) -> p.substring(1, p.length - 1)
                else -> return null
            }
        }
        // Sayaç parçası yoksa PoW girdisi her turda aynı olur; anlamını yitirir.
        if (parts.none { it == null }) return null

        val md = java.security.MessageDigest.getInstance("SHA-256")
        for (i in 0 until LIMIT) {
            val s = i.toString(base)
            val input = parts.joinToString("") { it ?: s }
            val h = md.digest(input.toByteArray(Charsets.UTF_8))
            if ((wordAt(h, word) and mask) == 0L) return s
        }
        return null
    }

    private fun fallback(n: String, b: Int): String? {
        val shift = 32 - b.coerceIn(1, 31)
        val md = java.security.MessageDigest.getInstance("SHA-256")
        for (i in 0 until LIMIT) {
            val h = md.digest("$n:$i".toByteArray(Charsets.UTF_8))
            if ((wordAt(h, 0) ushr shift) == 0L) return i.toString()
        }
        return null
    }

    private fun wordAt(h: ByteArray, i: Int): Long {
        val o = i * 4
        return ((h[o].toLong() and 0xff) shl 24) or ((h[o + 1].toLong() and 0xff) shl 16) or
            ((h[o + 2].toLong() and 0xff) shl 8) or (h[o + 3].toLong() and 0xff)
    }
}
