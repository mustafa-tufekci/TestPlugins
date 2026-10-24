package com.panates

object CanliTVChannels {
    val kanallar = listOf(
        // --- Spor Kanalları ---
        CanliKanal(
            id = "trt-spor",
            name = "TRT Spor",
            category = "spor",
            streamUrl = "https://tv-trtspor1.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_Spor_logo.svg?width=120",
            description = "TRT Spor Canlı Yayın (HD)"
        ),
        CanliKanal(
            id = "trt-spor-yildiz",
            name = "TRT Spor Yıldız",
            category = "spor",
            streamUrl = "https://trt.daioncdn.net/trtspor-yildiz/master.m3u8?app=clean",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_Spor_Y%C4%B1ld%C4%B1z_logo.svg?width=120",
            description = "TRT Spor Yıldız Canlı Yayın (HD)"
        ),
        CanliKanal(
            id = "trt-spor-2",
            name = "TRT Spor (Yedek)",
            category = "spor",
            streamUrl = "https://tv-trtspor2.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_Spor_logo.svg?width=120",
            description = "TRT Spor 2. Hat Canlı Yayın"
        ),

        // --- tabii Spor (UEFA Şampiyonlar & Avrupa Ligi) ---
        CanliKanal(
            id = "tabii-spor-1",
            name = "tabii Spor 1",
            category = "tabii",
            streamUrl = "https://Pz4qT7nW1mR9sH2vL5xK8jG3y.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_Spor_logo.svg?width=120",
            description = "tabii Spor 1 - UEFA Şampiyonlar / Avrupa Ligi Canlı Maç Yayını"
        ),
        CanliKanal(
            id = "tabii-spor-2",
            name = "tabii Spor 2",
            category = "tabii",
            streamUrl = "https://mR8bV4kL1nQ7sH9tW2xP5zJ6g.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_Spor_logo.svg?width=120",
            description = "tabii Spor 2 - UEFA Canlı Maç Yayını"
        ),
        CanliKanal(
            id = "tabii-spor-3",
            name = "tabii Spor 3",
            category = "tabii",
            streamUrl = "https://mR4vL7nQ2sH9tW5xP1zK3gJ8b.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_Spor_logo.svg?width=120",
            description = "tabii Spor 3 - UEFA Canlı Maç Yayını"
        ),
        CanliKanal(
            id = "tabii-spor-4",
            name = "tabii Spor 4",
            category = "tabii",
            streamUrl = "https://sH7tQ2nW5mR1vL9xP4zK3gJ8k.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_Spor_logo.svg?width=120",
            description = "tabii Spor 4 - UEFA Canlı Maç Yayını"
        ),
        CanliKanal(
            id = "tabii-spor-5",
            name = "tabii Spor 5",
            category = "tabii",
            streamUrl = "https://vL2nQ8sH4tW1mR7xP5zK9gJ3y.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_Spor_logo.svg?width=120",
            description = "tabii Spor 5 - UEFA Canlı Maç Yayını"
        ),
        CanliKanal(
            id = "tabii-spor-6",
            name = "tabii Spor 6",
            category = "tabii",
            streamUrl = "https://Pz2rT7nW1mR9sH4vL6xK3gJ5b.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_Spor_logo.svg?width=120",
            description = "tabii Spor 6 - UEFA Canlı Maç Yayını"
        ),

        // --- Ulusal Kanallar ---
        CanliKanal(
            id = "trt-1",
            name = "TRT 1",
            category = "ulusal",
            streamUrl = "https://tv-trt1.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_1_logo_2021.svg?width=120",
            description = "TRT 1 HD Canlı Yayın"
        ),
        CanliKanal(
            id = "tv8",
            name = "TV8",
            category = "ulusal",
            streamUrl = "https://tv8.daioncdn.net/tv8/tv8.m3u8?app=tv8_web&ce=3",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Tv8_logo.svg?width=120",
            description = "TV8 HD Canlı Yayın"
        ),
        CanliKanal(
            id = "show-tv",
            name = "Show TV",
            category = "ulusal",
            streamUrl = "https://ciner.daioncdn.net/showtv/showtv.m3u8?app=showtv_web&ce=3",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Show_TV_logo.svg?width=120",
            description = "Show TV HD Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-d",
            name = "Kanal D",
            category = "ulusal",
            streamUrl = "https://demiroren.daioncdn.net/kanald/kanald.m3u8?app=kanald_web&ce=3",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Kanal_D_logo.svg?width=120",
            description = "Kanal D HD Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-d-duhnet",
            name = "Kanal D (Yedek)",
            category = "ulusal",
            streamUrl = "https://kdlive.duhnet.tv/S2/HLS_LIVE/kanalddainp/playlist.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Kanal_D_logo.svg?width=120",
            description = "Kanal D Yedek Canlı Yayın"
        ),
        CanliKanal(
            id = "now-tv",
            name = "NOW TV",
            category = "ulusal",
            streamUrl = "https://nowtv.daioncdn.net/nowtv/nowtv.m3u8?ce=3&app=nowtv_web",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/NOW_logo_2024.svg?width=120",
            description = "NOW TV HD Canlı Yayın",
            dynamicResolver = "nowtv"
        ),

        // --- Haber & Belgesel ---
        CanliKanal(
            id = "trt-haber",
            name = "TRT Haber",
            category = "haber",
            streamUrl = "https://tv-trthaber.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_Haber_logo_2020.svg?width=120",
            description = "TRT Haber HD Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-belgesel",
            name = "TRT Belgesel",
            category = "haber",
            streamUrl = "https://tv-trtbelgesel.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_Belgesel_logo_2019.svg?width=120",
            description = "TRT Belgesel HD Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-2",
            name = "TRT 2 (Kültür & Sanat)",
            category = "haber",
            streamUrl = "https://tv-trt2.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_2_logo_2019.svg?width=120",
            description = "TRT 2 Kültür Sanat Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-world",
            name = "TRT World",
            category = "haber",
            streamUrl = "https://tv-trtworld.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_World_logo_2015.svg?width=120",
            description = "TRT World English News"
        ),
        CanliKanal(
            id = "trt-cocuk",
            name = "TRT Çocuk",
            category = "haber",
            streamUrl = "https://tv-trtcocuk.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_%C3%87ocuk_logo.svg?width=120",
            description = "TRT Çocuk HD Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-muzik",
            name = "TRT Müzik",
            category = "haber",
            streamUrl = "https://tv-trtmuzik.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/TRT_M%C3%BCzik_logo_2020.svg?width=120",
            description = "TRT Müzik HD Canlı Yayın"
        ),

        // --- Radyo & Müzik ---
        CanliKanal(
            id = "kral-fm",
            name = "Kral FM",
            category = "radyo",
            streamUrl = "https://dygedge.radyotvonline.net/kralfm/playlist.m3u8",
            posterUrl = "https://upload.wikimedia.org/wikipedia/tr/9/91/Kral_FM_logosu.png",
            description = "Kral FM Canlı Radyo Yayını"
        ),
        CanliKanal(
            id = "power-turk",
            name = "Power Türk",
            category = "radyo",
            streamUrl = "https://listen.powerapp.com.tr/powerturk/abr/playlist.m3u8",
            posterUrl = "https://upload.wikimedia.org/wikipedia/tr/thumb/8/87/Powert%C3%BCrk_TV_logosu.png/512px-Powert%C3%BCrk_TV_logosu.png",
            description = "Power Türk Canlı Müzik Yayını"
        )
    )

    fun findById(id: String): CanliKanal? = kanallar.firstOrNull { it.id == id }

    fun findByName(name: String): CanliKanal? = kanallar.firstOrNull {
        it.name.equals(name, ignoreCase = true) || it.name.contains(name, ignoreCase = true)
    }
}
