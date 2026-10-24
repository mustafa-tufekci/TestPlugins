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
    )

    fun findById(id: String): CanliKanal? = kanallar.firstOrNull { it.id == id }

    fun findByName(name: String): CanliKanal? = kanallar.firstOrNull {
        it.name.equals(name, ignoreCase = true) || it.name.contains(name, ignoreCase = true)
    }
}
