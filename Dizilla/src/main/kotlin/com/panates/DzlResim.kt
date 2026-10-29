package com.panates

import org.jsoup.nodes.Element

/**
 * Resim adresi duzenleyici.
 *
 * Kaynaklarin cogu listede 16:9 kucuk kapak/onizleme gorselini ya da
 * WordPress'in kirpilmis kucuk varyantini veriyor; oynatici ise 2:3 afis
 * bekliyor. Bu yardimci en uygun gorseli secer ve adresi buyutur.
 *
 * Paket (`com.panates`) tum modullerde ortak oldugu icin sinif adi her
 * modulde farklidir; yardimci her modulun icine kopyalanir.
 */
object DzlResim {
    private val cdnBoyut = Regex("""/images/(?:0|360|640)/360/(?:85|90)/(?=[^/]+$)""")
    private val tmdbBoyut = Regex("""/t/p/(?:w\d+|h\d+)/""")
    private val wpKucuk = Regex("""-\d{2,4}x\d{2,4}(\.[A-Za-z0-9]{2,5})$""")
    private val srcsetAdet = Regex("""\s+(\d+)(?:w|x)$""")
    private val kotuAnahtar = listOf("logo", "brand", "platforms", "backdrop", "loader", "placeholder", "avatar")

    private fun dolu(deger: String?): String? =
        deger?.trim()?.takeIf { it.isNotBlank() && !it.startsWith("data:") }

    private fun srcsetEnBuyuk(deger: String?): String? {
        val ham = deger?.trim().orEmpty()
        if (ham.isBlank()) return null
        var enIyi: String? = null
        var enIyiGenislik = -1
        ham.split(',').forEach { parca ->
            val temiz = parca.trim()
            if (temiz.isBlank()) return@forEach
            val adres = temiz.substringBefore(' ').trim()
            if (adres.isBlank() || adres.startsWith("data:")) return@forEach
            val genislik = srcsetAdet.find(temiz.substringAfter(' ', ""))?.groupValues?.get(1)?.toIntOrNull() ?: 0
            if (genislik > enIyiGenislik) {
                enIyiGenislik = genislik
                enIyi = adres
            }
        }
        return enIyi
    }

    private fun puan(img: Element): Int {
        val metin = (img.attr("src") + " " + img.attr("data-src") + " " +
            img.attr("data-lazy-src") + " " + img.className() + " " + img.attr("alt")).lowercase()
        var p = 0
        if (listOf("poster", "afis", "kapak", "cover", "pimg").any { metin.contains(it) }) p += 3
        if (kotuAnahtar.any { metin.contains(it) }) p -= 5
        val genislik = img.attr("width").trim().toIntOrNull() ?: 0
        val yukseklik = img.attr("height").trim().toIntOrNull() ?: 0
        if (genislik > 0 && yukseklik > genislik) p += 2
        if (img.attr("src").startsWith("data:")) p -= 1
        if (dolu(img.attr("data-src")) != null) p += 1
        return p
    }

    /** Kapsayicidaki (kart, satir, detay blogu) en uygun gorsel adresini dondurur. */
    fun kart(kap: Element): String? {
        val adaylar = kap.select("img")
        if (adaylar.isEmpty()) return null
        val img = adaylar.maxByOrNull { puan(it) } ?: return null
        return buyuk(
            dolu(img.attr("data-src"))
                ?: dolu(img.attr("data-lazy-src"))
                ?: dolu(img.attr("data-original"))
                ?: srcsetEnBuyuk(img.attr("data-srcset"))
                ?: srcsetEnBuyuk(img.attr("srcset"))
                ?: dolu(img.attr("src"))
        )
    }

    /** Adresi mutlaklastirir ve bilinen kucuk varyantlari buyutur. */
    fun buyuk(ham: String?): String? {
        var adres = ham?.trim().orEmpty()
        if (adres.isBlank()) return null
        adres = adres.substringBefore('#').substringBefore(' ').trim()
        if (adres.isBlank() || adres.startsWith("data:")) return null
        adres = when {
            adres.startsWith("//") -> "https:$adres"
            adres.startsWith("http://") -> "https://" + adres.removePrefix("http://")
            else -> adres
        }
        if (!adres.startsWith("http")) return adres
        adres = adres.replace("images-macellan-online.cdn.ampproject.org/i/s/", "")
        adres = cdnBoyut.replace(adres, "/images/630/910/95/")
        adres = tmdbBoyut.replace(adres, "/t/p/w500/")
        if (adres.contains("/wp-content/uploads/")) adres = wpKucuk.replace(adres, "$1")
        if (adres.contains("/storage/tmdb/posters/w")) adres = Regex("/storage/tmdb/posters/w\\d+/").replace(adres, "/storage/tmdb/posters/w780/")
        if (adres.contains("/poster/film/")) adres = adres.replace("/poster/film/", "/poster/film-lg/")
        if (adres.contains("/images/list/poster/") && !adres.contains("@2x")) {
            adres = adres.replace(".webp", "@2x.webp").replace(".jpg", "@2x.jpg")
        }
        return adres
    }
}
