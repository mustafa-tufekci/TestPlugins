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
        CanliKanal(
            id = "trt-spor-blutv",
            name = "TRT Spor (BluTV)",
            category = "spor",
            streamUrl = "https://mn-nl.mncdn.com/blutv_trtspor2/live.m3u8",
            posterUrl = "https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/TRT_Spor_logo.svg/120px-TRT_Spor_logo.svg.png",
            description = "TRT Spor BluTV Canlı Yayın"
        ),
        CanliKanal(
            id = "trt-spor-yildiz-blutv",
            name = "TRT Spor Yıldız (BluTV)",
            category = "spor",
            streamUrl = "https://mn-nl.mncdn.com/blutv_trtsporyildiz/live.m3u8",
            posterUrl = "https://thumb.wikimedia.org/wikipedia/commons/thumb/c/ce/TRT_Spor_Y%C4%B1ld%C4%B1z_Logo.svg/120px-TRT_Spor_Y%C4%B1ld%C4%B1z_Logo.svg.png",
            description = "TRT Spor Yıldız BluTV Canlı Yayın"
        ),
        CanliKanal(
            id = "aspor",
            name = "a Spor",
            category = "spor",
            streamUrl = "https://trkvz-live.daioncdn.net/aspor/aspor.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=aspor.com.tr&sz=128",
            description = "a Spor Canlı Yayın"
        ),
        CanliKanal(
            id = "sports-tv",
            name = "Sports TV",
            category = "spor",
            streamUrl = "https://mn-nl.mncdn.com/blutv_sportstv2/live_720p2000000kbps/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=sportstv.com.tr&sz=128",
            description = "Sports TV Canlı Yayın"
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
            id = "atv",
            name = "ATV",
            category = "ulusal",
            streamUrl = "https://trkvz-live.daioncdn.net/atv/atv.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=atv.com.tr&sz=128",
            description = "ATV HD Canlı Yayın"
        ),
        CanliKanal(
            id = "atv-avrupa",
            name = "ATV Avrupa",
            category = "ulusal",
            streamUrl = "http://antalya.tvizlehd.com/live/atvavrupa.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=atv.com.tr&sz=128",
            description = "ATV Avrupa Canlı Yayın"
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
            id = "show-turk",
            name = "Show Türk",
            category = "ulusal",
            streamUrl = "https://mn-nl.mncdn.com/blutv_showturk2/live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=showtv.com.tr&sz=128",
            description = "Show Türk Canlı Yayın"
        ),
        CanliKanal(
            id = "show-max",
            name = "Show Max",
            category = "ulusal",
            streamUrl = "https://mn-nl.mncdn.com/blutv_showmax2/live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=showtv.com.tr&sz=128",
            description = "Show Max Canlı Yayın"
        ),
        CanliKanal(
            id = "star-tv",
            name = "Star TV",
            category = "ulusal",
            streamUrl = "https://mn-nl.mncdn.com/blutv_startv2/live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=startv.com.tr&sz=128",
            description = "Star TV HD Canlı Yayın"
        ),
        CanliKanal(
            id = "eurostar",
            name = "EuroStar",
            category = "ulusal",
            streamUrl = "https://mn-nl.mncdn.com/blutv_eurostar2/live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=startv.com.tr&sz=128",
            description = "EuroStar Canlı Yayın"
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
            id = "euro-d",
            name = "Euro D",
            category = "ulusal",
            streamUrl = "https://mn-nl.mncdn.com/blutv_eurod2/live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=kanald.com.tr&sz=128",
            description = "Euro D Canlı Yayın"
        ),
        CanliKanal(
            id = "fox-tv",
            name = "FOX",
            category = "ulusal",
            streamUrl = "https://mn-nl.mncdn.com/blutv_foxtv2/live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=fox.com.tr&sz=128",
            description = "FOX TV Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-7",
            name = "Kanal 7",
            category = "ulusal",
            streamUrl = "https://live.kanal7.com/live/kanal7LiveDesktop/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=kanal7.com&sz=128",
            description = "Kanal 7 HD Canlı Yayın"
        ),
        CanliKanal(
            id = "kanal-7-avrupa",
            name = "Kanal 7 Avrupa",
            category = "ulusal",
            streamUrl = "https://live.kanal7.com/live/kanal7AvrupaLive/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=kanal7.com&sz=128",
            description = "Kanal 7 Avrupa Canlı Yayın"
        ),
        CanliKanal(
            id = "tv8",
            name = "TV8",
            category = "ulusal",
            streamUrl = "https://tv8.daioncdn.net/tv8/tv8.m3u8?app=tv8_web&ce=3",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv8.com.tr&sz=128",
            description = "TV8 HD Canlı Yayın"
        ),
        CanliKanal(
            id = "tv8-int",
            name = "TV8 INT",
            category = "ulusal",
            streamUrl = "https://mn-nl.mncdn.com/blutv_tv8_int2/live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv8.com.tr&sz=128",
            description = "TV8 INT Canlı Yayın"
        ),
        CanliKanal(
            id = "tv8-5",
            name = "TV8.5",
            category = "ulusal",
            streamUrl = "https://mn-nl.mncdn.com/blutv_tv8_52/live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tv8.com.tr&sz=128",
            description = "TV8.5 Canlı Yayın"
        ),
        CanliKanal(
            id = "teve2",
            name = "Teve2",
            category = "ulusal",
            streamUrl = "https://mn-nl.mncdn.com/blutv_teve22/live_720p2000000kbps/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=teve2.com.tr&sz=128",
            description = "Teve2 Canlı Yayın"
        ),
        CanliKanal(
            id = "360-tv",
            name = "360 TV",
            category = "ulusal",
            streamUrl = "https://turkmedya-live.ercdn.net/tv360/tv360_720p.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=360.com.tr&sz=128",
            description = "360 TV Canlı Yayın"
        ),
        CanliKanal(
            id = "beyaz-tv",
            name = "Beyaz TV",
            category = "ulusal",
            streamUrl = "https://mn-nl.mncdn.com/blutv_beyaztv2/live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=beyaztv.com.tr&sz=128",
            description = "Beyaz TV Canlı Yayın"
        ),
        CanliKanal(
            id = "a2-tv",
            name = "A2",
            category = "ulusal",
            streamUrl = "http://antalya.tvizlehd.com/live/a2tv.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=atv.com.tr&sz=128",
            description = "A2 Canlı Yayın"
        ),
        CanliKanal(
            id = "afyon-turk-tv",
            name = "Afyon Türk TV",
            category = "ulusal",
            streamUrl = "https://5be5d840359c6.streamlock.net/afyonturktv/afyonturktv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=afyonturktv.com&sz=128",
            description = "Afyon Türk TV Canlı Yayın"
        ),
        CanliKanal(
            id = "cine5-tv",
            name = "Cine5 TV",
            category = "ulusal",
            streamUrl = "https://host.onlineradyotv.net:8081/cine5/tracks-v1a1/mono.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=cine5.com.tr&sz=128",
            description = "Cine5 TV Canlı Yayın"
        ),
        CanliKanal(
            id = "minika-cocuk",
            name = "Minika Çocuk",
            category = "ulusal",
            streamUrl = "https://antalya.tvizlehd.com/live/minikacocuk.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=minika.com.tr&sz=128",
            description = "Minika Çocuk Canlı Yayın"
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

        // --- Haber Kanalları ---
        CanliKanal(
            id = "trt-haber",
            name = "TRT Haber",
            category = "haber",
            streamUrl = "https://tv-trthaber.medya.trt.com.tr/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=trthaber.com&sz=128",
            description = "TRT Haber HD Canlı Yayın"
        ),
        CanliKanal(
            id = "a-haber",
            name = "A Haber",
            category = "haber",
            streamUrl = "http://antalya.tvizlehd.com/live/ahaber.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=ahaber.com.tr&sz=128",
            description = "A Haber Canlı Yayın"
        ),
        CanliKanal(
            id = "tvnet",
            name = "TVNET",
            category = "haber",
            streamUrl = "https://mn-nl.mncdn.com/tvnet/tvnet/chunklist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tvnet.com.tr&sz=128",
            description = "TVNET Canlı Yayın"
        ),
        CanliKanal(
            id = "ntv",
            name = "NTV",
            category = "haber",
            streamUrl = "https://mn-nl.mncdn.com/blutv_ntv_dvr/live_720p2000000/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=ntv.com.tr&sz=128",
            description = "NTV Canlı Yayın"
        ),
        CanliKanal(
            id = "tgrt-haber",
            name = "TGRT Haber",
            category = "haber",
            streamUrl = "https://mn-nl.mncdn.com/blutv_tgrthaber2/live_720p2000000kbps/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tgrthaber.com.tr&sz=128",
            description = "TGRT Haber Canlı Yayın"
        ),
        CanliKanal(
            id = "haber-global",
            name = "Haber Global",
            category = "haber",
            streamUrl = "https://mn-nl.mncdn.com/blutv_haberglobal_dvr/live_720p2000000/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=haberglobal.com.tr&sz=128",
            description = "Haber Global Canlı Yayın"
        ),
        CanliKanal(
            id = "haberturk",
            name = "Habertürk",
            category = "haber",
            streamUrl = "https://mn-nl.mncdn.com/blutv_haberturk_dvr/live_720p2000000/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=haberturk.com&sz=128",
            description = "Habertürk Canlı Yayın"
        ),
        CanliKanal(
            id = "bloomberg-ht",
            name = "Bloomberg HT",
            category = "haber",
            streamUrl = "https://ciner.daioncdn.net/bloomberght/bloomberght_720p.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=bloomberght.com&sz=128",
            description = "Bloomberg HT Canlı Yayın"
        ),
        CanliKanal(
            id = "24-tv",
            name = "24 TV",
            category = "haber",
            streamUrl = "https://mn-nl.mncdn.com/kanal24/smil:kanal24.smil/chunklist_b896000.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=24tv.com.tr&sz=128",
            description = "24 TV Canlı Yayın"
        ),
        CanliKanal(
            id = "cnn-turk",
            name = "CNN Türk",
            category = "haber",
            streamUrl = "https://live.duhnet.tv/S2/HLS_LIVE/cnnturknp/track_4_1000/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=cnnturk.com&sz=128",
            description = "CNN Türk Canlı Yayın"
        ),
        CanliKanal(
            id = "halk-tv",
            name = "Halk TV",
            category = "haber",
            streamUrl = "https://mn-nl.mncdn.com/blutv_halktv2/live_720p2000000kbps/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=halktv.com.tr&sz=128",
            description = "Halk TV Canlı Yayın"
        ),
        CanliKanal(
            id = "akit-tv",
            name = "Akit TV",
            category = "haber",
            streamUrl = "https://akittv-live.ercdn.net/akittv/akittv_720p.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=akit.tv&sz=128",
            description = "Akit TV Canlı Yayın"
        ),
        CanliKanal(
            id = "tele-1",
            name = "Tele 1",
            category = "haber",
            streamUrl = "https://mn-nl.mncdn.com/blutv_tele1_dvr/live_720p2000000/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tele1.com.tr&sz=128",
            description = "Tele 1 Canlı Yayın"
        ),

        // --- Haber & Belgesel ---
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
        CanliKanal(
            id = "tgrt-belgesel",
            name = "TGRT Belgesel",
            category = "haber",
            streamUrl = "https://b01c02nl.mediatriple.net/videoonlylive/mtsxxkzwwuqtglive/broadcast_5fe4598be8e5d.smil/chunklist_b600000.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tgrtbelgesel.com.tr&sz=128",
            description = "TGRT Belgesel Canlı Yayın"
        ),
        CanliKanal(
            id = "investigation-discovery",
            name = "Investigation Discovery",
            category = "haber",
            streamUrl = "https://dnx8nkmlprapr.cloudfront.net/out/v1/c9e1c183395d4d29a9fa34ecdef4163b/master.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=discovery.com&sz=128",
            description = "Investigation Discovery Canlı Yayın"
        ),
        CanliKanal(
            id = "blutv-belgesel",
            name = "Blutv Belgesel",
            category = "haber",
            streamUrl = "https://d1grcygon2cylc.cloudfront.net/out/v1/39e430e8c1ce4a8d8176c7be8c743529/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=blutv.com&sz=128",
            description = "Blutv Belgesel Canlı Yayın"
        ),
        CanliKanal(
            id = "yaban-tv",
            name = "Yaban TV",
            category = "haber",
            streamUrl = "https://59cba4d34b678.streamlock.net/live1/yabantv/playlist.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=yabantv.com.tr&sz=128",
            description = "Yaban TV Canlı Yayın"
        ),

        // --- Dini Kanallar ---
        CanliKanal(
            id = "diyanet-tv",
            name = "Diyanet TV",
            category = "dini",
            streamUrl = "https://eustr73.mediatriple.net/videoonlylive/mtikoimxnztxlive/broadcast_5e3bf95a47e07.smil/rz1dvIHz4QFoG_0.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=diyanet.tv&sz=128",
            description = "Diyanet TV Canlı Yayın"
        ),
        CanliKanal(
            id = "lalegul-tv",
            name = "Lalegül TV",
            category = "dini",
            streamUrl = "https://lbl.netmedya.net/hls/lalegultv.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=lalegultv.com.tr&sz=128",
            description = "Lalegül TV Canlı Yayın"
        ),
        CanliKanal(
            id = "semerkand-tv",
            name = "Semerkand TV",
            category = "dini",
            streamUrl = "https://b01c02nl.mediatriple.net/videoonlylive/mtisvwurbfcyslive/broadcast_58d915bd40efc.smil/chunklist_b500000.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=semerkandtv.com.tr&sz=128",
            description = "Semerkand TV Canlı Yayın"
        ),
        CanliKanal(
            id = "dost-tv",
            name = "DOST TV",
            category = "dini",
            streamUrl = "http://dost.stream.emsal.im/tv/live.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=dosttv.com.tr&sz=128",
            description = "DOST TV Canlı Yayın"
        ),

        // --- Eğlence & Müzik ---
        CanliKanal(
            id = "tlc",
            name = "TLC",
            category = "eglence",
            streamUrl = "https://mn-nl.mncdn.com/blutv_tlc_dvr/live_720p2000000/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=tlc.com.tr&sz=128",
            description = "TLC Canlı Yayın"
        ),
        CanliKanal(
            id = "dmax",
            name = "DMAX",
            category = "eglence",
            streamUrl = "https://mn-nl.mncdn.com/blutv_dmax_dvr/live_720p2000000/index.m3u8",
            posterUrl = "https://www.google.com/s2/favicons?domain=dmax.com.tr&sz=128",
            description = "DMAX Canlı Yayın"
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
        )
    )

    fun findById(id: String): CanliKanal? = kanallar.firstOrNull { it.id == id }

    fun findByName(name: String): CanliKanal? = kanallar.firstOrNull {
        it.name.equals(name, ignoreCase = true) || it.name.contains(name, ignoreCase = true)
    }
}