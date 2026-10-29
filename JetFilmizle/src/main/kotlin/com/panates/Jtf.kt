package com.panates

import android.util.Log
import org.json.JSONObject
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

/**
 * jetfilmizle.io kapandı; site jetfilmizle.app adresinde Bootstrap tabanlı yeni bir tema ile devam ediyor.
 * Oynatıcı akışı:
 *   POST /jetplayer (film_id, source_index, player_type) -> <iframe src="https://playerx.info/?id=NNNN">
 *   playerx sayfası -> var initialPlayback = {...} -> item.sources[].file (göreli), item.tracks[] (altyazı)
 *   m3u8 isteği playerx kökenli Referer ister.
 */
class Jtf : MainAPI() {
    override var mainUrl              = "https://jetfilmizle.app"
    override var name                 = "JetFilmizle"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val supportedTypes       = setOf(TvType.Movie, TvType.TvSeries)

    private val tarayici = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    )

    override val mainPage = mainPageOf(
        "${mainUrl}/filmler/SAYFA"        to "Son Filmler",
        "${mainUrl}/turkce-dublaj/SAYFA"  to "Türkçe Dublaj",
        "${mainUrl}/turkce-altyazili/SAYFA" to "Türkçe Altyazılı",
        "${mainUrl}/yerli-filmler/SAYFA"  to "Yerli Filmler",
        "${mainUrl}/diziler/SAYFA"        to "Diziler",
        "${mainUrl}/trendler/SAYFA"       to "Trendler"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        // ! İlk sayfa /filmler, sonrakiler /filmler/sayfa-2 biçiminde.
        val url = if (page <= 1) request.data.replace("SAYFA", "").trimEnd('/') + "/"
        else request.data.replace("SAYFA", "sayfa-$page").trimEnd('/')

        val document = app.get(url, headers = tarayici).document
        val home     = document.select("div.film-card, div.card.film-card").mapNotNull { it.toSearchResult() }.distinctBy { it.url }

        return newHomePageResponse(request.name, home, hasNext = home.isNotEmpty())
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val a = this.selectFirst("a[href]") ?: return null
        val href = fixUrlNull(a.attr("href")) ?: return null
        if (!href.contains("/film/") && !href.contains("/dizi/")) return null

        val baslik = (a.attr("title").ifBlank { null }
            ?: this.selectFirst("img")?.attr("alt")?.ifBlank { null }
            ?: return null)
            .substringBefore(" Full ")
            .substringBefore(" izle")
            .trim()

        val posterUrl = JtfResim.kart(this)
        val tur       = if (href.contains("/dizi/")) TvType.TvSeries else TvType.Movie

        return if (tur == TvType.Movie) {
            newMovieSearchResponse(baslik, href, TvType.Movie) { this.posterUrl = posterUrl }
        } else {
            newTvSeriesSearchResponse(baslik, href, TvType.TvSeries) { this.posterUrl = posterUrl }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("${mainUrl}/arama", params = mapOf("q" to query), headers = tarayici).document

        return document.select("div.film-card, div.card.film-card").mapNotNull { it.toSearchResult() }.distinctBy { it.url }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    private fun yapisalVeri(html: String): JSONObject? {
        for (eslesme in Regex("""<script[^>]+application/ld\+json[^>]*>(.*?)</script>""", RegexOption.DOT_MATCHES_ALL).findAll(html)) {
            val kok = runCatching { JSONObject(eslesme.groupValues[1].trim()) }.getOrNull() ?: continue
            val dugumler = kok.optJSONArray("@graph")
            if (dugumler != null) {
                for (i in 0 until dugumler.length()) {
                    val d = dugumler.optJSONObject(i) ?: continue
                    if (d.optString("@type") in setOf("Movie", "TVSeries")) return d
                }
            }
            if (kok.optString("@type") in setOf("Movie", "TVSeries")) return kok
        }
        return null
    }

    private fun JSONObject.adlar(alan: String): List<String> {
        val dizi = optJSONArray(alan) ?: return optString(alan).takeIf { it.isNotBlank() }?.let { listOf(it) } ?: emptyList()
        return (0 until dizi.length()).mapNotNull { i ->
            dizi.optJSONObject(i)?.optString("name")?.takeIf { it.isNotBlank() }
                ?: dizi.optString(i).takeIf { it.isNotBlank() && !it.startsWith("{") }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val html     = app.get(url, headers = tarayici).text
        val document = org.jsoup.Jsoup.parse(html, url)
        val veri     = yapisalVeri(html)

        val oynatici = document.selectFirst("div#active-player")
        val title    = veri?.optString("name")?.ifBlank { null }
            ?: oynatici?.attr("data-film-title")?.ifBlank { null }
            ?: document.selectFirst("h1")?.text()?.trim()?.ifBlank { null }
            ?: return null

        val poster = fixUrlNull(veri?.optString("image")?.ifBlank { null }
            ?: document.selectFirst("meta[property=og:image]")?.attr("content"))

        val description = veri?.optString("description")?.ifBlank { null }
            ?: document.selectFirst("meta[name=description]")?.attr("content")
        val year = (veri?.optString("datePublished")?.take(4)
            ?: oynatici?.attr("data-film-year")?.take(4))?.toIntOrNull()
        val tags   = veri?.adlar("genre").orEmpty()
        val actors = veri?.adlar("actor").orEmpty().map { Actor(it) }
        val rating = veri?.optJSONObject("aggregateRating")?.optString("ratingValue")?.ifBlank { null }
            ?: oynatici?.attr("data-film-rating")?.ifBlank { null }
        val sure = oynatici?.attr("data-film-duration")?.toIntOrNull()?.takeIf { it > 0 }
        val fragman = document.selectFirst("iframe#trailerIframe")?.attr("data-src")?.ifBlank { null }

        val oneriler = document.select("div.film-card, div.card.film-card").mapNotNull { it.toSearchResult() }
            .filter { it.url.trimEnd('/') != url.trimEnd('/') }
            .distinctBy { it.url }

        val tur = if (url.contains("/dizi/")) TvType.TvSeries else TvType.Movie

        if (tur == TvType.Movie) {
            return newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl       = poster
                this.year            = year
                this.plot            = description
                this.tags            = tags
                this.score           = Score.from10(rating)
                this.duration        = sure
                this.recommendations = oneriler
                addActors(actors)
                addTrailer(fragman)
            }
        }

        // Dizi sayfaları bölümleri JS ile yükler; oynatıcı yine film_id üzerinden çalışır.
        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, emptyList()) {
            this.posterUrl       = poster
            this.year            = year
            this.plot            = description
            this.tags            = tags
            this.score           = Score.from10(rating)
            this.recommendations = oneriler
            addActors(actors)
            addTrailer(fragman)
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("JTF", "data » $data")
        val html     = app.get(data, headers = tarayici).text
        val document = org.jsoup.Jsoup.parse(html, data)

        val filmId = document.selectFirst("input[name=film_id]")?.attr("value")?.ifBlank { null } ?: return false
        Log.d("JTF", "filmId » $filmId")

        val kaynaklar = document.select("button.player-source-btn[data-source-index]")
            .mapNotNull { btn ->
                val index = btn.attr("data-source-index").toIntOrNull() ?: return@mapNotNull null
                val tur   = btn.attr("data-player-type").ifBlank { "dublaj" }
                Triple(index, tur, btn.text().trim())
            }
            .ifEmpty { listOf(Triple(0, "dublaj", "TR")) }
            .distinctBy { "${it.second}-${it.first}" }

        var bulundu = false
        for ((index, tur, etiket) in kaynaklar) {
            val yanit = runCatching {
                app.post(
                    "${mainUrl}/jetplayer",
                    headers = tarayici + mapOf("X-Requested-With" to "XMLHttpRequest"),
                    referer = data,
                    data    = mapOf(
                        "film_id"      to filmId,
                        "source_index" to index.toString(),
                        "player_type"  to tur
                    )
                ).text
            }.getOrNull() ?: continue

            val iframe = Regex("""<iframe[^>]+src=['"]([^'"]+)['"]""").find(yanit)?.groupValues?.get(1)
                ?.let { fixUrl(it) } ?: continue
            Log.d("JTF", "$etiket » $iframe")

            if (iframe.contains("playerx.")) {
                if (playerX(iframe, etiket, subtitleCallback, callback)) bulundu = true
            } else {
                bulundu = true
                runCatching { loadExtractor(iframe, data, subtitleCallback, callback) }
            }
        }

        return bulundu
    }

    /** Verilen konumdan başlayan JSON nesnesini süslü parantez eşleştirerek ayıklar. */
    private fun jsonGovde(metin: String, baslangic: Int): String? {
        var derinlik = 0
        var tirnak   = false
        var kacis    = false
        for (i in baslangic until metin.length) {
            val c = metin[i]
            if (kacis) { kacis = false; continue }
            if (c == '\\') { kacis = true; continue }
            if (c == '"') { tirnak = !tirnak; continue }
            if (tirnak) continue
            when (c) {
                '{' -> derinlik++
                '}' -> {
                    derinlik--
                    if (derinlik == 0) return metin.substring(baslangic, i + 1)
                }
            }
        }
        return null
    }

    /** playerx.info tipi JWPlayer sarmalayıcılarını çözer. */
    private suspend fun playerX(
        url: String,
        etiket: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val kok = Regex("""^(https?://[^/]+)""").find(url)?.groupValues?.get(1) ?: return false
        val sayfa = app.get(url, headers = tarayici, referer = "${mainUrl}/").text

        val bas = Regex("""var\s+initialPlayback\s*=\s*""").find(sayfa)?.range?.last?.plus(1) ?: return false
        val govde = jsonGovde(sayfa, bas) ?: return false
        val nesne = runCatching { JSONObject(govde) }.getOrNull() ?: return false

        val item = nesne.optJSONObject("item") ?: return false
        val referer = "$kok/"

        val izler = item.optJSONArray("tracks")
        if (izler != null) {
            for (i in 0 until izler.length()) {
                val iz = izler.optJSONObject(i) ?: continue
                val dosya = iz.optString("file").ifBlank { continue }
                subtitleCallback.invoke(
                    newSubtitleFile(
                        iz.optString("label").ifBlank { "Altyazı" },
                        if (dosya.startsWith("http")) dosya else "$referer${dosya.removePrefix("/")}"
                    ) {
                        this.headers = mapOf("Referer" to referer, "User-Agent" to tarayici["User-Agent"]!!)
                    }
                )
            }
        }

        val kaynaklar = item.optJSONArray("sources") ?: return false
        var bulundu = false
        for (i in 0 until kaynaklar.length()) {
            val kaynak = kaynaklar.optJSONObject(i) ?: continue
            val dosya  = kaynak.optString("file").ifBlank { continue }
            val akis   = if (dosya.startsWith("http")) dosya else "$referer${dosya.removePrefix("/")}"
            val tip    = kaynak.optString("type").lowercase()

            callback.invoke(
                newExtractorLink(
                    source = this.name,
                    name   = "${this.name} ($etiket)",
                    url    = akis,
                    type   = if (tip.contains("mp4")) ExtractorLinkType.VIDEO else ExtractorLinkType.M3U8
                ) {
                    this.referer = referer
                    this.quality = Qualities.Unknown.value
                    this.headers = mapOf("Referer" to referer, "User-Agent" to tarayici["User-Agent"]!!)
                }
            )
            bulundu = true
        }

        return bulundu
    }
}
