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
 * dizipal15xx.com (see domainCandidates) — Next.js App Router (Turbopack) rewrite.
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
    /**
     * The site rotates its numbered domain every few weeks. Candidates are probed once per
     * app session (see [ensureDomain]) and mainUrl is pointed at the first one that answers,
     * so a rotation no longer needs an emergency plugin release.
     */
    private val domainCandidates = listOf(
        "https://dizipal1595.com", "https://dizipal1596.com", "https://dizipal1597.com",
        "https://dizipal1594.com", "https://dizipal1593.com", "https://dizipal1592.com",
    )

    override var mainUrl = domainCandidates.first()
    override var name = "DiziPal"
    override val hasMainPage = true
    override var lang = "tr"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries, TvType.Anime)

    override val mainPage = listOf(
        MainPageData("Anasayfa", "$mainUrl/"),
        MainPageData("Yeni Bölümler", "$mainUrl/yeni-eklenen-dizi-bolumler"),
        MainPageData("Yabancı Diziler", "$mainUrl/yabanci-dizi-izle"),
        MainPageData("Filmler", "$mainUrl/film-izle"),
        MainPageData("Platform Bölümleri", "$mainUrl/platform-bolumler"),
        MainPageData("Keşfet", "$mainUrl/kesfet"),
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        ensureDomain()
        if (page > 1) return newHomePageResponse(request.name, emptyList())
        // mainPage URLs were baked with the first candidate at construction; remap to the live domain.
        val doc = app.get(request.data.replace(domainCandidates.first(), mainUrl)).document
        val items = LinkedHashMap<String, SearchResponse>()

        doc.select("a.tc[href]").forEach { card ->
            val item = cardFromAnchor(card) ?: return@forEach
            items.putIfAbsent(item.url, item)
        }
        doc.select("a.sr[href^=/bolum/], a.ecl[href^=/bolum/], a.mwr[href^=/bolum/]").forEach { card ->
            val item = episodeCardFromAnchor(card) ?: return@forEach
            items.putIfAbsent(item.url, item)
        }
        return newHomePageResponse(request.name, items.values.toList())
    }

    @Volatile private var domainProbed = false

    /**
     * Probe the candidate domains once and keep the first whose homepage lists series cards.
     * A dead DNS or a parked page both fail the "/series/" content check. If nothing answers
     * (temporary outage) we keep the first candidate and still mark probed, to avoid probing
     * on every request; a plugin reload re-probes.
     */
    private suspend fun ensureDomain() {
        if (domainProbed) return
        for (d in domainCandidates) {
            val ok = runCatching {
                val res = app.get(d, timeout = 8L)
                res.isSuccessful && res.text.contains("/series/")
            }.getOrDefault(false)
            if (ok) {
                mainUrl = d
                break
            }
        }
        domainProbed = true
    }

    override suspend fun search(query: String): List<SearchResponse> {
        ensureDomain()
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
        val aria = a.attr("aria-label").trim()

        // aria-label biçimi: "<Dizi>, 4. Sezon 6. Bölüm, dublaj"
        val parcalar = aria.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val seriesName = parcalar.firstOrNull { !it.startsWith("Son:") && !it.contains("Sezon") }
            ?: slug.replace('-', ' ')

        val bolumEtiketi = Regex("\\d+\\.\\s*Sezon\\s*\\d+\\.\\s*Bölüm").find(aria)?.value
            ?: a.select("span").map { it.text().trim() }
                .firstOrNull { it.contains("Sezon") && it.contains("Bölüm") }
            ?: "${m.groupValues[2]}. Sezon ${m.groupValues[3]}. Bölüm"

        val title = "$seriesName - $bolumEtiketi"
        val poster = DpResim.kart(a)
        return newTvSeriesSearchResponse(title, "$mainUrl/series/$slug", TvType.TvSeries) {
            this.posterUrl = poster
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        ensureDomain()
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
        ensureDomain()
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
                val decrypted = decryptPlayer(raw) ?: DpRmk.decryptFromPage(raw) ?: return@forEach
                normalizePlayer(decrypted)
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

            // Unknown player layout: try our own ContentX/NP_CONFIG decoder first; if it
            // emits nothing, fall through to the extractor registry instead of giving up.
            if (token == null) {
                var emitted = false
                runCatching {
                    DpContentX().getUrl(playerUrl, "$mainUrl/", subtitleCallback) { link ->
                        emitted = true
                        callback(renameLink(link, label))
                    }
                }
                if (emitted) {
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
                var emitted = false
                DpContentX().getUrl(source2, "$mainUrl/", subtitleCallback) { link ->
                    emitted = true
                    callback(renameLink(link, label))
                }
                if (emitted) found = true
            }
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

    /**
     * player.sources → (etiket, href). Anahtar sırasına ve bitişikliğe bağlı değil:
     * "player":{ içindeki "sources" dizisi parantez eşleştirmeyle alınır, her üye
     * kendi içinde label/href aranır. (neowatch Rsc.kaynaklar portu)
     */
    private fun parseSources(raw: String): List<DpSource> {
        val playerPos = raw.indexOf("\"player\":{").takeIf { it >= 0 } ?: return emptyList()
        val arrPos = raw.indexOf("\"sources\":", playerPos)
        if (arrPos < 0) return emptyList()
        val arr = jsonBlock(raw, arrPos + "\"sources\":".length) ?: return emptyList()
        return Regex("\\{[^{}]*\\}").findAll(arr).mapNotNull { m ->
            val href = Regex("\"href\":\"([^\"]+)\"").find(m.value)?.groupValues?.get(1) ?: return@mapNotNull null
            val label = Regex("\"label\":\"([^\"]+)\"").find(m.value)?.groupValues?.get(1) ?: return@mapNotNull null
            DpSource(label, href)
        }.toList()
    }

    /**
     * RSC'deki player.enc {ciphertext,iv,salt} + "k" parolasıyla iframe adresini çözer.
     * Anahtar sırasına/bitişikliğe bağlı değil: enc nesnesi parantez eşleştirmeyle alınır,
     * "k" enc konumundan sonraki ilk eşleşmeden okunur; "k" yoksa ya da çözüm başarısızsa
     * site paketindeki sabit parola (DpRmk.PAROLA) denenir. (neowatch oynaticiCoz portu)
     */
    private fun decryptPlayer(raw: String): String? {
        val playerPos = raw.indexOf("\"player\":{").takeIf { it >= 0 } ?: return null
        val encPos = raw.indexOf("\"enc\":", playerPos).takeIf { it >= 0 } ?: return null
        val encObj = jsonBlock(raw, encPos + "\"enc\":".length) ?: return null

        Regex("\"k\":\"([^\"]+)\"").find(raw, encPos)?.groupValues?.get(1)?.let { parola ->
            runCatching {
                val ciphertext = Regex("\"ciphertext\":\"([^\"]+)\"").find(encObj)?.groupValues?.get(1) ?: return@runCatching null
                val iv = Regex("\"iv\":\"([^\"]+)\"").find(encObj)?.groupValues?.get(1) ?: return@runCatching null
                val salt = Regex("\"salt\":\"([^\"]+)\"").find(encObj)?.groupValues?.get(1) ?: return@runCatching null
                val key = pbkdf2Sha512(parola.toByteArray(Charsets.UTF_8), hexToBytes(salt), 999, 32)
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(hexToBytes(iv)))
                String(cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP)), Charsets.UTF_8).trim()
            }.getOrNull()?.let { return it }
        }
        // Sabit parola yedeği: DpRmk.decrypt enc nesnesini PAROLA ile çözer.
        return DpRmk.decrypt(encObj)?.trim()
    }

    /** [bas] konumundan itibaren ilk {…} ya da […] bloğunu dize kaçışlarına dikkat ederek döndürür. */
    private fun jsonBlock(s: String, from: Int): String? {
        var i = from
        while (i < s.length && s[i] != '{' && s[i] != '[') {
            if (!s[i].isWhitespace()) return null
            i++
        }
        if (i >= s.length) return null
        val opener = s[i]
        val closer = if (opener == '{') '}' else ']'
        var depth = 0
        while (i < s.length) {
            when (val c = s[i]) {
                '"' -> { i = skipString(s, i + 1); continue }
                opener -> depth++
                closer -> {
                    depth--
                    if (depth == 0) return s.substring(from, i + 1)
                }
            }
            i++
        }
        return null
    }

    /** [bas] konumundaki açılış tırnağından kapanış tırnağına kadar atlar; kapanış sonrası konumu döndürür. */
    private fun skipString(s: String, from: Int): Int {
        var i = from
        while (i < s.length) {
            when (s[i]) {
                '\\' -> i++
                '"' -> return i + 1
            }
            i++
        }
        return i
    }

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
