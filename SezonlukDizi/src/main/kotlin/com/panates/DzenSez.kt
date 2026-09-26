package com.panates

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.json.JSONArray

class DzenSez : ExtractorApi() {
    override val name = "Dzen"
    override val mainUrl = "https://dzen.ru/"
    override val requiresReferer = false

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val document = app.get(
            url = url,
            headers = mapOf("X-Requested-With" to "XMLHttpRequest"),
            referer = this.mainUrl,
        ).document

        val script = document.select("script").find { it.data().contains("streams") }?.data() ?: return
        val content = script.substringAfter("\"streams\":").substringBefore("],") + "]"
        val streams = try {
            JSONArray(content)
        } catch (_: Exception) {
            return
        }

        for (i in 0 until streams.length()) {
            val stream = streams.optJSONObject(i) ?: continue
            val type = stream.optString("type", "")
            val streamUrl = stream.optString("url", "")
            if (streamUrl.isBlank()) continue

            val quality = when {
                type.contains("fullhd") -> Qualities.P1080.value
                type.contains("high") -> Qualities.P720.value
                type.contains("medium") -> Qualities.P480.value
                type.contains("low") -> Qualities.P360.value
                type.contains("lowest") -> Qualities.P240.value
                type.contains("tiny") -> Qualities.P144.value
                else -> Qualities.Unknown.value
            }
            val qualityName = Qualities.getStringByInt(quality)

            val linkType = when (type) {
                "hls" -> ExtractorLinkType.M3U8
                "dash" -> ExtractorLinkType.DASH
                else -> ExtractorLinkType.VIDEO
            }
            val label = when (type) {
                "hls" -> "HLS"
                "dash" -> "DASH"
                else -> qualityName
            }

            callback.invoke(
                newExtractorLink(
                    source = this.name + " - $label",
                    name = this.name + " - $label",
                    url = streamUrl,
                    type = linkType
                ) {
                    this.referer = ""
                    this.quality = quality
                }
            )
        }
    }
}
