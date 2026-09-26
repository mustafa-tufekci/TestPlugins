package com.panates

import android.util.Log
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.base64Decode
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.getAndUnpack
import com.lagradost.cloudstream3.utils.newExtractorLink
import com.lagradost.nicehttp.NiceResponse

open class HCCloseLoadExtractor : ExtractorApi() {
    override val name            = "CloseLoad"
    override val mainUrl         = "https://hdfilmcehennemi.mobi"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val extRef = referer ?: ""
        Log.d("Kekik_${this.name}", "url » $url")

        val iSource = app.get(url, referer = extRef)
        val packerScript = iSource.document.select("script")
            .find { it.data().contains("eval(function(p,a,c,k,e") }?.data()?.trim()
        getSubs(iSource, packerScript, subtitleCallback)

        val unpacked = packerScript?.let { script ->
            runCatching { getAndUnpack(script) }.getOrNull()
        }

        val haystack = iSource.text + "\n" + (unpacked ?: "")
        val slug = hdcSlugFromEmbedUrl(url)
        val streamUrl = hdcSelectStreamUrl(haystack, slug)
        if (streamUrl != null) {
            emitLink(callback, streamUrl)
            return
        }

        if (unpacked != null) {
            val legacyUrl = legacyDecode(unpacked)
            if (legacyUrl != null) {
                emitLink(callback, legacyUrl)
                return
            }
        }

        Log.d("Kekik_${this.name}", "no stream decoded » $url")
    }

    private suspend fun emitLink(callback: (ExtractorLink) -> Unit, url: String) {
        callback.invoke(
            newExtractorLink(
                source  = this.name,
                name    = this.name,
                url     = url,
                ExtractorLinkType.M3U8
            ) {
                this.referer = mainUrl
                this.quality = Qualities.Unknown.value
            }
        )
    }

    private fun getSubs(
        iSource: NiceResponse,
        obfuscatedScript: String?,
        subtitleCallback: (SubtitleFile) -> Unit
    ) {
        iSource.document.select("track").forEach {
            subtitleCallback.invoke(
                SubtitleFile(
                    lang = it.attr("label"),
                    url = mainUrl + it.attr("src")
                )
            )
        }
        val track = obfuscatedScript?.substringAfter("tracks: ")?.substringBefore("]") + "]"
        if (track.startsWith("[") && track.endsWith("]")) {
            Log.d("Kekik_${this.name}", "track -> $track")
            val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            val tracks: List<SubSource> = objectMapper.readValue(track)
            Log.d("Kekik_${this.name}", "tracks -> $tracks")
            tracks.forEach { it ->
                subtitleCallback.invoke(
                    SubtitleFile(
                        lang = it.label.toString(),
                        url = mainUrl + it.file.toString()
                    )
                )
            }
        }
    }

    private fun legacyDecode(rawScript: String): String? {
        val helloVarmi = rawScript.contains("dc_hello")
        var dcRegex = Regex("dc_hello\\(\"([^\"]*)\"\\)", setOf(RegexOption.IGNORE_CASE))
        if (!helloVarmi) {
            dcRegex = Regex("""dc_\w+\(\[(.*?)\]\)""", RegexOption.DOT_MATCHES_ALL)
        }
        val groupValues = dcRegex.find(rawScript)?.groupValues?.get(1) ?: return null

        return if (helloVarmi) {
            Log.d("Kekik_${this.name}", "groupValues » $groupValues")
            val dcUrl = dcHello(groupValues).substringAfter("http")
            Log.d("Kekik_${this.name}", "dcUrl » $dcUrl")
            dcUrl
        } else {
            val parts = groupValues.split(",")
                .map { it.trim().removeSurrounding("\"") }
            Log.d("Kekik_${this.name}", "parts » $parts")
            val dcUrl = dcNew(parts)
            Log.d("Kekik_${this.name}", "dcUrl » $dcUrl")
            dcUrl
        }
    }

    fun dcHello(base64Input: String): String {
        val decodedOnce = base64Decode(base64Input)
        val reversedString = decodedOnce.reversed()
        val decodedTwice = base64Decode(reversedString)
        val link    = if (decodedTwice.contains("+")) {
            decodedTwice.substringAfterLast("+")
        } else if (decodedTwice.contains(" ")) {
            decodedTwice.substringAfterLast(" ")
        } else if (decodedTwice.contains("|")){
            decodedTwice.substringAfterLast("|")
        } else {
            decodedTwice
        }
        return link
    }

    fun dcNew(parts: List<String>): String {
        val combinedValue = parts.joinToString("")
        val rot13Applied = combinedValue.map { char ->
            when (char) {
                in 'a'..'z' -> {
                    val shifted = char + 13
                    if (shifted > 'z') shifted - 26 else shifted
                }
                in 'A'..'Z' -> {
                    val shifted = char + 13
                    if (shifted > 'Z') shifted - 26 else shifted
                }
                else -> char
            }
        }.joinToString("")
        val base64DecodedString = base64Decode(rot13Applied)
        val reversedString = base64DecodedString.reversed()
        val result = StringBuilder()
        reversedString.forEachIndexed { i, char ->
            var charCode = char.code
            charCode = (charCode - (399756995 % (i + 5)) + 256) % 256
            result.append(charCode.toChar())
        }
        return result.toString()
    }
}

private data class SubSource(
    @JsonProperty("file") val file: String? = null,
    @JsonProperty("label") val label: String? = null,
    @JsonProperty("language") val language: String? = null,
    @JsonProperty("kind") val kind: String? = null
)
