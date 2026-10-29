package com.panates

import android.util.Log
import com.lagradost.cloudstream3.app

/** One live channel parsed from an M3U/M3U8 playlist. */
data class CanliM3uKanal(
    val name: String,
    val url: String,
    val group: String,
    val logo: String?,
    val headers: Map<String, String>
)

/**
 * Runtime IPTV source: the iptv-org Turkish playlist is fetched and parsed on demand,
 * so newly added/removed channels reach users without a plugin release.
 * #EXTVLCOPT http-user-agent / http-referrer lines become per-stream headers.
 */
object CanliTVIptv {
    private const val M3U_URL = "https://iptv-org.github.io/iptv/countries/tr.m3u"
    private const val CACHE_MS = 10 * 60 * 1000L
    private const val UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

    private val NSFW = listOf("nsfw", "xxx", "adult", "+18", "18+", "yetişkin", "erotik")

    private val GROUP_TR = mapOf(
        "general" to "Genel",
        "news" to "Haber",
        "sports" to "Spor",
        "music" to "Müzik",
        "kids" to "Çocuk",
        "documentary" to "Belgesel",
        "entertainment" to "Eğlence",
        "religious" to "Dini",
        "movies" to "Film",
        "culture" to "Kültür",
        "education" to "Eğitim",
        "animation" to "Animasyon",
        "business" to "İş",
        "travel" to "Gezi",
        "lifestyle" to "Yaşam",
        "series" to "Dizi",
        "family" to "Aile",
        "weather" to "Hava",
        "shop" to "Alışveriş",
        "undefined" to "Diğer"
    )

    @Volatile private var cache: List<CanliM3uKanal>? = null
    @Volatile private var cacheTime = 0L

    suspend fun kanallar(): List<CanliM3uKanal> {
        val now = System.currentTimeMillis()
        val cached = cache
        if (cached != null && now - cacheTime < CACHE_MS) return cached

        return try {
            val text = app.get(M3U_URL, headers = mapOf("User-Agent" to UA)).text
            parse(text).also {
                if (it.isNotEmpty()) {
                    cache = it
                    cacheTime = now
                }
            }
        } catch (e: Exception) {
            Log.e("CanliTV", "IPTV listesi alinamadi: ${e.message}")
            cached ?: emptyList()
        }
    }

    /** Turkish label for a raw `group-title` value ("News;Sports" -> "Haber / Spor"). */
    fun groupTr(raw: String): String {
        val parts = raw.split(";").map { it.trim() }.filter { it.isNotBlank() }
        if (parts.isEmpty()) return "Diğer"
        return parts.joinToString(" / ") { part ->
            GROUP_TR[part.lowercase()] ?: part
        }
    }

    /** True when the channel belongs to one of the given raw group tokens. */
    fun inGroups(kanal: CanliM3uKanal, tokens: List<String>): Boolean {
        if (tokens.isEmpty()) return true
        val parts = kanal.group.split(";").map { it.trim().lowercase() }
        return tokens.any { token -> parts.contains(token) }
    }

    fun parse(text: String): List<CanliM3uKanal> {
        if (!text.trimStart().startsWith("#EXTM3U")) return emptyList()

        val out = mutableListOf<CanliM3uKanal>()
        var pendingName: String? = null
        var pendingGroup = ""
        var pendingLogo: String? = null
        var pendingHeaders = mutableMapOf<String, String>()

        for (rawLine in text.lineSequence()) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            when {
                line.startsWith("#EXTINF") -> {
                    val header = line.substringAfter(":", "")
                    val attrs = parseAttrs(header)
                    pendingName = line.substringAfterLast(",", "").trim().ifBlank { null }
                    pendingGroup = attrs["group-title"].orEmpty()
                    pendingLogo = attrs["tvg-logo"]?.takeIf { it.isNotBlank() }
                    pendingHeaders = mutableMapOf()
                    // Some entries carry the headers inline on #EXTINF instead of #EXTVLCOPT.
                    attrs["http-user-agent"]?.takeIf { it.isNotBlank() }?.let {
                        pendingHeaders["user-agent"] = it
                    }
                    (attrs["http-referrer"] ?: attrs["http-referer"])?.takeIf { it.isNotBlank() }?.let {
                        pendingHeaders["referrer"] = it
                    }
                }

                line.startsWith("#EXTVLCOPT") -> {
                    val opt = line.substringAfter(":", "").trim()
                    val key = opt.substringBefore("=").trim().lowercase()
                    val value = opt.substringAfter("=", "").trim()
                    when (key) {
                        "http-user-agent" -> pendingHeaders["user-agent"] = value
                        "http-referrer", "http-referer" -> pendingHeaders["referrer"] = value
                    }
                }

                line.startsWith("#") -> Unit

                else -> {
                    val pieces = line.split("|")
                    val url = pieces.first().trim()
                    for (i in 1 until pieces.size) {
                        val kv = pieces[i].trim()
                        val k = kv.substringBefore("=", "").trim().lowercase()
                        val v = kv.substringAfter("=", "").trim()
                        if (v.isBlank()) continue
                        when (k) {
                            "user-agent" -> pendingHeaders["user-agent"] = v
                            "referrer", "referer" -> pendingHeaders["referrer"] = v
                        }
                    }

                    val name = pendingName
                    if (url.isNotBlank() && !name.isNullOrBlank() && isAllowed(pendingGroup)) {
                        out.add(
                            CanliM3uKanal(
                                name = name,
                                url = url,
                                group = pendingGroup.ifBlank { "Undefined" },
                                logo = pendingLogo,
                                headers = pendingHeaders.toMap()
                            )
                        )
                    }

                    pendingName = null
                    pendingGroup = ""
                    pendingLogo = null
                    pendingHeaders = mutableMapOf()
                }
            }
        }

        return out.distinctBy { it.url }
    }

    private fun isAllowed(group: String): Boolean {
        val g = group.lowercase()
        return NSFW.none { g.contains(it) }
    }

    private fun parseAttrs(header: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val regex = Regex("""([A-Za-z0-9_-]+)="([^"]*)"""")
        for (m in regex.findAll(header)) {
            map[m.groupValues[1].lowercase()] = m.groupValues[2]
        }
        return map
    }
}