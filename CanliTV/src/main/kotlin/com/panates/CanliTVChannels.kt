package com.panates

object CanliTVChannels {
    val kanallar = listOf(
        // --- Spor Kanalları ---
        CanliKanal(
            id = "trt-spor",
            name = "TRT Spor",
            category = "spor",
            streamUrl = "https://tv-trtspor1.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/TRT_Spor_logo.svg/120px-TRT_Spor_logo.svg.png",
            description = "TRT Spor Canlı Yayın (HD)"
        ),
        CanliKanal(
            id = "trt-spor-yildiz",
            name = "TRT Spor Yıldız",
            category = "spor",
            streamUrl = "https://trt.daioncdn.net/trtspor-yildiz/master.m3u8?app=clean",
            posterUrl = "https://thumb.wikimedia.org/wikipedia/commons/thumb/c/ce/TRT_Spor_Y%C4%B1ld%C4%B1z_Logo.svg/120px-TRT_Spor_Y%C4%B1ld%C4%B1z_Logo.svg.png",
            description = "TRT Spor Yıldız Canlı Yayın (HD)"
        ),
        CanliKanal(
            id = "trt-spor-2",
            name = "TRT Spor (Yedek)",
            category = "spor",
            streamUrl = "https://tv-trtspor2.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/TRT_Spor_logo.svg/120px-TRT_Spor_logo.svg.png",
            description = "TRT Spor 2. Hat Canlı Yayın"
        ),

        // --- tabii Spor (UEFA Şampiyonlar & Avrupa Ligi) ---
        CanliKanal(
            id = "tabii-spor-1",
            name = "tabii Spor 1",
            category = "tabii",
            streamUrl = "https://Pz4qT7nW1mR9sH2vL5xK8jG3y.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/TRT_Spor_logo.svg/120px-TRT_Spor_logo.svg.png",
            description = "tabii Spor 1 - UEFA Şampiyonlar / Avrupa Ligi Canlı Maç Yayını"
        ),
        CanliKanal(
            id = "tabii-spor-2",
            name = "tabii Spor 2",
            category = "tabii",
            streamUrl = "https://mR8bV4kL1nQ7sH9tW2xP5zJ6g.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/TRT_Spor_logo.svg/120px-TRT_Spor_logo.svg.png",
            description = "tabii Spor 2 - UEFA Canlı Maç Yayını"
        ),
        CanliKanal(
            id = "tabii-spor-3",
            name = "tabii Spor 3",
            category = "tabii",
            streamUrl = "https://mR4vL7nQ2sH9tW5xP1zK3gJ8b.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/TRT_Spor_logo.svg/120px-TRT_Spor_logo.svg.png",
            description = "tabii Spor 3 - UEFA Canlı Maç Yayını"
        ),
        CanliKanal(
            id = "tabii-spor-4",
            name = "tabii Spor 4",
            category = "tabii",
            streamUrl = "https://sH7tQ2nW5mR1vL9xP4zK3gJ8k.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/TRT_Spor_logo.svg/120px-TRT_Spor_logo.svg.png",
            description = "tabii Spor 4 - UEFA Canlı Maç Yayını"
        ),
        CanliKanal(
            id = "tabii-spor-5",
            name = "tabii Spor 5",
            category = "tabii",
            streamUrl = "https://vL2nQ8sH4tW1mR7xP5zK9gJ3y.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/TRT_Spor_logo.svg/120px-TRT_Spor_logo.svg.png",
            description = "tabii Spor 5 - UEFA Canlı Maç Yayını"
        ),
        CanliKanal(
            id = "tabii-spor-6",
            name = "tabii Spor 6",
            category = "tabii",
            streamUrl = "https://Pz2rT7nW1mR9sH4vL6xK3gJ5b.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/TRT_Spor_logo.svg/120px-TRT_Spor_logo.svg.png",
            description = "tabii Spor 6 - UEFA Canlı Maç Yayını"
        ),

        // --- Ulusal Kanallar ---
        CanliKanal(
            id = "trt-1",
            name = "TRT 1",
            category = "ulusal",
            streamUrl = "https://tv-trt1.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=trt1.com.tr&sz=128",
            description = "TRT 1 HD Canlı Yayın"
        ),
        CanliKanal(
            id = "tv8",
            name = "TV8",
            category = "ulusal",
            streamUrl = "https://tv8.daioncdn.net/tv8/tv8.m3u8?app=tv8_web&ce=3",
            posterUrl = "https://www.google.com/s2/favicons?domain=trt1.com.tr&sz=128",
            description = "TV8 HD Canlı Yayın"
        ),
        CanliKanal(
            id = "show-tv",
            name = "Show TV",
            category = "ulusal",
            streamUrl = "https://ciner.daioncdn.net/showtv/showtv.m3u8?app=showtv_web&ce=3",
            posterUrl = "https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cb/Show_TV_logo.svg/120px-Show_TV_logo.svg.png",
            description = "Show TV HD Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-d",
            name = "Kanal D",
            category = "ulusal",
            streamUrl = "https://demiroren.daioncdn.net/kanald/kanald.m3u8?app=kanald_web&ce=3",
            posterUrl = "https://www.google.com/s2/favicons?domain=kanald.com.tr&sz=128",
            description = "Kanal D HD Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-d-duhnet",
            name = "Kanal D (Yedek)",
            category = "ulusal",
            streamUrl = "https://kdlive.duhnet.tv/S2/HLS_LIVE/kanalddainp/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=kanald.com.tr&sz=128",
            description = "Kanal D Yedek Canlı Yayın"
        ),
        CanliKanal(
            id = "now-tv",
            name = "NOW TV",
            category = "ulusal",
            streamUrl = "https://nowtv.daioncdn.net/nowtv/nowtv.m3u8?ce=3&app=nowtv_web",
            posterUrl = "https://www.google.com/s2/favicons?domain=nowtv.com.tr&sz=128",
            description = "NOW TV HD Canlı Yayın",
            dynamicResolver = "nowtv"
        ),

        // --- Haber & Belgesel ---
        CanliKanal(
            id = "trt-haber",
            name = "TRT Haber",
            category = "haber",
            streamUrl = "https://tv-trthaber.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=trthaber.com&sz=128",
            description = "TRT Haber HD Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-belgesel",
            name = "TRT Belgesel",
            category = "haber",
            streamUrl = "https://tv-trtbelgesel.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=trtbelgesel.com.tr&sz=128",
            description = "TRT Belgesel HD Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-2",
            name = "TRT 2 (Kültür & Sanat)",
            category = "haber",
            streamUrl = "https://tv-trt2.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=trt2.com.tr&sz=128",
            description = "TRT 2 Kültür Sanat Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-world",
            name = "TRT World",
            category = "haber",
            streamUrl = "https://tv-trtworld.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=trtworld.com&sz=128",
            description = "TRT World English News"
        ),
        CanliKanal(
            id = "trt-cocuk",
            name = "TRT Çocuk",
            category = "haber",
            streamUrl = "https://tv-trtcocuk.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=trtcocuk.com.tr&sz=128",
            description = "TRT Çocuk HD Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-muzik",
            name = "TRT Müzik",
            category = "haber",
            streamUrl = "https://tv-trtmuzik.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=trtmuzik.com.tr&sz=128",
            description = "TRT Müzik HD Canlı Yayın"
        ),

        // --- Radyo & Müzik ---
        CanliKanal(
            id = "kral-fm",
            name = "Kral FM",
            category = "radyo",
            streamUrl = "https://dygedge.radyotvonline.net/kralfm/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=kralfm.com.tr&sz=128",
            description = "Kral FM Canlı Radyo Yayını"
        ),
        CanliKanal(
            id = "power-turk",
            name = "Power Türk",
            category = "radyo",
            streamUrl = "https://listen.powerapp.com.tr/powerturk/abr/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=powerturk.com.tr&sz=128",
            description = "Power Türk Canlı Müzik Yayını"
        ),

        // --- iptv-org kaynagindan eklenen kanallar ---
        CanliKanal(
            id = "aksu-tv",
            name = "Aksu TV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_aksutv/aksutv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Aksu TV Canlı Yayın"
        ),
        CanliKanal(
            id = "anadolu-net-tv",
            name = "Anadolu Net TV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_anadolunet/anadolunet/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Anadolu Net TV Canlı Yayın"
        ),
        CanliKanal(
            id = "as-tv",
            name = "AS TV",
            category = "haber",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_astv/astv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "AS TV Canlı Yayın"
        ),
        CanliKanal(
            id = "brtv",
            name = "BRTV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_brtv/brtv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "BRTV Canlı Yayın"
        ),
        CanliKanal(
            id = "bric-ve-satranc-tv",
            name = "Bric ve Satranc TV",
            category = "ulusal",
            streamUrl = "http://139.162.182.79/live/test/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=139.162.182.79&sz=128",
            description = "Bric ve Satranc TV Canlı Yayın"
        ),
        CanliKanal(
            id = "ciftci-tv",
            name = "Ciftci TV",
            category = "kultur",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_ciftcitv/ciftcitv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Ciftci TV Canlı Yayın"
        ),
        CanliKanal(
            id = "a2tv",
            name = "A2TV",
            category = "ulusal",
            streamUrl = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/a2tv/a2tv.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=rnttwmjcin.turknet.ercdn.net&sz=128",
            description = "A2TV Canlı Yayın"
        ),
        CanliKanal(
            id = "aras-tv",
            name = "Aras TV",
            category = "ulusal",
            streamUrl = "http://1.rtmp.org/tv217/yayin.stream/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=1.rtmp.org&sz=128",
            description = "Aras TV Canlı Yayın"
        ),
        CanliKanal(
            id = "dha",
            name = "DHA",
            category = "haber",
            streamUrl = "https://603c568fccdf5.streamlock.net/live/dhaweb1_C5efC/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=603c568fccdf5.streamlock.net&sz=128",
            description = "DHA Canlı Yayın"
        ),
        CanliKanal(
            id = "deniz-postasi-tv",
            name = "Deniz Postasi TV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_denizpostasi/denizpostasi/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Deniz Postasi TV Canlı Yayın"
        ),
        CanliKanal(
            id = "diyar-tv",
            name = "Diyar TV",
            category = "dini",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_diyartv/diyartv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Diyar TV Canlı Yayın"
        ),
        CanliKanal(
            id = "bir-tv",
            name = "Bir TV",
            category = "ulusal",
            streamUrl = "https://edge.taksimbilisim.com/birtv/bant1/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=edge.taksimbilisim.com&sz=128",
            description = "Bir TV Canlı Yayın"
        ),
        CanliKanal(
            id = "ertv",
            name = "ERTV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_ertv_new/ertv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "ERTV Canlı Yayın"
        ),
        CanliKanal(
            id = "al-zahra-tv-turkic",
            name = "Al-Zahra TV Turkic",
            category = "dini",
            streamUrl = "https://live.al-zahratv.com/live/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.al-zahratv.com&sz=128",
            description = "Al-Zahra TV Turkic Canlı Yayın"
        ),
        CanliKanal(
            id = "ekoturk",
            name = "Ekoturk",
            category = "haber",
            streamUrl = "https://cdn.ekoturk.net/hls/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=cdn.ekoturk.net&sz=128",
            description = "Ekoturk Canlı Yayın"
        ),
        CanliKanal(
            id = "a-spor-sd",
            name = "A Spor SD",
            category = "spor",
            streamUrl = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=rnttwmjcin.turknet.ercdn.net&sz=128",
            description = "A Spor SD Canlı Yayın"
        ),
        CanliKanal(
            id = "es-tv",
            name = "ES TV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_estv/estv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "ES TV Canlı Yayın"
        ),
        CanliKanal(
            id = "babyfirst",
            name = "BabyFirst",
            category = "cocuk",
            streamUrl = "https://cdn.fast.jwp.services/v1/channel/1_jB6940Ei_TvhvIHDL/manifest.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=cdn.fast.jwp.services&sz=128",
            description = "BabyFirst Canlı Yayın"
        ),
        CanliKanal(
            id = "cnbc-e",
            name = "CNBC-e",
            category = "ulusal",
            streamUrl = "https://hnpsechtsc.turknet.ercdn.net/xpnvudnlsv/cnbc-e/cnbc-e.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=hnpsechtsc.turknet.ercdn.net&sz=128",
            description = "CNBC-e Canlı Yayın"
        ),
        CanliKanal(
            id = "etv-kayseri",
            name = "ETV Kayseri",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_etv/etv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "ETV Kayseri Canlı Yayın"
        ),
        CanliKanal(
            id = "fb-tv",
            name = "FB TV",
            category = "spor",
            streamUrl = "http://1hskrdto.rocketcdn.com/fenerbahcetv.smil/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=1hskrdto.rocketcdn.com&sz=128",
            description = "FB TV Canlı Yayın"
        ),
        CanliKanal(
            id = "atv-alanya",
            name = "ATV Alanya",
            category = "ulusal",
            streamUrl = "https://cdn-alanyatv.yayin.com.tr/alanyatv/alanyatv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=cdn-alanyatv.yayin.com.tr&sz=128",
            description = "ATV Alanya Canlı Yayın"
        ),
        CanliKanal(
            id = "alanya-posta-tv",
            name = "Alanya Posta TV",
            category = "ulusal",
            streamUrl = "https://api-tv3.yayin.com.tr/postatv/postatv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=api-tv3.yayin.com.tr&sz=128",
            description = "Alanya Posta TV Canlı Yayın"
        ),
        CanliKanal(
            id = "360",
            name = "360",
            category = "haber",
            streamUrl = "https://turkmedya-live.ercdn.net/tv360/tv360.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=turkmedya-live.ercdn.net&sz=128",
            description = "360 Canlı Yayın"
        ),
        CanliKanal(
            id = "24-tv",
            name = "24 TV",
            category = "haber",
            streamUrl = "https://turkmedya-live.ercdn.net/tv24/tv24.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=turkmedya-live.ercdn.net&sz=128",
            description = "24 TV Canlı Yayın"
        ),
        CanliKanal(
            id = "haber-global",
            name = "Haber Global",
            category = "haber",
            streamUrl = "https://tv.ensonhaber.com/haberglobal/haberglobal.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv.ensonhaber.com&sz=128",
            description = "Haber Global Canlı Yayın"
        ),
        CanliKanal(
            id = "haberturk-tv",
            name = "Haberturk TV",
            category = "haber",
            streamUrl = "https://tv.ensonhaber.com/haberturk/haberturk.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv.ensonhaber.com&sz=128",
            description = "Haberturk TV Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-3",
            name = "Kanal 3",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_kanal3/kanal3/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Kanal 3 Canlı Yayın"
        ),
        CanliKanal(
            id = "disney-jr",
            name = "Disney Jr.",
            category = "cocuk",
            streamUrl = "https://saran-live.ercdn.net/disneyjunior/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=saran-live.ercdn.net&sz=128",
            description = "Disney Jr. Canlı Yayın"
        ),
        CanliKanal(
            id = "edessa-tv",
            name = "Edessa TV",
            category = "ulusal",
            streamUrl = "https://canli.edessatv.com/hls/stream.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=canli.edessatv.com&sz=128",
            description = "Edessa TV Canlı Yayın"
        ),
        CanliKanal(
            id = "htspor-tv",
            name = "HTSpor TV",
            category = "spor",
            streamUrl = "https://ciner.daioncdn.net/ht-spor/ht-spor.m3u8?app=web",
            posterUrl = "https://www.google.com/s2/favicons?domain=ciner.daioncdn.net&sz=128",
            description = "HTSpor TV Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-12",
            name = "Kanal 12",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_kanal12/kanal12/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Kanal 12 Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-15",
            name = "Kanal 15",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_kanal15/kanal15/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Kanal 15 Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-26",
            name = "Kanal 26",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_kanal26/kanal26/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Kanal 26 Canlı Yayın"
        ),
        CanliKanal(
            id = "euro-d",
            name = "Euro D",
            category = "ulusal",
            streamUrl = "https://live.duhnet.tv/S2/HLS_LIVE/eurodnp/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.duhnet.tv&sz=128",
            description = "Euro D Canlı Yayın"
        ),
        CanliKanal(
            id = "erzurum-web-tv",
            name = "Erzurum Web TV",
            category = "ulusal",
            streamUrl = "https://win29.yayin.com.tr/erzurumwebtv/erzurumwebtv/iptvdelisi.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=win29.yayin.com.tr&sz=128",
            description = "Erzurum Web TV Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-58",
            name = "Kanal 58",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_kanal58/kanal58/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Kanal 58 Canlı Yayın"
        ),
        CanliKanal(
            id = "dream-turk",
            name = "Dream Turk",
            category = "radyo",
            streamUrl = "https://live.duhnet.tv/S2/HLS_LIVE/dreamturknp/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.duhnet.tv&sz=128",
            description = "Dream Turk Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-firat",
            name = "Kanal Firat",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_kanalfirat/kanalfirat/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Kanal Firat Canlı Yayın"
        ),
        CanliKanal(
            id = "haber61-tv",
            name = "Haber61 TV",
            category = "haber",
            streamUrl = "https://cdn-haber61tv.yayin.com.tr/haber61tv/smil:haber61tv.smil/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=cdn-haber61tv.yayin.com.tr&sz=128",
            description = "Haber61 TV Canlı Yayın"
        ),
        CanliKanal(
            id = "flash-haber-tv",
            name = "Flash Haber TV",
            category = "haber",
            streamUrl = "https://b01c02nl.mediatriple.net/videoonlylive/mtyycglqauzjhlive/broadcast_67c053c48829f.smil/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=b01c02nl.mediatriple.net&sz=128",
            description = "Flash Haber TV Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-v",
            name = "Kanal V",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_kanalv/kanalv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Kanal V Canlı Yayın"
        ),
        CanliKanal(
            id = "kay-tv",
            name = "Kay TV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_kaytv/kaytv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Kay TV Canlı Yayın"
        ),
        CanliKanal(
            id = "kent-turk",
            name = "Kent Turk",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_kentturktv/kentturktv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Kent Turk Canlı Yayın"
        ),
        CanliKanal(
            id = "konya-olay-tv",
            name = "Konya Olay TV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_konyaolaytv/konyaolaytv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Konya Olay TV Canlı Yayın"
        ),
        CanliKanal(
            id = "life-tv",
            name = "Life TV",
            category = "haber",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_lifetv/lifetv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Life TV Canlı Yayın"
        ),
        CanliKanal(
            id = "ilke-tv",
            name = "Ilke TV",
            category = "ulusal",
            streamUrl = "https://stream.ilketv.com.tr/hls/ilkecanli.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=stream.ilketv.com.tr&sz=128",
            description = "Ilke TV Canlı Yayın"
        ),
        CanliKanal(
            id = "finest-tv",
            name = "Finest TV",
            category = "radyo",
            streamUrl = "https://media.finest.tv/hls/live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=media.finest.tv&sz=128",
            description = "Finest TV Canlı Yayın"
        ),
        CanliKanal(
            id = "halk-tv-sd",
            name = "Halk TV SD",
            category = "haber",
            streamUrl = "https://halktv-live.daioncdn.net/halktv/halktv.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=halktv-live.daioncdn.net&sz=128",
            description = "Halk TV SD Canlı Yayın"
        ),
        CanliKanal(
            id = "imam-hussein-tv-5",
            name = "Imam Hussein TV 5",
            category = "dini",
            streamUrl = "http://live.al-zahratv.com/live/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.al-zahratv.com&sz=128",
            description = "Imam Hussein TV 5 Canlı Yayın"
        ),
        CanliKanal(
            id = "mavikaradeniz-tv",
            name = "MaviKaradeniz TV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_mavikaradeniz/mavikaradeniz/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "MaviKaradeniz TV Canlı Yayın"
        ),
        CanliKanal(
            id = "meltem-tv",
            name = "Meltem TV",
            category = "ulusal",
            streamUrl = "https://vhxyrsly.rocketcdn.com/meltemtv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=vhxyrsly.rocketcdn.com&sz=128",
            description = "Meltem TV Canlı Yayın"
        ),
        CanliKanal(
            id = "mercan-tv",
            name = "Mercan TV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_mercantv/mercantv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Mercan TV Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-23",
            name = "Kanal 23",
            category = "ulusal",
            streamUrl = "https://cdn-kanal23.yayin.com.tr/kanal23/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=cdn-kanal23.yayin.com.tr&sz=128",
            description = "Kanal 23 Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-7-avrupa",
            name = "Kanal 7 Avrupa",
            category = "ulusal",
            streamUrl = "https://livetv.radyotvonline.net/kanal7live/kanal7avr/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=livetv.radyotvonline.net&sz=128",
            description = "Kanal 7 Avrupa Canlı Yayın"
        ),
        CanliKanal(
            id = "kocaeli-tv",
            name = "Kocaeli TV",
            category = "ulusal",
            streamUrl = "https://edge.taksimbilisim.com/kocaelitv/bant1/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=edge.taksimbilisim.com&sz=128",
            description = "Kocaeli TV Canlı Yayın"
        ),
        CanliKanal(
            id = "finans-turk-tv",
            name = "Finans Turk TV",
            category = "haber",
            streamUrl = "https://yayin30.haber100.com/live/finansturk/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=yayin30.haber100.com&sz=128",
            description = "Finans Turk TV Canlı Yayın"
        ),
        CanliKanal(
            id = "love-nature",
            name = "Love Nature",
            category = "ulusal",
            streamUrl = "https://aegis-cloudfront-1.tubi.video/6d6d0f24-8445-4b4c-bdf6-44f9e38beaa4/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=aegis-cloudfront-1.tubi.video&sz=128",
            description = "Love Nature Canlı Yayın"
        ),
        CanliKanal(
            id = "olayturk-tv",
            name = "OlayTurk TV",
            category = "haber",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_olayturk/olayturk/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "OlayTurk TV Canlı Yayın"
        ),
        CanliKanal(
            id = "kral-pop-tv",
            name = "Kral Pop TV",
            category = "radyo",
            streamUrl = "https://dogus-live.daioncdn.net/kralpoptv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=dogus-live.daioncdn.net&sz=128",
            description = "Kral Pop TV Canlı Yayın"
        ),
        CanliKanal(
            id = "luys-tv",
            name = "Luys TV",
            category = "kultur",
            streamUrl = "https://b01c02nl.mediatriple.net/videoonlylive/mtpayqrfkgirxelive/broadcast_5e91c5ac96898.smil/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=b01c02nl.mediatriple.net&sz=128",
            description = "Luys TV Canlı Yayın"
        ),
        CanliKanal(
            id = "minika-go",
            name = "Minika Go",
            category = "cocuk",
            streamUrl = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/minikago/minikago.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=rnttwmjcin.turknet.ercdn.net&sz=128",
            description = "Minika Go Canlı Yayın"
        ),
        CanliKanal(
            id = "power-dance",
            name = "Power Dance",
            category = "radyo",
            streamUrl = "https://livetv.powerapp.com.tr/dance/dance.smil/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=livetv.powerapp.com.tr&sz=128",
            description = "Power Dance Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-d-drama",
            name = "Kanal D Drama",
            category = "ulusal",
            streamUrl = "http://190.93.224.42/KANAL-D-DRAMA/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=190.93.224.42&sz=128",
            description = "Kanal D Drama Canlı Yayın"
        ),
        CanliKanal(
            id = "powerturk-akustik",
            name = "PowerTurk Akustik",
            category = "radyo",
            streamUrl = "https://livetv.powerapp.com.tr/pturkakustik/akustik.smil/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=livetv.powerapp.com.tr&sz=128",
            description = "PowerTurk Akustik Canlı Yayın"
        ),
        CanliKanal(
            id = "number-1-ask",
            name = "Number 1 Ask",
            category = "radyo",
            streamUrl = "https://b01c02nl.mediatriple.net/videoonlylive/mtkgeuihrlfwlive/u_stream_5c9e18f9cea15_1/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=b01c02nl.mediatriple.net&sz=128",
            description = "Number 1 Ask Canlı Yayın"
        ),
        CanliKanal(
            id = "kurdistan-tv",
            name = "Kurdistan TV",
            category = "ulusal",
            streamUrl = "https://5a3ed7a72ed4b.streamlock.net/live/SMIL:myStream.smil/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=5a3ed7a72ed4b.streamlock.net&sz=128",
            description = "Kurdistan TV Canlı Yayın"
        ),
        CanliKanal(
            id = "powerturk-tv",
            name = "PowerTurk TV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_powerturktv/powerturktv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "PowerTurk TV Canlı Yayın"
        ),
        CanliKanal(
            id = "minika-cocuk",
            name = "Minika Cocuk",
            category = "cocuk",
            streamUrl = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/minikago_cocuk/minikago_cocuk.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=rnttwmjcin.turknet.ercdn.net&sz=128",
            description = "Minika Cocuk Canlı Yayın"
        ),
        CanliKanal(
            id = "power-love",
            name = "Power Love",
            category = "radyo",
            streamUrl = "https://livetv.powerapp.com.tr/plove/love.smil/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=livetv.powerapp.com.tr&sz=128",
            description = "Power Love Canlı Yayın"
        ),
        CanliKanal(
            id = "sat-7-turk",
            name = "Sat 7 Turk",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_sat7turk/sat7turk/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Sat 7 Turk Canlı Yayın"
        ),
        CanliKanal(
            id = "power-tv",
            name = "Power TV",
            category = "ulusal",
            streamUrl = "https://livetv.powerapp.com.tr/powerTV/powerhd.smil/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=livetv.powerapp.com.tr&sz=128",
            description = "Power TV Canlı Yayın"
        ),
        CanliKanal(
            id = "powerturk-slow",
            name = "PowerTurk Slow",
            category = "radyo",
            streamUrl = "https://livetv.powerapp.com.tr/pturkslow/slow.smil/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=livetv.powerapp.com.tr&sz=128",
            description = "PowerTurk Slow Canlı Yayın"
        ),
        CanliKanal(
            id = "number-1-dance",
            name = "Number 1 Dance",
            category = "radyo",
            streamUrl = "https://b01c02nl.mediatriple.net/videoonlylive/mtkgeuihrlfwlive/u_stream_5c9e2aa8acf44_1/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=b01c02nl.mediatriple.net&sz=128",
            description = "Number 1 Dance Canlı Yayın"
        ),
        CanliKanal(
            id = "powerturk-taptaze",
            name = "PowerTurk Taptaze",
            category = "radyo",
            streamUrl = "https://livetv.powerapp.com.tr/pturktaptaze/taptaze.smil/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=livetv.powerapp.com.tr&sz=128",
            description = "PowerTurk Taptaze Canlı Yayın"
        ),
        CanliKanal(
            id = "sun-rtv",
            name = "Sun RTV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_sunrtv/sunrtv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Sun RTV Canlı Yayın"
        ),
        CanliKanal(
            id = "star-tv",
            name = "Star TV",
            category = "ulusal",
            streamUrl = "https://dogus.daioncdn.net/startv/startv_720p.m3u8?app=a20ac41e-bdc3-4aa1-934d-26b484480ac9&ce=3&sid=8l4w3lst4co5",
            posterUrl = "https://www.google.com/s2/favicons?domain=dogus.daioncdn.net&sz=128",
            description = "Star TV Canlı Yayın"
        ),
        CanliKanal(
            id = "number-1-tv",
            name = "Number 1 TV",
            category = "radyo",
            streamUrl = "https://b01c02nl.mediatriple.net/videoonlylive/mtkgeuihrlfwlive/broadcast_5c9e17cd59e8b.smil/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=b01c02nl.mediatriple.net&sz=128",
            description = "Number 1 TV Canlı Yayın"
        ),
        CanliKanal(
            id = "tempo-tv",
            name = "Tempo TV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_tempotv/tempotv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Tempo TV Canlı Yayın"
        ),
        CanliKanal(
            id = "number-1-damar",
            name = "Number 1 Damar",
            category = "radyo",
            streamUrl = "https://b01c02nl.mediatriple.net/videoonlylive/mtkgeuihrlfwlive/u_stream_5c9e198784bdc_1/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=b01c02nl.mediatriple.net&sz=128",
            description = "Number 1 Damar Canlı Yayın"
        ),
        CanliKanal(
            id = "tivi-6",
            name = "Tivi 6",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_tivi6/tivi6/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Tivi 6 Canlı Yayın"
        ),
        CanliKanal(
            id = "persiana-turkiye",
            name = "Persiana Turkiye",
            category = "ulusal",
            streamUrl = "https://turkhls.persiana.live/hls/stream.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=turkhls.persiana.live&sz=128",
            description = "Persiana Turkiye Canlı Yayın"
        ),
        CanliKanal(
            id = "ton-tv",
            name = "Ton TV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_tontv/tontv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Ton TV Canlı Yayın"
        ),
        CanliKanal(
            id = "sercem-tv",
            name = "Sercem TV",
            category = "dini",
            streamUrl = "https://canli.sercemtv.com.tr/hls/0/stream.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=canli.sercemtv.com.tr&sz=128",
            description = "Sercem TV Canlı Yayın"
        ),
        CanliKanal(
            id = "tgrt-belgesel",
            name = "TGRT Belgesel",
            category = "ulusal",
            streamUrl = "https://b01c02nl.mediatriple.net/videoonlylive/mtsxxkzwwuqtglive/broadcast_5fe462afc6a0e.smil/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=b01c02nl.mediatriple.net&sz=128",
            description = "TGRT Belgesel Canlı Yayın"
        ),
        CanliKanal(
            id = "tbmm-tv",
            name = "TBMM TV",
            category = "ulusal",
            streamUrl = "https://meclistv-live.ercdn.net/meclistv/meclistv.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=meclistv-live.ercdn.net&sz=128",
            description = "TBMM TV Canlı Yayın"
        ),
        CanliKanal(
            id = "love-nature-hd",
            name = "Love Nature HD",
            category = "ulusal",
            streamUrl = "https://mumbai-edge.smartplaytv.in/LoveNature/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=mumbai-edge.smartplaytv.in&sz=128",
            description = "Love Nature HD Canlı Yayın"
        ),
        CanliKanal(
            id = "tele-1",
            name = "Tele 1",
            category = "ulusal",
            streamUrl = "https://tele1-live.ercdn.net/tele1/tele1.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tele1-live.ercdn.net&sz=128",
            description = "Tele 1 Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-naturescape",
            name = "Stingray Naturescape",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/naturescape-a003-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Naturescape Canlı Yayın"
        ),
        CanliKanal(
            id = "mc-tv",
            name = "MC TV",
            category = "ulusal",
            streamUrl = "https://rrr.sz.xlcdn.com/?account=mceutv&file=mc2&output=playlist.m3u8&protocol=https&service=wowza&type=live",
            posterUrl = "https://www.google.com/s2/favicons?domain=rrr.sz.xlcdn.com&sz=128",
            description = "MC TV Canlı Yayın"
        ),
        CanliKanal(
            id = "tjk-tv",
            name = "TJK TV",
            category = "spor",
            streamUrl = "https://tjktv-live.tjk.org/tjktv.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tjktv-live.tjk.org&sz=128",
            description = "TJK TV Canlı Yayın"
        ),
        CanliKanal(
            id = "tjk-tv-2",
            name = "TJK TV 2",
            category = "spor",
            streamUrl = "https://tjktv-live.tjk.org/tjktv2/tjktv2.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tjktv-live.tjk.org&sz=128",
            description = "TJK TV 2 Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-avaz",
            name = "TRT Avaz",
            category = "ulusal",
            streamUrl = "https://tv-trtavaz.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv-trtavaz.medya.trt.com.tr&sz=128",
            description = "TRT Avaz Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-arabi",
            name = "TRT Arabi",
            category = "haber",
            streamUrl = "https://tv-trtarabi.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv-trtarabi.medya.trt.com.tr&sz=128",
            description = "TRT Arabi Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-eba-ilkokul",
            name = "TRT EBA Ilkokul",
            category = "kultur",
            streamUrl = "https://tv-e-okul00.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv-e-okul00.medya.trt.com.tr&sz=128",
            description = "TRT EBA Ilkokul Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-diyanet-cocuk",
            name = "TRT Diyanet Cocuk",
            category = "cocuk",
            streamUrl = "https://tv-trtdiyanetcocuk.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv-trtdiyanetcocuk.medya.trt.com.tr&sz=128",
            description = "TRT Diyanet Cocuk Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-eba-lise",
            name = "TRT EBA Lise",
            category = "kultur",
            streamUrl = "https://tv-e-okul02.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv-e-okul02.medya.trt.com.tr&sz=128",
            description = "TRT EBA Lise Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-3",
            name = "TRT 3",
            category = "ulusal",
            streamUrl = "https://tv-trt3.live.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv-trt3.live.trt.com.tr&sz=128",
            description = "TRT 3 Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-genc",
            name = "TRT Genc",
            category = "kultur",
            streamUrl = "https://tv-trtgenc.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv-trtgenc.medya.trt.com.tr&sz=128",
            description = "TRT Genc Canlı Yayın"
        ),
        CanliKanal(
            id = "spacetoon-turkey",
            name = "Spacetoon Turkey",
            category = "cocuk",
            streamUrl = "https://live-tr-next.spacetoongo.com/ST_TR_NEXT/hls/h7qefeiwfbjn1.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live-tr-next.spacetoongo.com&sz=128",
            description = "Spacetoon Turkey Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-eba",
            name = "TRT EBA",
            category = "kultur",
            streamUrl = "https://tv-e-okul01.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv-e-okul01.medya.trt.com.tr&sz=128",
            description = "TRT EBA Canlı Yayın"
        ),
        CanliKanal(
            id = "urfa-natik-tv",
            name = "Urfa Natik TV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_urfanatiktv/urfanatiktv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Urfa Natik TV Canlı Yayın"
        ),
        CanliKanal(
            id = "van-65-tv",
            name = "Van 65 TV",
            category = "ulusal",
            streamUrl = "https://live.artidijitalmedya.com/artidijital_van65/van65/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.artidijitalmedya.com&sz=128",
            description = "Van 65 TV Canlı Yayın"
        ),
        CanliKanal(
            id = "tv-4",
            name = "TV 4",
            category = "ulusal",
            streamUrl = "https://turkmedya-live.ercdn.net/tv4/tv4.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=turkmedya-live.ercdn.net&sz=128",
            description = "TV 4 Canlı Yayın"
        ),
        CanliKanal(
            id = "yol-tv",
            name = "YOL TV",
            category = "ulusal",
            streamUrl = "https://live.yoltv.com/hls/stream.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.yoltv.com&sz=128",
            description = "YOL TV Canlı Yayın"
        ),
        CanliKanal(
            id = "tv-264",
            name = "TV 264",
            category = "ulusal",
            streamUrl = "https://b01c02nl.mediatriple.net/videoonlylive/mtdxkkitgbrckilive/broadcast_5ee244263fd6d.smil/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=b01c02nl.mediatriple.net&sz=128",
            description = "TV 264 Canlı Yayın"
        ),
        CanliKanal(
            id = "tv-den",
            name = "TV Den",
            category = "ulusal",
            streamUrl = "http://canli.tvden.com.tr/hls/live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=canli.tvden.com.tr&sz=128",
            description = "TV Den Canlı Yayın"
        ),
        CanliKanal(
            id = "vav-tv",
            name = "Vav TV",
            category = "ulusal",
            streamUrl = "https://playlist.fasttvcdn.com/pl/rfrk9821hdy9dayo8wfyha/kltr-sanat-tv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=playlist.fasttvcdn.com&sz=128",
            description = "Vav TV Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-turk",
            name = "TRT Turk",
            category = "ulusal",
            streamUrl = "https://tv-trtturk.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv-trtturk.medya.trt.com.tr&sz=128",
            description = "TRT Turk Canlı Yayın"
        ),
        CanliKanal(
            id = "west-azerbaijan-tv",
            name = "West Azerbaijan TV",
            category = "ulusal",
            streamUrl = "https://ncdn.telewebion.ir/azarbayjangharbi/live/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=ncdn.telewebion.ir&sz=128",
            description = "West Azerbaijan TV Canlı Yayın"
        ),

        // --- Beles TiVi kaynagindan eklenen kanallar ---
        CanliKanal(
            id = "babytv",
            name = "BabyTv",
            category = "cocuk",
            streamUrl = "https://saran-live.ercdn.net/babytv/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=saran-live.ercdn.net&sz=128",
            description = "BabyTv Canlı Yayın"
        ),
        CanliKanal(
            id = "teve2",
            name = "Teve2",
            category = "ulusal",
            streamUrl = "https://ackaxsqacw.turknet.ercdn.net/ozfkfbbjba/teve2/teve2.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=ackaxsqacw.turknet.ercdn.net&sz=128",
            description = "Teve2 Canlı Yayın"
        ),
        CanliKanal(
            id = "dmax",
            name = "DMAX",
            category = "kultur",
            streamUrl = "https://dogus.daioncdn.net/dmax/dmax.m3u8?ce=3&app=5a02c599-d17e-4982-9b04-090934d51af7",
            posterUrl = "https://www.google.com/s2/favicons?domain=dogus.daioncdn.net&sz=128",
            description = "DMAX Canlı Yayın"
        ),
        CanliKanal(
            id = "tlc",
            name = "TLC",
            category = "kultur",
            streamUrl = "https://dogus.daioncdn.net/tlc/tlc.m3u8?ce=3&app=1aca0aa1-06e3-41ff-a8e5-38d7df475469",
            posterUrl = "https://www.google.com/s2/favicons?domain=dogus.daioncdn.net&sz=128",
            description = "TLC Canlı Yayın"
        ),
        CanliKanal(
            id = "destination-nature",
            name = "Destination Nature",
            category = "kultur",
            streamUrl = "https://d2cl8wi5f8zf1j.cloudfront.net/v1/master/3722c60a815c199d9c0ef36c5b73da68a62b09d1/cc-0ay35jkybhd8m/DestinationNature_FR.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=d2cl8wi5f8zf1j.cloudfront.net&sz=128",
            description = "Destination Nature Canlı Yayın"
        ),
        CanliKanal(
            id = "disney-jr",
            name = "Disney Jr",
            category = "cocuk",
            streamUrl = "https://saran-live.ercdn.net/disneyjunior/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=saran-live.ercdn.net&sz=128",
            description = "Disney Jr Canlı Yayın"
        ),
        CanliKanal(
            id = "dream-t-rk",
            name = "DREAM TÜRK",
            category = "radyo",
            streamUrl = "https://live.duhnet.tv/S2/HLS_LIVE/dreamturknp/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live.duhnet.tv&sz=128",
            description = "DREAM TÜRK Canlı Yayın"
        ),
        CanliKanal(
            id = "nature-time",
            name = "Nature Time",
            category = "kultur",
            streamUrl = "https://amg00090-blueantmedia-naturetime-samsungfr-yakbf.amagi.tv/playlist/amg00090-blueantmedia-naturetime-samsungfr/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=amg00090-blueantmedia-naturetime-samsungfr-yakbf.amagi.tv&sz=128",
            description = "Nature Time Canlı Yayın"
        ),
        CanliKanal(
            id = "lovenature",
            name = "lovenature",
            category = "kultur",
            streamUrl = "https://viptine.site/2/vavoo.php?hls=259380083927b83aced3e1",
            posterUrl = "https://www.google.com/s2/favicons?domain=viptine.site&sz=128",
            description = "lovenature Canlı Yayın"
        ),
        CanliKanal(
            id = "natgeowild",
            name = "Natgeowild",
            category = "kultur",
            streamUrl = "https://viptine.site/2/vavoo.php?hls=1519237116daf8e783a22b",
            posterUrl = "https://www.google.com/s2/favicons?domain=viptine.site&sz=128",
            description = "Natgeowild Canlı Yayın"
        ),
        CanliKanal(
            id = "viasathistory",
            name = "Viasathistory",
            category = "kultur",
            streamUrl = "https://viptine.site/2/vavoo.php?hls=1335130209de4b92f31496",
            posterUrl = "https://www.google.com/s2/favicons?domain=viptine.site&sz=128",
            description = "Viasathistory Canlı Yayın"
        ),
        CanliKanal(
            id = "abantv-1",
            name = "ŞabanTv -1",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/Kemal-sunal-1.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "ŞabanTv -1 Canlı Yayın"
        ),
        CanliKanal(
            id = "ye-il-am",
            name = "Yeşilçam",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/Trfilmler.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Yeşilçam Canlı Yayın"
        ),
        CanliKanal(
            id = "zengin-k-z-fakir-o-lan",
            name = "Zengin Kız Fakir Oğlan",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/zengin-kiz.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Zengin Kız Fakir Oğlan Canlı Yayın"
        ),
        CanliKanal(
            id = "abantv-2",
            name = "ŞabanTv -2",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/Kemal-sunal-2.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "ŞabanTv -2 Canlı Yayın"
        ),
        CanliKanal(
            id = "a-k-memnu",
            name = "Aşk-ı Memnu",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/aski-memnu.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Aşk-ı Memnu Canlı Yayın"
        ),
        CanliKanal(
            id = "g-l-ah-film",
            name = "Gülşah Film",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/Gulsah-film.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Gülşah Film Canlı Yayın"
        ),
        CanliKanal(
            id = "yaprak-d-k-m",
            name = "Yaprak Dökümü",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/yaprak-dokumu.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Yaprak Dökümü Canlı Yayın"
        ),
        CanliKanal(
            id = "dirili-ertu-rul",
            name = "Diriliş Ertuğrul",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/dirilis-ertugrul.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Diriliş Ertuğrul Canlı Yayın"
        ),
        CanliKanal(
            id = "kuzey-g-ney",
            name = "Kuzey Güney",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/kuzey-guney.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Kuzey Güney Canlı Yayın"
        ),
        CanliKanal(
            id = "g-n-l-da",
            name = "Gönül Dağı",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/gonul-dagi.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Gönül Dağı Canlı Yayın"
        ),
        CanliKanal(
            id = "5-minutes-craft",
            name = "5 MINUTES CRAFT",
            category = "cocuk",
            streamUrl = "https://soul-5mincrafteng-rakuten.amagi.tv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=soul-5mincrafteng-rakuten.amagi.tv&sz=128",
            description = "5 MINUTES CRAFT Canlı Yayın"
        ),
        CanliKanal(
            id = "ad-n-feriha-koydum",
            name = "Adını Feriha Koydum",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/Feriha.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Adını Feriha Koydum Canlı Yayın"
        ),
        CanliKanal(
            id = "tabii-tv",
            name = "tabii tv",
            category = "eglence",
            streamUrl = "https://ceokzokgtd.erbvr.com/tabiitv/tabiitv.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=ceokzokgtd.erbvr.com&sz=128",
            description = "tabii tv Canlı Yayın"
        ),
        CanliKanal(
            id = "cnn-t-rk",
            name = "Cnn Türk",
            category = "haber",
            streamUrl = "https://trn03.tulix.tv/gt-cnnturk-tv/index.m3u8?token=10a5253f5ee4a368d5ffae3e332099c8",
            posterUrl = "https://www.google.com/s2/favicons?domain=trn03.tulix.tv&sz=128",
            description = "Cnn Türk Canlı Yayın"
        ),
        CanliKanal(
            id = "k-smetse-olur",
            name = "Kısmetse Olur",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/kismetse-olur.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Kısmetse Olur Canlı Yayın"
        ),
        CanliKanal(
            id = "cnn-t-rk",
            name = "Cnn Türk",
            category = "haber",
            streamUrl = "https://cnn.impresents.workers.dev/canli-yayin.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=cnn.impresents.workers.dev&sz=128",
            description = "Cnn Türk Canlı Yayın"
        ),
        CanliKanal(
            id = "s-f-r-bir",
            name = "Sıfır Bir",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/sifir-bir.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Sıfır Bir Canlı Yayın"
        ),
        CanliKanal(
            id = "fatmag-l-n-su-u-ne",
            name = "Fatmagül'ün Suçu Ne?",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/Fatmagul.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Fatmagül'ün Suçu Ne? Canlı Yayın"
        ),
        CanliKanal(
            id = "tolgshow",
            name = "Tolgshow",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/Tolgshow.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Tolgshow Canlı Yayın"
        ),
        CanliKanal(
            id = "i-nci-taneleri",
            name = "İnci Taneleri",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/inci-taneleri.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "İnci Taneleri Canlı Yayın"
        ),
        CanliKanal(
            id = "fx",
            name = "Fx",
            category = "eglence",
            streamUrl = "https://saran-live.ercdn.net/fx/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=saran-live.ercdn.net&sz=128",
            description = "Fx Canlı Yayın"
        ),
        CanliKanal(
            id = "k-z-lc-k-erbeti",
            name = "Kızılcık Şerbeti",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/kizilcik-serbeti.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Kızılcık Şerbeti Canlı Yayın"
        ),
        CanliKanal(
            id = "hz-yusuf",
            name = "Hz Yusuf",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/hzyusuf.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Hz Yusuf Canlı Yayın"
        ),
        CanliKanal(
            id = "arkada-m-ho-geldin",
            name = "Arkadaşım Hoşgeldin",
            category = "eglence",
            streamUrl = "https://raw.githubusercontent.com/panorea744/stawerlo/refs/heads/main/noutube1/arkadasim-hosgeldin.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=raw.githubusercontent.com&sz=128",
            description = "Arkadaşım Hoşgeldin Canlı Yayın"
        ),
        CanliKanal(
            id = "france-24-english",
            name = "France 24 English",
            category = "haber",
            streamUrl = "https://dash3.antik.sk/live/test_france24_eng/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=dash3.antik.sk&sz=128",
            description = "France 24 English Canlı Yayın"
        ),
        CanliKanal(
            id = "al-jazeera-english",
            name = "Al Jazeera English",
            category = "haber",
            streamUrl = "https://dash4.antik.sk/live/test_aljazeera_eng/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=dash4.antik.sk&sz=128",
            description = "Al Jazeera English Canlı Yayın"
        ),
        CanliKanal(
            id = "bbc-world-news",
            name = "BBC World News",
            category = "haber",
            streamUrl = "https://vs-hls-push-ww-live.akamaized.net/x=4/i=urn:bbc:pips:service:bbc_news_channel_hd/t=3840/v=pv14/b=5070016/main.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=vs-hls-push-ww-live.akamaized.net&sz=128",
            description = "BBC World News Canlı Yayın"
        ),
        CanliKanal(
            id = "dw-english",
            name = "DW English",
            category = "haber",
            streamUrl = "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=dwamdstream102.akamaized.net&sz=128",
            description = "DW English Canlı Yayın"
        ),
        CanliKanal(
            id = "euronews-en",
            name = "Euronews EN",
            category = "haber",
            streamUrl = "https://cdn-euronews.akamaized.net/live/eds/euronews-en/25080/euronews-en.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=cdn-euronews.akamaized.net&sz=128",
            description = "Euronews EN Canlı Yayın"
        ),
        CanliKanal(
            id = "bloomberg-originals",
            name = "Bloomberg Originals",
            category = "haber",
            streamUrl = "https://www.bloomberg.com/media-manifest/streams/originals-global.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=www.bloomberg.com&sz=128",
            description = "Bloomberg Originals Canlı Yayın"
        ),
        CanliKanal(
            id = "bloomberg-us",
            name = "Bloomberg US",
            category = "haber",
            streamUrl = "https://www.bloomberg.com/media-manifest/streams/phoenix-us.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=www.bloomberg.com&sz=128",
            description = "Bloomberg US Canlı Yayın"
        ),
        CanliKanal(
            id = "bloomberg-eu",
            name = "Bloomberg EU",
            category = "haber",
            streamUrl = "https://www.bloomberg.com/media-manifest/streams/eu.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=www.bloomberg.com&sz=128",
            description = "Bloomberg EU Canlı Yayın"
        ),
        CanliKanal(
            id = "nhk-english",
            name = "NHK English",
            category = "haber",
            streamUrl = "https://media-tyo.hls.nhkworld.jp/hls/w/live/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=media-tyo.hls.nhkworld.jp&sz=128",
            description = "NHK English Canlı Yayın"
        ),
        CanliKanal(
            id = "cgtn-news",
            name = "CGTN NEWS",
            category = "haber",
            streamUrl = "https://english-livetx.cgtn.com/hls/yypdyyctzb_hd.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=english-livetx.cgtn.com&sz=128",
            description = "CGTN NEWS Canlı Yayın"
        ),
        CanliKanal(
            id = "kiss-tv",
            name = "Kiss TV",
            category = "radyo",
            streamUrl = "https://tv.broadcasting.ro/kisstv/a629c031-2170-4ee9-a36d-87a891aa3131_output_0.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv.broadcasting.ro&sz=128",
            description = "Kiss TV Canlı Yayın"
        ),
        CanliKanal(
            id = "cbs-news",
            name = "CBS News",
            category = "haber",
            streamUrl = "https://cbsn-us.cbsnstream.cbsnews.com/out/v1/55a8648e8f134e82a470f83d562deeca/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=cbsn-us.cbsnstream.cbsnews.com&sz=128",
            description = "CBS News Canlı Yayın"
        ),
        CanliKanal(
            id = "trace-latin",
            name = "Trace Latin",
            category = "radyo",
            streamUrl = "https://amg01131-tracetv-tracelatina-glewed-vtnk7.amagi.tv/playlist1080P.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=amg01131-tracetv-tracelatina-glewed-vtnk7.amagi.tv&sz=128",
            description = "Trace Latin Canlı Yayın"
        ),
        CanliKanal(
            id = "trace-urban",
            name = "Trace Urban",
            category = "radyo",
            streamUrl = "https://lightning-traceurban-samsungau.amagi.tv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lightning-traceurban-samsungau.amagi.tv&sz=128",
            description = "Trace Urban Canlı Yayın"
        ),
        CanliKanal(
            id = "viasathistory",
            name = "Viasathistory",
            category = "kultur",
            streamUrl = "https://kbl.impresents.workers.dev/cable-live/id/viasat-explore",
            posterUrl = "https://www.google.com/s2/favicons?domain=kbl.impresents.workers.dev&sz=128",
            description = "Viasathistory Canlı Yayın"
        ),
        CanliKanal(
            id = "company-tv",
            name = "Company TV",
            category = "radyo",
            streamUrl = "https://5929b138b139d.streamlock.net/CompanyTV/smil:CompanyTV.smil/chunklist_w2056647378_b1428000_slita.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=5929b138b139d.streamlock.net&sz=128",
            description = "Company TV Canlı Yayın"
        ),
        CanliKanal(
            id = "fox-news",
            name = "Fox News",
            category = "haber",
            streamUrl = "https://fox-foxnewsnow-vizio.amagi.tv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=fox-foxnewsnow-vizio.amagi.tv&sz=128",
            description = "Fox News Canlı Yayın"
        ),
        CanliKanal(
            id = "arirang-world",
            name = "ARIRANG WORLD",
            category = "haber",
            streamUrl = "https://amdlive-ch01-ctnd-com.akamaized.net/arirang_1ch/smil:arirang_1ch.smil/chunklist_b2256000_sleng.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=amdlive-ch01-ctnd-com.akamaized.net&sz=128",
            description = "ARIRANG WORLD Canlı Yayın"
        ),
        CanliKanal(
            id = "nbc-news",
            name = "NBC News",
            category = "haber",
            streamUrl = "https://xumo-drct-nbcnn-ir8ze.fast.nbcuni.com/live/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=xumo-drct-nbcnn-ir8ze.fast.nbcuni.com&sz=128",
            description = "NBC News Canlı Yayın"
        ),
        CanliKanal(
            id = "rtl-102-5-tv",
            name = "RTL 102.5 Tv",
            category = "radyo",
            streamUrl = "https://streamcdnb4-dd782ed59e2a4e86aabf6fc508674b59.msvdn.net/live/S97044836/tbbP8T1ZRPBL/chunklist_b3256000.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=streamcdnb4-dd782ed59e2a4e86aabf6fc508674b59.msvdn.net&sz=128",
            description = "RTL 102.5 Tv Canlı Yayın"
        ),
        CanliKanal(
            id = "vevo-pop",
            name = "Vevo Pop",
            category = "radyo",
            streamUrl = "https://amg00056-amg00056c9-rakuten-fr-3243.playouts.now.amagi.tv/playlist/amg00056-vevotvfast-vevopopfr-rakutenfr/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=amg00056-amg00056c9-rakuten-fr-3243.playouts.now.amagi.tv&sz=128",
            description = "Vevo Pop Canlı Yayın"
        ),
        CanliKanal(
            id = "vevo-hip-hop-rb",
            name = "Vevo Hip-Hop RB",
            category = "radyo",
            streamUrl = "https://amg00056-amg00056c14-rakuten-fr-3244.playouts.now.amagi.tv/playlist/amg00056-vevotvfast-vevohiphopandrbfr-rakutenfr/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=amg00056-amg00056c14-rakuten-fr-3244.playouts.now.amagi.tv&sz=128",
            description = "Vevo Hip-Hop RB Canlı Yayın"
        ),
        CanliKanal(
            id = "music-box-hits",
            name = "Music Box Hits",
            category = "radyo",
            streamUrl = "http://88.212.7.14/live/mb_hits/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=88.212.7.14&sz=128",
            description = "Music Box Hits Canlı Yayın"
        ),
        CanliKanal(
            id = "music-box-dance",
            name = "Music Box Dance",
            category = "radyo",
            streamUrl = "http://88.212.7.14/live/mb_dance/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=88.212.7.14&sz=128",
            description = "Music Box Dance Canlı Yayın"
        ),
        CanliKanal(
            id = "music-box-classic",
            name = "Music Box Classic",
            category = "radyo",
            streamUrl = "https://dash4.antik.sk/live/mb_classic/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=dash4.antik.sk&sz=128",
            description = "Music Box Classic Canlı Yayın"
        ),
        CanliKanal(
            id = "music-box-sexy",
            name = "Music Box Sexy",
            category = "radyo",
            streamUrl = "http://88.212.7.14/live/mb_sexy/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=88.212.7.14&sz=128",
            description = "Music Box Sexy Canlı Yayın"
        ),
        CanliKanal(
            id = "emotion-music",
            name = "Emotion Music",
            category = "radyo",
            streamUrl = "https://amg01736-harmonysecomgro-harmonymelody-samsungfr-n25pe.amagi.tv/playlist/amg01736-harmonysecomgro-harmonymelody-samsungfr/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=amg01736-harmonysecomgro-harmonymelody-samsungfr-n25pe.amagi.tv&sz=128",
            description = "Emotion Music Canlı Yayın"
        ),
        CanliKanal(
            id = "that-s-90s00s",
            name = "that's 90s00s",
            category = "radyo",
            streamUrl = "https://amg01076-amg01076c19-rakuten-gb-8653.playouts.now.amagi.tv/ts-eu-w1-n2/playlist/amg01076-lightning-now90s00s-rakutengb/cb513f19657460909bce2167d0d334dd844c3bdd0b61cb3071aa448f049f4b1bdae5a5d3ec4ae47821c04d873d5f66411f21c118dc7c1fc7c76adf046727486b9b9ab265d9896450f08d597562ac7b1c51e00e07c47100238ebb4e5dd670e7295243a5583f5063c0bf5fab704fc1d50bd8767dd8a9d963317c383ea57e657305d692dfd07777ae0eb3876a0ac47116b2a063f3e669dea0488f5371ac408451dfcdb0def92098b6937091938145c78e2062208d51b3b0e4ebc3a2a1ecfe51622a3b40f4ac83fb010c87554a4fc219aad541d21cd480298d6178ee4ec80fcc11846fb806aee50ef1fce09f6f4e3badc85e3a67b2f3fadba9657ded2fbb85ecde7873d6a7462809992dfe2b393d96f2d35bd59145d8e651e8de0e3c36c4834a9299c0e1bee1f705f642ffc9ff963255401972258353a78864d869fc76acdc1c28580d69a2985cc8839b321d6d4d8f8c5b942313b6d58e89bd566e2a01dfdb52cdbf8ef6cea6a54bcf99790ab8c7b3a23d0c/85/1920x1080_4716800/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=amg01076-amg01076c19-rakuten-gb-8653.playouts.now.amagi.tv&sz=128",
            description = "that's 90s00s Canlı Yayın"
        ),
        CanliKanal(
            id = "deluxe-music-rap",
            name = "DELUXE MUSIC Rap",
            category = "radyo",
            streamUrl = "https://sdn-global-live-streaming-packager-cache.3qsdn.com/65183/65183_264_live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=sdn-global-live-streaming-packager-cache.3qsdn.com&sz=128",
            description = "DELUXE MUSIC Rap Canlı Yayın"
        ),
        CanliKanal(
            id = "deluxe-music-english",
            name = "DELUXE MUSIC English",
            category = "radyo",
            streamUrl = "https://sdn-global-live-streaming-packager-cache.3qsdn.com/13456/13456_264_live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=sdn-global-live-streaming-packager-cache.3qsdn.com&sz=128",
            description = "DELUXE MUSIC English Canlı Yayın"
        ),
        CanliKanal(
            id = "movie-music",
            name = "Movie Music",
            category = "radyo",
            streamUrl = "https://stream.ads.ottera.tv/playlist.m3u8?network_id=3034",
            posterUrl = "https://www.google.com/s2/favicons?domain=stream.ads.ottera.tv&sz=128",
            description = "Movie Music Canlı Yayın"
        ),
        CanliKanal(
            id = "qello-concerts-2",
            name = "Qello Concerts 2",
            category = "radyo",
            streamUrl = "https://stream.ads.ottera.tv/playlist.m3u8?network_id=3039",
            posterUrl = "https://www.google.com/s2/favicons?domain=stream.ads.ottera.tv&sz=128",
            description = "Qello Concerts 2 Canlı Yayın"
        ),
        CanliKanal(
            id = "that-s-80s",
            name = "that's 80s",
            category = "radyo",
            streamUrl = "https://lightning-now80s-samsungnz.amagi.tv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lightning-now80s-samsungnz.amagi.tv&sz=128",
            description = "that's 80s Canlı Yayın"
        ),
        CanliKanal(
            id = "qello-concerts-1",
            name = "Qello Concerts 1",
            category = "radyo",
            streamUrl = "https://stream.ads.ottera.tv/playlist.m3u8?network_id=3031",
            posterUrl = "https://www.google.com/s2/favicons?domain=stream.ads.ottera.tv&sz=128",
            description = "Qello Concerts 1 Canlı Yayın"
        ),
        CanliKanal(
            id = "classica",
            name = "Classica",
            category = "radyo",
            streamUrl = "https://stream.ads.ottera.tv/playlist.m3u8?network_id=3038",
            posterUrl = "https://www.google.com/s2/favicons?domain=stream.ads.ottera.tv&sz=128",
            description = "Classica Canlı Yayın"
        ),
        CanliKanal(
            id = "djazz",
            name = "Djazz",
            category = "radyo",
            streamUrl = "https://stream.ads.ottera.tv/playlist.m3u8?network_id=3037",
            posterUrl = "https://www.google.com/s2/favicons?domain=stream.ads.ottera.tv&sz=128",
            description = "Djazz Canlı Yayın"
        ),
        CanliKanal(
            id = "that-s-rock",
            name = "that's Rock",
            category = "radyo",
            streamUrl = "https://lightning-now90s-samsungnz.amagi.tv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lightning-now90s-samsungnz.amagi.tv&sz=128",
            description = "that's Rock Canlı Yayın"
        ),
        CanliKanal(
            id = "qwest-jazz",
            name = "Qwest Jazz",
            category = "radyo",
            streamUrl = "https://qwestjazz-rakuten.amagi.tv/hls/amagi_hls_data_rakutenAA-qwestjazz-rakuten/CDN/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=qwestjazz-rakuten.amagi.tv&sz=128",
            description = "Qwest Jazz Canlı Yayın"
        ),
        CanliKanal(
            id = "deluxe-music-dance",
            name = "DELUXE MUSIC Dance",
            category = "radyo",
            streamUrl = "https://d37i8mvsaxucfl.cloudfront.net/v1/master/3722c60a815c199d9c0ef36c5b73da68a62b09d1/cc-0785d94zsjwwd/index.m3u8?uid=%7BPSID%7D&optout=%7BTARGETOPT%7D&country=DE&vendor=Samsung",
            posterUrl = "https://www.google.com/s2/favicons?domain=d37i8mvsaxucfl.cloudfront.net&sz=128",
            description = "DELUXE MUSIC Dance Canlı Yayın"
        ),
        CanliKanal(
            id = "movie-music-by-stingray",
            name = "Movie Music by Stingray",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/cmusic-cme004-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Movie Music by Stingray Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-classica",
            name = "Stingray Classica",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/classica-cla008-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Classica Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-iconcerts",
            name = "Stingray iConcerts",
            category = "radyo",
            streamUrl = "https://dash4.antik.sk/live/test_stingray_iconcerts_hd_hevc50/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=dash4.antik.sk&sz=128",
            description = "Stingray iConcerts Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-djazz",
            name = "Stingray DJAZZ",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/djazz-djaads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray DJAZZ Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-alternative",
            name = "Stingray Alternative",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-102ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Alternative Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-easy-listening",
            name = "Stingray Easy Listening",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-137ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Easy Listening Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-flashback-70-s",
            name = "Stingray Flashback 70's",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-115ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Flashback 70's Canlı Yayın"
        ),
        CanliKanal(
            id = "that-s-70s",
            name = "that's 70s",
            category = "radyo",
            streamUrl = "https://lightning-now70s-samsungnz.amagi.tv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lightning-now70s-samsungnz.amagi.tv&sz=128",
            description = "that's 70s Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-remember-the-80s",
            name = "Stingray Remember the 80s",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-128ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Remember the 80s Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-classic-rock",
            name = "Stingray Classic Rock",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-101ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Classic Rock Canlı Yayın"
        ),
        CanliKanal(
            id = "qello-concerts-by-stingray",
            name = "Qello Concerts by Stingray",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/qello-qello001-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Qello Concerts by Stingray Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-holidayscapes",
            name = "Stingray Holidayscapes",
            category = "radyo",
            streamUrl = "https://plextv.ott-channels.stingray.com/holidayscapes/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=plextv.ott-channels.stingray.com&sz=128",
            description = "Stingray Holidayscapes Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-nothin-but-90s",
            name = "Stingray Nothin' But 90s",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-142ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Nothin' But 90s Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-hip-hop-r-b",
            name = "Stingray Hip-Hop/R&B",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-133ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Hip-Hop/R&B Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-greatest-hits",
            name = "Stingray Greatest Hits",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-155ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Greatest Hits Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-exitos-del-momento",
            name = "Stingray Exitos del Momento",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-190ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Exitos del Momento Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-karaoke",
            name = "Stingray Karaoke",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/karaoke-kar000-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Karaoke Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-pop-adult",
            name = "Stingray Pop Adult",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-104ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Pop Adult Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-romance-latino",
            name = "Stingray Romance Latino",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-202ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Romance Latino Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-smooth-jazz",
            name = "Stingray Smooth Jazz",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-140ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Smooth Jazz Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-hit-list",
            name = "Stingray Hit List",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-107ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Hit List Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-today-s-k-pop",
            name = "Stingray Today's K-Pop",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-317ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Today's K-Pop Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-the-spa",
            name = "Stingray The Spa",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-122ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray The Spa Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-hot-country",
            name = "Stingray Hot Country",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-108ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Hot Country Canlı Yayın"
        ),
        CanliKanal(
            id = "stingray-soul-storm",
            name = "Stingray Soul Storm",
            category = "radyo",
            streamUrl = "https://lotus.stingray.com/manifest/ose-134ads-montreal/samsungtvplus/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lotus.stingray.com&sz=128",
            description = "Stingray Soul Storm Canlı Yayın"
        ),
        CanliKanal(
            id = "red-bull-tv",
            name = "Red Bull TV",
            category = "spor",
            streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=rbmn-live.akamaized.net&sz=128",
            description = "Red Bull TV Canlı Yayın"
        ),
        CanliKanal(
            id = "trace-sports",
            name = "Trace Sports",
            category = "spor",
            streamUrl = "https://lightning-tracesport-samsungau.amagi.tv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lightning-tracesport-samsungau.amagi.tv&sz=128",
            description = "Trace Sports Canlı Yayın"
        ),
        CanliKanal(
            id = "abc-news-live",
            name = "ABC News Live",
            category = "haber",
            streamUrl = "https://live-manifest.production-public.tubi.io/live/14e4f5f2-9787-4c49-8554-f2ad109764a7/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=live-manifest.production-public.tubi.io&sz=128",
            description = "ABC News Live Canlı Yayın"
        ),
    )

    fun findById(id: String): CanliKanal? = kanallar.firstOrNull { it.id == id }

    fun findByName(name: String): CanliKanal? = kanallar.firstOrNull {
        it.name.equals(name, ignoreCase = true) || it.name.contains(name, ignoreCase = true)
    }
}
