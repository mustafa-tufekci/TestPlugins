package com.panates

import android.util.Base64
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrlNull
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newAnimeSearchResponse
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.Score
import org.json.JSONObject
import org.jsoup.nodes.Element

/**
 * turkanime.live (eski turkanime.co) - Tailwind temalı güncel Turk Anime TV.
 *
 * - Liste: /animeler?tab=all&genre=<tur>&page=N  -> a[href*=/dizi/] kartları (h3 baslik, img afis)
 * - Arama: /api/search.php?q=<sorgu> -> {"results":[{"title","type","slug","poster"}]}
 * - Detay: /dizi/<slug> -> h1, og:image, ld+json (TVSeries), bolum listesi a[href*=/bolum/]
 * - Bolum: /bolum/<slug>-N-sezon-M-bolum -> Base64 kodlanmış <iframe> (streamcorecdn.com)
 */
class Tka : MainAPI() {
    override var mainUrl = "https://turkanime.live"
    override var name = "TurkAnime"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = false
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie)

    private val tarayici = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    )

    override val mainPage = mainPageOf(
        "${mainUrl}/animeler?tab=all" to "Tüm Animeler",
        "${mainUrl}/animeler?tab=all&sort=rating" to "En Yüksek Puan",
        "${mainUrl}/animeler?tab=all&translation_type=subtitled" to "Türkçe Altyazı",
        "${mainUrl}/animeler?tab=all&translation_type=dubbed" to "Dublaj",
        "${mainUrl}/animeler?tab=all&genre=aksiyon" to "Aksiyon",
        "${mainUrl}/animeler?tab=all&genre=aksiyon-macera" to "Aksiyon & Macera",
        "${mainUrl}/animeler?tab=all&genre=bilim-kurgu-fantazi" to "Bilim Kurgu & Fantazi",
        "${mainUrl}/animeler?tab=all&genre=dram" to "Dram",
        "${mainUrl}/animeler?tab=all&genre=fantastik" to "Fantastik",
        "${mainUrl}/animeler?tab=all&genre=gizem" to "Gizem",
        "${mainUrl}/animeler?tab=all&genre=komedi" to "Komedi",
        "${mainUrl}/animeler?tab=all&genre=macera" to "Macera",
        "${mainUrl}/animeler?tab=all&genre=romantik" to "Romantik",
        "${mainUrl}/animeler?tab=all&genre=savas-politik" to "Savaş & Politik",
        "${mainUrl}/animeler?tab=all&genre=suc" to "Suç",
    )

    private fun sayfaAdresi(data: String, page: Int): String =
        if (page <= 1) data else "$data&page=$page"

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(sayfaAdresi(request.data, page), headers = tarayici).document
        val sonuc = document.select("a[href*='/dizi/'], a[href*='/film/']")
            .mapNotNull { it.toSearchResult() }
            .distinctBy { it.url }

        return newHomePageResponse(request.name, sonuc, hasNext = sonuc.isNotEmpty())
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val href = fixUrlNull(this.attr("href")) ?: return null
        if (!href.contains("/dizi/") && !href.contains("/film/")) return null

        val posterEl = this.selectFirst("img")
        val baslik = this.selectFirst("h3")?.text()?.trim()?.takeIf { it.isNotBlank() }
            ?: posterEl?.attr("alt")?.substringBefore(" izle")?.trim()?.takeIf { it.isNotBlank() }
            ?: return null
        val poster = posterEl?.let { el -> el.attr("src").ifBlank { el.attr("data-src") } }
            ?.let { fixUrlNull(it) }
        val puan = Regex("""(\d+(?:[.,]\d+)?)""")
            .find(this.selectFirst("span.text-primary")?.text().orEmpty())
            ?.groupValues?.get(1)
        val tur = if (href.contains("/film/")) TvType.AnimeMovie else TvType.Anime

        return newAnimeSearchResponse(baslik, href, tur) {
            this.posterUrl = poster
            this.score = Score.from10(puan)
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val metin = app.get(
            "$mainUrl/api/search.php",
            params = mapOf("q" to query),
            headers = tarayici
        ).text

        return runCatching {
            val dizi = JSONObject(metin).optJSONArray("results") ?: return emptyList()
            (0 until dizi.length()).mapNotNull { i ->
                val oge = dizi.optJSONObject(i) ?: return@mapNotNull null
                val baslik = oge.optString("title").trim().ifBlank { return@mapNotNull null }
                val slug = oge.optString("slug").trim().ifBlank { return@mapNotNull null }
                val tip = oge.optString("type")
                val href = if (tip.contains("movie") || tip.contains("film")) {
                    "$mainUrl/film/$slug"
                } else {
                    "$mainUrl/dizi/$slug"
                }
                newAnimeSearchResponse(
                    baslik,
                    href,
                    if (tip.contains("movie") || tip.contains("film")) TvType.AnimeMovie else TvType.Anime
                ) {
                    this.posterUrl = fixUrlNull(oge.optString("poster"))
                }
            }
        }.getOrDefault(emptyList())
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    private fun yapisalVeri(html: String): JSONObject? {
        val bloklar = Regex(
            """<script[^>]+type=["']application/ld\+json["'][^>]*>(.*?)</script>""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
        ).findAll(html).map { it.groupValues[1].trim() }.toList()

        for (blok in bloklar) {
            val kok = runCatching { JSONObject(blok) }.getOrNull() ?: continue
            val adaylar = mutableListOf<JSONObject>()
            adaylar.add(kok)
            kok.optJSONArray("@graph")?.let { graf ->
                for (i in 0 until graf.length()) graf.optJSONObject(i)?.let { adaylar.add(it) }
            }
            for (aday in adaylar) {
                val tip = aday.opt("@type")?.toString().orEmpty()
                if (tip.contains("TVSeries", true) || tip.contains("Movie", true) || tip.contains("Anime", true)) {
                    return aday
                }
            }
        }
        return null
    }

    private fun adlar(deger: Any?): List<String> = when (deger) {
        null -> emptyList()
        is String -> listOf(deger)
        is org.json.JSONArray -> (0 until deger.length()).mapNotNull { i ->
            val oge = deger.opt(i)
            when (oge) {
                is String -> oge
                is JSONObject -> oge.optString("name").ifBlank { null }
                else -> null
            }
        }
        is JSONObject -> listOfNotNull(deger.optString("name").ifBlank { null })
        else -> emptyList()
    }

    override suspend fun load(url: String): LoadResponse? {
        val html = app.get(url, headers = tarayici).text
        val document = org.jsoup.Jsoup.parse(html, url)
        val veri = yapisalVeri(html)

        val baslik = veri?.optString("name")?.trim()?.takeIf { it.isNotBlank() }
            ?: document.selectFirst("h1")?.text()?.trim()?.takeIf { it.isNotBlank() }
            ?: document.selectFirst("meta[property=og:title]")?.attr("content")
                ?.substringBefore(" izle")?.trim()
            ?: return null

        val poster = (veri?.opt("image")?.let { adlar(it).firstOrNull() })
            ?: document.selectFirst("meta[property=og:image]")?.attr("content")
        val aciklama = veri?.optString("description")?.trim()?.takeIf { it.isNotBlank() }
            ?: document.selectFirst("meta[name=description]")?.attr("content")?.trim()
        val yil = veri?.optString("datePublished")?.take(4)?.toIntOrNull()
        val etiketler = adlar(veri?.opt("genre"))
        val puan = veri?.optJSONObject("aggregateRating")?.optString("ratingValue")
        val tur = when {
            veri?.opt("@type")?.toString()?.contains("Movie", true) == true || url.contains("/film/") ->
                TvType.AnimeMovie
            else -> TvType.Anime
        }

        val bolumler = document.select("a[href*='/bolum/']")
            .mapNotNull { it.toEpisode(url) }
            .distinctBy { it.data }
            .sortedWith(compareBy({ it.season ?: 0 }, { it.episode ?: 0 }))

        val oneriler = document.select("a[href*='/dizi/'], a[href*='/film/']")
            .mapNotNull { it.toSearchResult() }
            .filter { !it.url.contains(url.substringAfter(mainUrl, "").trimEnd('/')) || it.url != url }
            .distinctBy { it.url }

        return newTvSeriesLoadResponse(baslik, url, tur, bolumler) {
            this.posterUrl = fixUrlNull(poster)
            this.plot = aciklama
            this.year = yil
            this.tags = etiketler
            this.score = Score.from10(puan)
            this.recommendations = oneriler.take(24)
        }
    }

    private fun Element.toEpisode(sayfaUrl: String): Episode? {
        val href = fixUrlNull(this.attr("href")) ?: return null
        if (!href.contains("/bolum/")) return null

        val hamEtiket = this.selectFirst("h4")?.text()?.trim().orEmpty()
        val eslesme = Regex("""(\d+)\.\s*Sezon\s*(\d+)\.\s*Bölüm""").find(hamEtiket)
        val slugEslesme = Regex("""-(\d+)-sezon-(\d+)-bolum""").find(href)

        val sezon = eslesme?.groupValues?.get(1)?.toIntOrNull()
            ?: slugEslesme?.groupValues?.get(1)?.toIntOrNull()
            ?: 1
        val bolum = eslesme?.groupValues?.get(2)?.toIntOrNull()
            ?: slugEslesme?.groupValues?.get(2)?.toIntOrNull()
            ?: return null

        val ad = hamEtiket.ifBlank { "$sezon. Sezon $bolum. Bölüm" }

        return newEpisode(href) {
            this.name = ad
            this.season = sezon
            this.episode = bolum
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val html = app.get(data, headers = tarayici).text
        var bulundu = false

        // Oynatıcı iframe'i Base64 kodlu olarak gömülüyor
        val kodlu = Regex("""const\s+encodedContent\s*=\s*['"]([^'"]+)['"]""")
            .find(html)?.groupValues?.get(1)
        val cozulmus = if (kodlu != null) {
            runCatching { String(Base64.decode(kodlu, Base64.DEFAULT)) }.getOrNull()
        } else {
            null
        }

        val adaylar = mutableListOf<String>()
        if (cozulmus != null) {
            Regex("""<iframe[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE).findAll(cozulmus)
                .forEach { adaylar.add(it.groupValues[1]) }
        }
        if (adaylar.isEmpty()) {
            Regex("""<iframe[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE).findAll(html)
                .forEach { adaylar.add(it.groupValues[1]) }
        }

        for (ham in adaylar.distinct()) {
            val iframe = fixUrlNull(ham) ?: continue
            if (!iframe.startsWith("http")) continue
            val basarili = runCatching {
                if (TkaStreamCore.uygunMu(iframe)) {
                    TkaStreamCore().getUrl(iframe, "$mainUrl/", subtitleCallback, callback)
                    true
                } else {
                    loadExtractor(iframe, "$mainUrl/", subtitleCallback, callback)
                }
            }.getOrDefault(false)
            if (basarili) bulundu = true
        }

        return bulundu
    }
}
