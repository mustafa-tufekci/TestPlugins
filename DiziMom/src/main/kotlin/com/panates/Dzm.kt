package com.panates

import android.util.Log
import org.json.JSONObject
import org.jsoup.nodes.Element
import java.net.URLEncoder
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

class Dzm : MainAPI() {
    override var mainUrl              = "https://ddizimom.net"
    override var name                 = "DiziMom"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val supportedTypes       = setOf(TvType.TvSeries, TvType.Movie)

    companion object {
        private const val DEFAULT_PLAYER_BASE = "https://pilavyerplay.top"
        private val tarayici = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            "Accept-Language" to "tr-TR,tr;q=0.9"
        )
    }

    override val mainPage = mainPageOf(
        "$mainUrl/"         to "Ana Sayfa",
        "$mainUrl/diziler"  to "Diziler",
        "$mainUrl/filmler"  to "Filmler",
        "$mainUrl/animes"   to "Animeler",
        "$mainUrl/takvim"   to "Takvim"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if (page <= 1) request.data else "${request.data}?page=$page"
        val document = app.get(url).document
        val home = document.select("a.group.block[href]")
            .mapNotNull { it.kart() }
            .distinctBy { it.url }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.kart(): SearchResponse? {
        val href = fixUrlNull(attr("href")) ?: return null
        val dizi = href.contains("/dizi/")
        val film = href.contains("/film/")
        val anime = href.contains("/anime/")
        if (!dizi && !film && !anime) return null

        val baslik = (selectFirst("img")?.attr("alt")?.substringBefore(" izle")?.trim()?.ifBlank { null }
            ?: attr("title").trim().ifBlank { null }
            ?: selectFirst("h3, .font-semibold")?.text()?.trim()?.ifBlank { null }
            ?: return null)

        val posterUrl = resim(selectFirst("img")?.attr("src")
            ?: selectFirst("img")?.attr("data-src"))

        return if (film) {
            newMovieSearchResponse(baslik, href, TvType.Movie) {
                this.posterUrl = posterUrl
            }
        } else {
            newTvSeriesSearchResponse(baslik, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
            }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/ara", params = mapOf("q" to query)).document
        return document.select("a.group.block[href]")
            .mapNotNull { it.kart() }
            .distinctBy { it.url }
    }

    // ── Load ─────────────────────────────────────────────────────────────

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val veri = yapisalVeri(document.html())
        val film = url.contains("/film/")

        val baslik = veri?.optString("name")?.ifBlank { null }
            ?: document.selectFirst("h1")?.text()?.trim()?.ifBlank { null }
            ?: return null

        val posterUrl = resim(veri?.optString("image"))
            ?: resim(document.selectFirst("meta[property=og:image]")?.attr("content"))

        val aciklama = veri?.optString("description")?.ifBlank { null }
            ?: document.selectFirst("meta[name=description]")?.attr("content")

        val yil = veri?.optString("startDate")?.take(4)?.toIntOrNull()
            ?: Regex("\\b(19|20)\\d{2}\\b").find(document.text())?.value?.toIntOrNull()

        val etiketler = mutableListOf<String>()
        veri?.optJSONArray("genre")?.let { arr ->
            for (i in 0 until arr.length()) arr.optString(i).takeIf { it.isNotBlank() }?.let { etiketler.add(it) }
        }

        val oyuncular = mutableListOf<String>()
        veri?.optJSONArray("actor")?.let { arr ->
            for (i in 0 until arr.length()) {
                val ad = arr.optJSONObject(i)?.optString("name")?.ifBlank { null } ?: continue
                oyuncular.add(ad)
            }
        }

        val fragman = document.selectFirst("iframe[src*=youtube], iframe[src*=youtu.be]")?.attr("src")

        return if (film) {
            newMovieLoadResponse(baslik, url, TvType.Movie, url) {
                this.posterUrl = posterUrl
                this.plot = aciklama
                this.year = yil
                this.tags = etiketler
                this.actors = oyuncular.map { ActorData(Actor(it), ActorRole.Main) }
                addTrailer(fixUrlNull(fragman))
            }
        } else {
            val bolumler = bolumler(document)
            newTvSeriesLoadResponse(baslik, url, TvType.TvSeries, bolumler) {
                this.posterUrl = posterUrl
                this.plot = aciklama
                this.year = yil
                this.tags = etiketler
                this.actors = oyuncular.map { ActorData(Actor(it), ActorRole.Main) }
            }
        }
    }

    private fun bolumler(document: org.jsoup.nodes.Document): List<Episode> {
        val gorulen = mutableSetOf<String>()
        return document.select("a[href*='/bolum-'], a[href*=\"/bolum\"]")
            .mapNotNull { a ->
                val href = fixUrlNull(a.attr("href")) ?: return@mapNotNull null
                if (!href.contains("/sezon") && !href.contains("-sezon")) return@mapNotNull null
                if (!gorulen.add(href)) return@mapNotNull null

                val m = Regex("-(\\d+)-sezon/bolum-(\\d+)").find(href)
                val metin = a.text().trim()
                val sezon = m?.groupValues?.get(1)?.toIntOrNull()
                    ?: Regex("(\\d+)\\.\\s*Sezon").find(metin)?.groupValues?.get(1)?.toIntOrNull()
                    ?: 1
                val bolum = m?.groupValues?.get(2)?.toIntOrNull()
                    ?: Regex("(\\d+)\\.\\s*Bölüm").find(metin)?.groupValues?.get(1)?.toIntOrNull()
                    ?: return@mapNotNull null

                newEpisode(href) {
                    this.name = metin.ifBlank { "$sezon. Sezon $bolum. Bölüm" }
                    this.season = sezon
                    this.episode = bolum
                }
            }
            .distinctBy { it.data }
    }

    private fun yapisalVeri(html: String): JSONObject? {
        val blok = Regex("""<script[^>]+application/ld\+json[^>]*>(.*?)</script>""", RegexOption.DOT_MATCHES_ALL)
        for (eslesme in blok.findAll(html)) {
            val kok = runCatching { JSONObject(eslesme.groupValues[1].trim()) }.getOrNull() ?: continue
            kok.optJSONArray("@graph")?.let { graf ->
                for (i in 0 until graf.length()) {
                    val dugum = graf.optJSONObject(i) ?: continue
                    if (dugum.optString("@type") in setOf("TVSeries", "Movie")) return dugum
                }
            }
            if (kok.optString("@type") in setOf("TVSeries", "Movie")) return kok
        }
        return null
    }

    private fun resim(ham: String?): String? {
        var adres = ham?.trim().orEmpty()
        if (adres.isBlank()) return null
        adres = adres.substringBefore('#')
        if (adres.startsWith("//")) adres = "https:$adres"
        if (!adres.startsWith("http")) return adres

        if (adres.contains("/storage/tmdb/posters/w")) {
            adres = Regex("/storage/tmdb/posters/w\\d+/").replace(adres, "/storage/tmdb/posters/w780/")
        }
        if (adres.contains("image.tmdb.org/t/p/w")) {
            adres = Regex("/t/p/w\\d+/").replace(adres, "/t/p/w500/")
        }
        return adres
    }

    // ── Links ───────────────────────────────────────────────────────────

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val watchPage = runCatching { app.get(data).document }.getOrNull() ?: return false
        var found = false

        // 1. Pilavyer (data-pv + assets/js/core.js)
        val dataPv = watchPage.selectFirst("div[data-pv]")?.attr("data-pv")
        val coreScriptSrc = watchPage.selectFirst("script[src*='pilavyerplay'], script[src*='/assets/js/core.js']")?.attr("src")

        val rawBase = if (!coreScriptSrc.isNullOrBlank()) {
            coreScriptSrc.substringBefore("/assets/js/core.js").substringBefore("/e/c.js").trim()
        } else {
            DEFAULT_PLAYER_BASE
        }
        val playerBase = when {
            rawBase.isBlank() -> DEFAULT_PLAYER_BASE
            rawBase.startsWith("//") -> "https:$rawBase"
            !rawBase.startsWith("http") -> "https://${rawBase.removePrefix("/")}"
            else -> rawBase.trimEnd('/')
        }

        if (!dataPv.isNullOrBlank()) {
            val playerUrl = "$playerBase/assets/js/s.php?s=${URLEncoder.encode(dataPv, "UTF-8")}"
            runCatching {
                val playerHtml = app.get(
                    playerUrl,
                    headers = mapOf(
                        "User-Agent" to tarayici["User-Agent"]!!,
                        "Referer" to "$playerBase/"
                    )
                ).text

                val eslesme = Regex("""window\.__PLAYER__\s*=\s*(\{.*?\});""").find(playerHtml)
                    ?: return@runCatching
                val json = JSONObject(eslesme.groupValues[1])
                val streamUrl = json.optString("stream")
                if (streamUrl.isBlank()) return@runCatching

                json.optJSONArray("subs")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val sub = arr.optJSONObject(i) ?: continue
                        val src = sub.optString("src").ifBlank { continue }
                        val etiket = sub.optString("label").ifBlank { "Türkçe" }
                        subtitleCallback(SubtitleFile(etiket, fixUrl(src)).apply {
                            headers = mapOf(
                                "Referer" to "$playerBase/",
                                "User-Agent" to tarayici["User-Agent"]!!
                            )
                        })
                    }
                }

                val diller = mutableListOf<String>()
                json.optJSONArray("audios")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        arr.optJSONObject(i)?.optString("label")?.takeIf { it.isNotBlank() }?.let { diller.add(it) }
                    }
                }
                val ad = if (diller.isNotEmpty()) "DiziMom (${diller.joinToString(" / ")})" else "DiziMom (HLS)"

                callback(
                    newExtractorLink(
                        source = "Pilavyer",
                        name = ad,
                        url = streamUrl,
                        type = ExtractorLinkType.M3U8
                    ) {
                        this.referer = "$playerBase/"
                        this.quality = Qualities.P1080.value
                        this.headers = mapOf(
                            "Referer" to "$playerBase/",
                            "User-Agent" to tarayici["User-Agent"]!!
                        )
                    }
                )
                found = true
            }.onFailure { Log.d("DIZIMOM", "pilavyer » ${it.message}") }
        }

        // 2. Yedek: iframe'ler
        if (!found) {
            for (iframe in watchPage.select("iframe[src], iframe[data-src]").map { it.attr("src").ifBlank { it.attr("data-src") } }) {
                val adres = fixUrlNull(iframe) ?: continue
                if (adres.contains("youtube") || adres.contains("youtu.be")) continue
                if (loadExtractor(adres, data, subtitleCallback, callback)) found = true
            }
        }

        return found
    }
}
