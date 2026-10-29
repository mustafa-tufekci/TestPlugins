package com.panates

import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.Score
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrlNull
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.newTvSeriesSearchResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.nodes.Element

/**
 * DiziYou - diziyou.net (WordPress/Wstream temasi, 2026).
 *
 * - Arsiv: /diziler/page/N, /filmler/page/N, /tur/<tur>/page/N -> article.card kartlari
 * - Arama: /?s=<sorgu> -> ayni kartlar
 * - Detay: /dizi/<slug>/ veya /film/<slug>/ -> article.hro (hro__poster, hro__caption h2, hro__meta,
 *   hro__genres, hro__overview) + bolum kartlari (article.card--episode)
 * - Bolumler ayrica REST ile tamamlanir: /wp-json/wp/v2/episodes?search=<dizi>&per_page=100
 * - Oynatma: bolum sayfasindaki iframe (ksdpictures.site/dzembed.php?id=... veya
 *   yabancidizim.com/rplayer/...) -> window.NP_CONFIG {sources[].master, subtitles[].url}
 */
class Dyu : MainAPI() {
    override var mainUrl = "https://diziyou.net"
    override var name = "DiziYou"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = false
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    private val tarayici = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    )

    override val mainPage = mainPageOf(
        "$mainUrl/diziler" to "Diziler",
        "$mainUrl/filmler" to "Filmler",
        "$mainUrl/tur/asya-dizileri" to "Asya Dizileri",
        "$mainUrl/tur/bilim-kurgu" to "Bilim Kurgu",
        "$mainUrl/tur/fantastik" to "Fantastik",
        "$mainUrl/tur/gerilim" to "Gerilim",
        "$mainUrl/tur/korku" to "Korku",
    )

    private fun sayfaAdresi(data: String, page: Int): String =
        if (page <= 1) "$data/" else "$data/page/$page/"

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(sayfaAdresi(request.data, page), headers = tarayici).document
        val sonuc = document.select("article.card")
            .mapNotNull { it.toSearchResult() }
            .distinctBy { it.url }

        return newHomePageResponse(request.name, sonuc, hasNext = sonuc.isNotEmpty())
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val link = this.selectFirst("a[href]") ?: return null
        val href = fixUrlNull(link.attr("href")) ?: return null
        val filmMi = href.contains("/film/")
        if (!filmMi && !href.contains("/dizi/")) return null

        val baslik = this.selectFirst("h3")?.text()?.trim()
            ?: link.attr("title").substringBefore(" izle").trim().takeIf { it.isNotBlank() }
            ?: return null
        val poster = this.selectFirst("img")?.let { el -> el.attr("src").ifBlank { el.attr("data-src") } }
            ?.let { fixUrlNull(it) }
        val puan = Regex("""(\d+(?:[.,]\d+)?)""")
            .find(this.selectFirst(".tmdb")?.text().orEmpty())
            ?.groupValues?.get(1)

        return if (filmMi) {
            newMovieSearchResponse(baslik, href, TvType.Movie) {
                this.posterUrl = poster
                this.score = Score.from10(puan)
            }
        } else {
            newTvSeriesSearchResponse(baslik, href, TvType.TvSeries) {
                this.posterUrl = poster
                this.score = Score.from10(puan)
            }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/", headers = tarayici, params = mapOf("s" to query)).document
        return document.select("article.card")
            .mapNotNull { it.toSearchResult() }
            .distinctBy { it.url }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    private suspend fun bolumler(document: org.jsoup.nodes.Document, baslik: String): List<Episode> {
        val sayfadan = document.select("article.card--episode").mapNotNull { it.toEpisode() }

        // Wstream temasi ilk 15 bolumu basar; geri kalani REST uzerinden tamamlanir
        val restten = runCatching {
            val metin = app.get(
                "$mainUrl/wp-json/wp/v2/episodes",
                headers = tarayici,
                params = mapOf("search" to baslik, "per_page" to "100", "orderby" to "date", "order" to "asc")
            ).text
            val dizi = JSONArray(metin)
            (0 until dizi.length()).mapNotNull { i ->
                val oge = dizi.optJSONObject(i) ?: return@mapNotNull null
                val ad = oge.optJSONObject("title")?.optString("rendered")?.trim().orEmpty()
                val href = oge.optString("link").trim()
                if (href.isBlank()) return@mapNotNull null
                val (sezon, bolum) = sezonBolum(ad, href) ?: return@mapNotNull null
                newEpisode(href) {
                    this.name = ad.ifBlank { "$sezon. Sezon $bolum. Bölüm" }
                    this.season = sezon
                    this.episode = bolum
                }
            }
        }.getOrDefault(emptyList())

        return (sayfadan + restten)
            .distinctBy { it.data }
            .sortedWith(compareBy({ it.season ?: 0 }, { it.episode ?: 0 }))
    }

    private fun sezonBolum(ad: String, href: String): Pair<Int, Int>? {
        Regex("""(\d+)\.?\s*Sezon\s*(\d+)\.?\s*Bölüm""").find(ad)?.let {
            val sezon = it.groupValues[1].toIntOrNull()
            val bolum = it.groupValues[2].toIntOrNull()
            if (sezon != null && bolum != null) return sezon to bolum
        }
        Regex("""-(\d+)-sezon-(\d+)-bolum""").find(href)?.let {
            val sezon = it.groupValues[1].toIntOrNull()
            val bolum = it.groupValues[2].toIntOrNull()
            if (sezon != null && bolum != null) return sezon to bolum
        }
        return null
    }

    private fun Element.toEpisode(): Episode? {
        val href = fixUrlNull(this.selectFirst("a[href*='/bolum/']")?.attr("href")) ?: return null
        val etiket = this.selectFirst("span.eps")?.text()?.trim()
            ?.replace(Regex("\\s+"), " ")
            .orEmpty()
        val (sezon, bolum) = sezonBolum(etiket, href) ?: return null
        val diziAdi = this.selectFirst("h3")?.text()?.trim().orEmpty()

        return newEpisode(href) {
            this.name = "$diziAdi $sezon. Sezon $bolum. Bölüm".trim()
            this.season = sezon
            this.episode = bolum
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url, headers = tarayici).document

        val baslik = document.selectFirst("article.hro .hro__caption h2")?.text()?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: document.selectFirst("article.hro h1")?.text()?.trim()?.substringBefore(" izle")?.trim()
            ?: return null
        val poster = fixUrlNull(document.selectFirst("article.hro .hro__poster img")?.attr("src"))
        val aciklama = document.selectFirst("article.hro .hro__overview")?.text()?.trim()
        val yil = document.selectFirst("article.hro .hro__caption span")?.text()?.trim()?.toIntOrNull()
        val puan = Regex("""(\d+(?:[.,]\d+)?)""")
            .find(document.selectFirst("article.hro .hro__meta .tmdb")?.text().orEmpty())
            ?.groupValues?.get(1)
        val etiketler = document.select("article.hro .hro__genres li a").map { it.text().trim() }
        val filmMi = url.contains("/film/")

        val oneriler = document.select("article.card")
            .mapNotNull { it.toSearchResult() }
            .filter { it.url != url }
            .distinctBy { it.url }

        if (filmMi) {
            return newMovieLoadResponse(baslik, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = aciklama
                this.year = yil
                this.tags = etiketler
                this.score = Score.from10(puan)
                this.recommendations = oneriler.take(24)
            }
        }

        return newTvSeriesLoadResponse(baslik, url, TvType.TvSeries, bolumler(document, baslik)) {
            this.posterUrl = poster
            this.plot = aciklama
            this.year = yil
            this.tags = etiketler
            this.score = Score.from10(puan)
            this.recommendations = oneriler.take(24)
        }
    }

    private fun originOf(url: String): String {
        val parca = url.substringAfter("://", "")
        val host = parca.substringBefore("/").substringBefore("?")
        return if (host.isBlank()) mainUrl else "https://$host"
    }

    private fun npConfig(html: String): JSONObject? {
        val anahtar = html.indexOf("NP_CONFIG")
        if (anahtar < 0) return null
        val bas = html.indexOf('{', anahtar)
        if (bas < 0) return null

        var derinlik = 0
        var metinIcinde = false
        var kacis = false
        for (i in bas until html.length) {
            val c = html[i]
            if (metinIcinde) {
                when {
                    kacis -> kacis = false
                    c == '\\' -> kacis = true
                    c == '"' -> metinIcinde = false
                }
                continue
            }
            when (c) {
                '"' -> metinIcinde = true
                '{' -> derinlik++
                '}' -> {
                    derinlik--
                    if (derinlik == 0) {
                        return runCatching { JSONObject(html.substring(bas, i + 1)) }.getOrNull()
                    }
                }
            }
        }
        return null
    }

    private suspend fun npOynat(
        iframeUrl: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val oynaticiOrigin = originOf(iframeUrl)
        val basliklar = tarayici + mapOf("Referer" to "$oynaticiOrigin/")
        val html = app.get(iframeUrl, headers = basliklar, referer = "$oynaticiOrigin/").text
        val veri = npConfig(html) ?: return false

        var bulundu = false
        veri.optJSONArray("subtitles")?.let { altyazilar ->
            for (i in 0 until altyazilar.length()) {
                val alt = altyazilar.optJSONObject(i) ?: continue
                val adres = alt.optString("url").ifBlank { alt.optString("file") }
                if (adres.isBlank()) continue
                val etiket = alt.optString("label").ifBlank { alt.optString("lang") }.ifBlank { "Altyazı" }
                subtitleCallback(SubtitleFile(etiket, adres).apply { headers = basliklar })
            }
        }

        val gorulen = mutableSetOf<String>()
        veri.optJSONArray("sources")?.let { kaynaklar ->
            for (i in 0 until kaynaklar.length()) {
                val kaynak = kaynaklar.optJSONObject(i) ?: continue
                val adres = kaynak.optString("master").ifBlank { kaynak.optString("file") }
                if (adres.isBlank() || !gorulen.add(adres)) continue
                val ad = kaynak.optString("title").ifBlank { name }
                callback(
                    newExtractorLink(
                        source = name,
                        name = "$name ($ad)",
                        url = adres,
                        type = if (adres.contains(".mp4")) ExtractorLinkType.VIDEO else ExtractorLinkType.M3U8
                    ) {
                        this.referer = "$oynaticiOrigin/"
                        this.quality = Qualities.P1080.value
                        this.headers = basliklar
                    }
                )
                bulundu = true
            }
        }

        return bulundu
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data, headers = tarayici).document
        var bulundu = false

        for (iframe in document.select("iframe[src], iframe[data-src]")) {
            val ham = iframe.attr("src").ifBlank { iframe.attr("data-src") }
            val adres = fixUrlNull(ham) ?: continue
            if (!adres.startsWith("http")) continue
            if (adres.contains("youtube.", true) || adres.contains("youtu.be", true)) continue

            val basarili = runCatching {
                if (npOynat(adres, subtitleCallback, callback)) {
                    true
                } else {
                    loadExtractor(adres, "$mainUrl/", subtitleCallback, callback)
                }
            }.getOrDefault(false)
            if (basarili) bulundu = true
        }

        return bulundu
    }
}
