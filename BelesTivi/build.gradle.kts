version = 1

cloudstream {
    authors     = listOf("Panates")
    language    = "tr"
    description = "Beleş TiVi uygulamasından alınan canlı TV kanalları. Spor, haber, belgesel, müzik, çocuk ve 7/24 dizi/film kanalları. Kaynak: Beleş TiVi Android uygulaması (com.sunargames.belestivi)."

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 1
    tvTypes = listOf("Live")
    iconUrl = "https://raw.githubusercontent.com/recloudstream/cloudstream-icons/master/icons/live.png"
}