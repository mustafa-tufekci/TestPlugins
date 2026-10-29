package com.panates

import android.util.Log
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors

/**
 * kultfilmler.net 2026 sonunda temasını değiştirdi: liste kartları "a.mcard",
 * film sayfasındaki oynatıcı ise vidpapi.xyz (FirePlayer) iframe'i.
 */
class Ktf : MainAPI() {
    override var mainUrl              = "https://kultfilmler.net"
    override var name                 = "KultFilmler"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val supportedTypes       = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        "${mainUrl}/page/"                                      to "Son Filmler",
        "${mainUrl}/category/aile-filmleri-izle/page/"		    to "Aile",
        "${mainUrl}/category/aksiyon-filmleri-izle/page/"	    to "Aksiyon",
        "${mainUrl}/category/animasyon-filmleri-izle/page/"	    to "Animasyon",
        "${mainUrl}/category/belgesel-izle/page/"			    to "Belgesel",
        "${mainUrl}/category/bilim-kurgu-filmleri-izle/page/"   to "Bilim Kurgu",
        "${mainUrl}/category/biyografi-filmleri-izle/page/"	    to "Biyografi",
        "${mainUrl}/category/dram-filmleri-izle/page/"		    to "Dram",
        "${mainUrl}/category/fantastik-filmleri-izle/page/"	    to "Fantastik",
        "${mainUrl}/category/gerilim-filmleri-izle/page/"	    to "Gerilim",
        "${mainUrl}/category/gizem-filmleri-izle/page/"		    to "Gizem",
        "${mainUrl}/category/kara-filmleri-izle/page/"		    to "Kara",
        "${mainUrl}/category/kisa-film-izle/page/"			    to "Kısa Metrajlı",
        "${mainUrl}/category/komedi-filmleri-izle/page/"		to "Komedi",
        "${mainUrl}/category/korku-filmleri-izle/page/"		    to "Korku",
        "${mainUrl}/category/macera-filmleri-izle/page/"		to "Macera",
        "${mainUrl}/category/muzik-filmleri-izle/page/"		    to "Müzik",
        "${mainUrl}/category/polisiye-filmleri-izle/page/"	    to "Polisiye",
        "${mainUrl}/category/politik-filmleri-izle/page/"	    to "Politik",
        "${mainUrl}/category/romantik-filmleri-izle/page/"	    to "Romantik",
        "${mainUrl}/category/savas-filmleri-izle/page/"		    to "Savaş",
        "${mainUrl}/category/spor-filmleri-izle/page/"		    to "Spor",
        "${mainUrl}/category/suc-filmleri-izle/page/"		    to "Suç",
        "${mainUrl}/category/tarih-filmleri-izle/page/"		    to "Tarih",
        "${mainUrl}/category/yerli-filmleri-izle/page/"		    to "Yerli"
    )

    private val tarayici = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}$page/", headers = tarayici).document
        val home     = document.select("a.mcard").mapNotNull { it.toSearchResult() }.distinctBy { it.url }

        return newHomePageResponse(request.name, home, hasNext = home.isNotEmpty())
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val href      = fixUrlNull(this.attr("href")) ?: return null
        val title     = this.selectFirst("h3")?.text()?.trim()?.ifBlank { null }
            ?: this.selectFirst("img")?.attr("alt")?.ifBlank { null }
            ?: return null
        val posterUrl = KtfResim.kart(this)
        val year      = this.selectFirst("span.pyear")?.text()?.trim()?.toIntOrNull()
        val puan      = this.selectFirst("span.mscore b")?.text()?.trim()

        return if (href.contains("/dizi/")) {
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
                this.year      = year
                this.score     = Score.from10(puan)
            }
        } else {
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
                this.year      = year
                this.score     = Score.from10(puan)
            }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("${mainUrl}/", params = mapOf("s" to query), headers = tarayici).document

        return document.select("a.mcard").mapNotNull { it.toSearchResult() }.distinctBy { it.url }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    private fun yapisalVeri(html: String): JSONObject? {
        for (eslesme in Regex("""<script[^>]+application/ld\+json[^>]*>(.*?)</script>""", RegexOption.DOT_MATCHES_ALL).findAll(html)) {
            val kok = runCatching { JSONObject(eslesme.groupValues[1].trim()) }.getOrNull() ?: continue
            if (kok.optString("@type") in setOf("Movie", "TVSeries")) return kok
            val dugumler = kok.optJSONArray("@graph") ?: continue
            for (i in 0 until dugumler.length()) {
                val d = dugumler.optJSONObject(i) ?: continue
                if (d.optString("@type") in setOf("Movie", "TVSeries")) return d
            }
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
        val document = Jsoup.parse(html, url)
        val veri     = yapisalVeri(html)

        val title       = veri?.optString("name")?.ifBlank { null } ?: document.selectFirst("h1")?.text()?.trim() ?: return null
        val poster      = fixUrlNull(veri?.optString("image")?.ifBlank { null } ?: document.selectFirst("meta[property=og:image]")?.attr("content"))
        val description = veri?.optString("description")?.ifBlank { null }
        val year        = veri?.optString("datePublished")?.take(4)?.toIntOrNull()
        val tags        = veri?.adlar("genre").orEmpty()
        val actors      = veri?.adlar("actor").orEmpty().map { Actor(it) }
        val oneriler    = document.select("a.mcard").mapNotNull { it.toSearchResult() }
            .filter { it.url.trimEnd('/') != url.trimEnd('/') }
            .distinctBy { it.url }

        if (url.contains("/dizi/")) {
            return newTvSeriesLoadResponse(title, url, TvType.TvSeries, emptyList()) {
                this.posterUrl       = poster
                this.year            = year
                this.plot            = description
                this.tags            = tags
                this.recommendations = oneriler
                addActors(actors)
            }
        }

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl       = poster
            this.year            = year
            this.plot            = description
            this.tags            = tags
            this.recommendations = oneriler
            addActors(actors)
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("KLT", "data » $data")
        val document = app.get(data, headers = tarayici).document

        val iframes = mutableSetOf<String>()
        document.select("div#player iframe[src], iframe[src]").forEach { cerceve ->
            val adres = fixUrlNull(cerceve.attr("src")) ?: return@forEach
            if (adres.contains("youtube.com") || adres.contains("youtu.be")) return@forEach
            iframes.add(adres)
        }

        var bulundu = false
        for (iframe in iframes) {
            Log.d("KLT", "iframe » $iframe")
            if (KtfFirePlayer.uygunMu(iframe)) {
                bulundu = true
                runCatching { KtfFirePlayer().getUrl(iframe, "${mainUrl}/", subtitleCallback, callback) }
            } else if (iframe.contains("vidmoly")) {
                bulundu = true
                runCatching {
                    val headers  = mapOf(
                        "User-Agent"     to tarayici["User-Agent"]!!,
                        "Sec-Fetch-Dest" to "iframe"
                    )
                    val iSource = app.get(iframe, headers = headers, referer = "${mainUrl}/").text
                    val m3uLink = Regex("""file:"([^"]+)""").find(iSource)?.groupValues?.get(1) ?: return@runCatching

                    callback.invoke(
                        newExtractorLink(
                            source = "VidMoly",
                            name   = "VidMoly",
                            url    = m3uLink
                        ) {
                            this.referer = "https://vidmoly.to/"
                            this.quality = Qualities.Unknown.value
                        }
                    )
                }
            } else {
                bulundu = true
                runCatching { loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback) }
            }
        }

        return bulundu
    }
}
