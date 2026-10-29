package com.panates

import android.util.Base64
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newExtractorLink
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * dizipal1592.com — Next.js App Router (Turbopack) rewrite.
 *
 * The site renders server components, so every page ships its data as an escaped RSC
 * flight payload (`self.__next_f.push([1,"..."])`). Cards are plain anchors:
 *   series -> a.tc[href=/series/<slug>]      movies -> a.tc[href=/movies/<slug>]
 *   episodes -> a.sr[href=/bolum/<slug>-<s>x<e>]
 * Search lives at /arama-yap?s=<query> (the /arama?q= route is client-side only and 404s).
 *
 * Playback: the player config holds an AES-256-CBC blob (`enc`) plus the password (`k`):
 *   key = PBKDF2-HMAC-SHA512(k, salt, iterations = 999, dkLen = 32)
 * decrypted value is a player URL such as `//sn.dplayer82.site/iframe.php?v=<id>`, which is
 * rewritten to sn.hotlinger.com (same ContentX player, reachable) and handed to the bundled
 * DpSnHotlinger extractor.
 */
class DiziPal : MainAPI() {
    override var mainUrl = "https://dizipal1592.com"
    override var name = "DiziPal"
    override val hasMainPage = true
    override var lang = "tr"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries, TvType.Anime)

    override val mainPage = listOf(
        MainPageData("$mainUrl/", "Anasayfa"),
        MainPageData("$mainUrl/yeni-eklenen-dizi-bolumler", "Yeni Bölümler"),
        MainPageData("$mainUrl/yabanci-dizi-izle", "Yabancı Diziler"),
        MainPageData("$mainUrl/film-izle", "Filmler"),
        MainPageData("$mainUrl/platform-bolumler", "Platform Bölümleri"),
        MainPageData("$mainUrl/kesfet", "Keşfet"),
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        if (page > 1) return newHomePageResponse(request.name, emptyList())
        val doc = app.get(request.data).document
        val items = LinkedHashMap<String, SearchResponse>()

        doc.select("a.tc[href]").forEach { card ->
            val item = cardFromAnchor(card) ?: return@forEach
            items.putIfAbsent(item.url, item)
        }
        doc.select("a.sr[href^=/bolum/]").forEach { card ->
            val item = episodeCardFromAnchor(card) ?: return@forEach
            items.putIfAbsent(item.url, item)
        }
        return newHomePageResponse(request.name, items.values.toList())
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/arama-yap", params = mapOf("s" to query)).document
        val out = LinkedHashMap<String, SearchResponse>()
        doc.select("a.tc[href], a[href^=/series/], a[href^=/movies/]").forEach { card ->
            val item = cardFromAnchor(card) ?: return@forEach
            out.putIfAbsent(item.url, item)
        }
        return out.values.toList()
    }

    private fun cardFromAnchor(a: org.jsoup.nodes.Element): SearchResponse? {
        val href = a.attr("href").trim()
        val isMovie = href.startsWith("/movies/")
        if (!isMovie && !href.startsWith("/series/")) return null
        val fullUrl = "$mainUrl$href"
        val aria = a.attr("aria-label")
        val label = a.selectFirst("span.tt")?.text()?.trim()
        val title = label?.takeIf { it.isNotBlank() }
            ?: aria.substringBefore(",").trim().takeIf { it.isNotBlank() }
            ?: a.text().trim().takeIf { it.isNotBlank() }
            ?: return null
        val poster = DpResim.kart(a)
        val year = Regex("\\b(19|20)\\d{2}\\b").find(aria)?.value?.toIntOrNull()
            ?: Regex("\\b(19|20)\\d{2}\\b").find(a.text())?.value?.toIntOrNull()

        return if (isMovie) {
            newMovieSearchResponse(title, fullUrl, TvType.Movie) {
                this.posterUrl = poster
                this.year = year
            }
        } else {
            newTvSeriesSearchResponse(title, fullUrl, TvType.TvSeries) {
                this.posterUrl = poster
                this.year = year
            }
        }
    }

    /** "Yeni Bölümler" cards point at an episode; they open the series page instead. */
    private fun episodeCardFromAnchor(a: org.jsoup.nodes.Element): SearchResponse? {
        val href = a.attr("href").trim()
        val m = Regex("^/bolum/(.+?)-(\\d+)x(\\d+)$").find(href) ?: return null
        val slug = m.groupValues[1]
        val spans = a.select("span").map { it.text().trim() }.filter { it.isNotBlank() }
        val seriesName = spans.firstOrNull { !it.matches(Regex("\\d+")) && !it.startsWith("Son:") }
            ?: slug.replace('-', ' ')
        val episodeInfo = spans.firstOrNull { it.startsWith("Son:") }
            ?.removePrefix("Son:")?.trim()
        val title = if (episodeInfo.isNullOrBlank()) seriesName else "$seriesName - $episodeInfo"
        val poster = DpResim.kart(a)
        return newTvSeriesSearchResponse(title, "$mainUrl/series/$slug", TvType.TvSeries) {
            this.posterUrl = poster
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val html = unescape(app.get(url).text)
        val isMovie = url.contains("/movies/")

        val title = firstGroup(html, "\"mjTitle\":\"([^\"]+)\"")
            ?: app.get(url).document.selectFirst("h1")?.text()?.trim()
            ?: return null
        val poster = firstGroup(html, "\"mjPoster\":\"([^\"]+)\"")
        val year = firstGroup(html, "\"mjYear\":\"((?:19|20)\\d{2})")?.toIntOrNull()
        val plot = firstGroup(html, "\"mjOverview\":\"(.*?)\",\"")
        val imdb = firstGroup(html, "\"mjImdb\":\"([0-9.]+)")
        val trailerId = firstGroup(html, "\"trailerRaw\":\"([^\"]+)\"")
        val tags = groupBlock(html, "\"mjGenres\":\\[(.*?)\\]")
            .mapNotNull { Regex("\"label\":\"([^\"]+)\"").find(it)?.groupValues?.get(1) }
        val cast = groupBlock(html, "\"mjCast\":\\[(.*?)\\]")
            .mapNotNull { Regex("\"label\":\"([^\"]+)\"").find(it)?.groupValues?.get(1) }

        if (isMovie) {
            val response = newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = plot
                this.year = year
                this.tags = tags
                this.score = Score.from10(imdb)
            }
            if (cast.isNotEmpty()) response.addActors(cast)
            if (!trailerId.isNullOrBlank()) {
                runCatching { response.addTrailer("https://www.youtube.com/watch?v=$trailerId") }
            }
            return response
        }

        val episodes = ArrayList<Episode>()
        Regex(
            "\"no\":(\\d+),\"label\":\"[^\"]*\",\"href\":\"[^\"]*\",\"episodes\":\\[([^\\]]*)\\]"
        ).findAll(html).forEach { season ->
            val seasonNo = season.groupValues[1].toIntOrNull() ?: return@forEach
            Regex("\"n\":(\\d+),\"title\":\"([^\"]*)\",\"date\":\"[^\"]*\",\"href\":\"([^\"]*)\"")
                .findAll(season.groupValues[2]).forEach { ep ->
                    val epNo = ep.groupValues[1].toIntOrNull() ?: return@forEach
                    val epName = ep.groupValues[2].ifBlank { null }
                    val epHref = ep.groupValues[3].ifBlank { return@forEach }
                    episodes.add(newEpisode("$mainUrl$epHref") {
                        this.name = epName
                        this.season = seasonNo
                        this.episode = epNo
                        this.posterUrl = poster
                    })
                }
        }

        val response = newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = poster
            this.plot = plot
            this.year = year
            this.tags = tags
            this.score = Score.from10(imdb)
        }
        if (cast.isNotEmpty()) response.addActors(cast)
        if (!trailerId.isNullOrBlank()) {
            runCatching { response.addTrailer("https://www.youtube.com/watch?v=$trailerId") }
        }
        return response
    }

    override suspend fun loadLinks(
        data: String,
        isCine: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val firstRaw = unescape(app.get(data).text)
        val sources = parseSources(firstRaw)
        val pages = LinkedHashMap<String, String>()
        pages[data] = firstRaw
        sources.forEach { source ->
            val full = if (source.href.startsWith("http")) source.href else "$mainUrl${source.href}"
            if (!pages.containsKey(full)) {
                pages[full] = runCatching { unescape(app.get(full).text) }.getOrNull() ?: return@forEach
            }
        }

        var found = false
        val seen = HashSet<String>()
        pages.forEach { (pageUrl, raw) ->
            val label = sources.firstOrNull {
                val full = if (it.href.startsWith("http")) it.href else "$mainUrl${it.href}"
                full == pageUrl
            }?.label ?: "DiziPal"
            val playerUrl = run {
                val enc = parseEnc(raw)
                val decrypted = if (enc != null) decryptPlayer(enc) else null
                val fallback = decrypted ?: DpRmk.decryptFromPage(raw) ?: return@forEach
                normalizePlayer(fallback)
            }
            if (!seen.add(playerUrl)) return@forEach

            // Host-specific decoders ported from the neowatch DiziPal family.
            val special = when {
                playerUrl.contains("formationfeed") -> DpJwKaynak()
                playerUrl.contains("streamcorecdn") -> DpStreamCore()
                playerUrl.contains("player.html?video=") -> DpVideoPlays()
                else -> null
            }
            if (special != null) {
                runCatching {
                    special.getUrl(playerUrl, "$mainUrl/", subtitleCallback) { link ->
                        callback(renameLink(link, label))
                    }
                }.onSuccess { found = true }
                return@forEach
            }

            val iframe = runCatching { app.get(playerUrl, referer = "$mainUrl/").text }.getOrNull()
                ?: return@forEach
            val token = Regex("openPlayer\\('([^']+)'").find(iframe)?.groupValues?.get(1)

            // Unknown player layout: try our own ContentX/NP_CONFIG decoder first, then the registry.
            if (token == null) {
                val handled = runCatching {
                    DpContentX().getUrl(playerUrl, "$mainUrl/", subtitleCallback) { link ->
                        callback(renameLink(link, label))
                    }
                    true
                }.getOrDefault(false)
                if (handled) {
                    found = true
                    return@forEach
                }
                runCatching { loadExtractor(playerUrl, "$mainUrl/", subtitleCallback, callback) }
                    .onSuccess { if (it) found = true }
                return@forEach
            }

            val host = playerUrl.substringAfter("://").substringBefore("/")
            val source2 = "https://$host/source2.php?v=$token"

            runCatching {
                DpContentX().getUrl(source2, "$mainUrl/", subtitleCallback) { link ->
                    callback(renameLink(link, label))
                }
            }.onSuccess { found = true }
        }
        return found
    }

    private fun renameLink(link: ExtractorLink, newName: String): ExtractorLink = ExtractorLink(
        source = newName,
        name = newName,
        url = link.url,
        referer = link.referer,
        quality = link.quality,
        headers = link.headers,
        extractorData = link.extractorData,
        type = link.type
    )

    // ── player payload ────────────────────────────────────────────────────

    private data class DpSource(val label: String, val href: String)
    private data class DpEnc(val ciphertext: String, val iv: String, val salt: String, val key: String)

    private fun parseSources(raw: String): List<DpSource> {
        val block = Regex("\"sources\":\\[(.*?)\\],\"language\"", RegexOption.DOT_MATCHES_ALL)
            .find(raw)?.groupValues?.get(1) ?: return emptyList()
        return Regex("\\{\"label\":\"([^\"]+)\",\"quality\":\"([^\"]*)\",\"href\":\"([^\"]+)\"")
            .findAll(block)
            .map { DpSource(it.groupValues[1], it.groupValues[3]) }
            .toList()
    }

    private fun parseEnc(raw: String): DpEnc? {
        val m = Regex(
            "\"enc\":\\{\"ciphertext\":\"([^\"]+)\",\"iv\":\"([^\"]+)\",\"salt\":\"([^\"]+)\"\\},\"k\":\"([^\"]+)\""
        ).find(raw) ?: return null
        return DpEnc(m.groupValues[1], m.groupValues[2], m.groupValues[3], m.groupValues[4])
    }

    private fun decryptPlayer(enc: DpEnc): String? = runCatching {
        val key = pbkdf2Sha512(enc.key.toByteArray(Charsets.UTF_8), hexToBytes(enc.salt), 999, 32)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(hexToBytes(enc.iv)))
        val plain = cipher.doFinal(Base64.decode(enc.ciphertext, Base64.NO_WRAP))
        String(plain, Charsets.UTF_8).trim()
    }.getOrNull()

    /** PBKDF2-HMAC-SHA512 done by hand: the platform SecretKeyFactory variant needs API 26. */
    private fun pbkdf2Sha512(password: ByteArray, salt: ByteArray, iterations: Int, dkLen: Int): ByteArray {
        val mac = Mac.getInstance("HmacSHA512")
        mac.init(SecretKeySpec(password, "HmacSHA512"))
        val out = ByteArray(dkLen)
        var offset = 0
        var blockIndex = 1
        while (offset < dkLen) {
            mac.update(salt)
            mac.update(
                byteArrayOf(
                    (blockIndex ushr 24).toByte(),
                    (blockIndex ushr 16).toByte(),
                    (blockIndex ushr 8).toByte(),
                    blockIndex.toByte()
                )
            )
            var u = mac.doFinal()
            val t = u.copyOf()
            repeat(iterations - 1) {
                u = mac.doFinal(u)
                for (i in t.indices) t[i] = (t[i].toInt() xor u[i].toInt()).toByte()
            }
            val len = minOf(t.size, dkLen - offset)
            System.arraycopy(t, 0, out, offset, len)
            offset += len
            blockIndex++
        }
        return out
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.trim()
        val out = ByteArray(clean.length / 2)
        for (i in out.indices) {
            out[i] = ((Character.digit(clean[i * 2], 16) shl 4) or Character.digit(clean[i * 2 + 1], 16)).toByte()
        }
        return out
    }

    private fun normalizePlayer(url: String): String {
        val out = url.trim()
        return when {
            out.startsWith("http://") || out.startsWith("https://") -> out
            out.startsWith("//") -> "https:$out"
            out.isBlank() -> out
            // Şemasız gelen oynatıcı adresleri (sn.dplayer*.site/...) app.get'te patlıyordu.
            else -> "https://$out"
        }
    }

    // ── tiny parsing helpers ──────────────────────────────────────────────

    private fun unescape(html: String): String =
        html.replace("\\\"", "\"").replace("\\\\/", "/")

    private fun firstGroup(text: String, pattern: String): String? =
        Regex(pattern, RegexOption.DOT_MATCHES_ALL).find(text)?.groupValues?.get(1)?.trim()

    private fun groupBlock(text: String, pattern: String): List<String> {
        val block = Regex(pattern, RegexOption.DOT_MATCHES_ALL).find(text)?.groupValues?.get(1)
            ?: return emptyList()
        return listOf(block)
    }
}
