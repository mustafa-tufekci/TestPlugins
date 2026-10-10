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

open class SlcContentX : ExtractorApi() {
    override val name            = "SlcContentX"
    override val mainUrl         = "https://contentx.me"
    override val requiresReferer = true

    private val jsonMapper by lazy { ObjectMapper() }

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val extRef   = referer ?: ""
        Log.d("SelcukFlix", "url » $url")

        val iSource = app.get(url, referer = extRef).text
        val iExtract = Regex("""window\.openPlayer\('([^']+)'""").find(iSource)?.groups?.get(1)?.value ?: throw ErrorLoadingException("iExtract is null")

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

        val vidSource  = app.get("${mainUrl}/source2.php?v=${iExtract}", referer=extRef).text

        // 2) source2.php JSON'u içindeki altyazı parçaları
        runCatching { collectJsonSubs(jsonMapper.readTree(vidSource)) { lang, raw -> addSub(lang, raw) } }

        // 3) Yeni player akışı: altyazılar track.php üzerinden ayrıca yükleniyor
        runCatching { fetchTrackSubs(iExtract, url, extRef.ifBlank { url }) { lang, raw -> addSub(lang, raw) } }

        Log.d("SelcukFlix", "subtitle » ${subPairs.map { it.second }}")

        subPairs.forEach { (subLang, subUrl) ->
            subtitleCallback.invoke(SubtitleFile(lang = subLang, url = subUrl))
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

class SlcHotlinger : SlcContentX() {
    override var name    = "SlcHotlinger"
    override var mainUrl = "https://hotlinger.com"
}

class SlcPichive : SlcContentX() {
    override var name    = "SlcPichive"
    override var mainUrl = "https://pichive.online"
}

class SlcFourPichive : SlcContentX() {
    override var name    = "SlcFourPichive"
    override var mainUrl = "https://four.pichive.online"
}
