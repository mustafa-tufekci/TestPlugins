version = 39

cloudstream {
    authors     = listOf("Panates", "hexated", "keyiflerolsun", "nikyokki")
    language    = "tr"
    description = "Türkiye'nin en hızlı hd film izleme sitesi"

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 1
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=hdfilmcehennemi.com&sz=%size%"
}

dependencies {
    // Rhino (JS engine) is provided by the CloudStream app at runtime; needed only to compile.
    compileOnly("org.mozilla:rhino:1.8.1")
}
