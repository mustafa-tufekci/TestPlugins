version = 3
cloudstream {
    authors     = listOf("Panates", "NeO")
    language    = "tr"
    description = "Film Makinesi, en yeni ve en güncel filmleri sitemizde full HD kalite farkı ile izleyebilirsiniz. HD film izle denildiğinde akla gelen en kaliteli film izleme sitesi."

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 3
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=filmmakinesi.to&sz=%size%"
}

dependencies {
    // Rhino (JS engine) is provided by the CloudStream app at runtime; needed only to compile.
    compileOnly("org.mozilla:rhino:1.8.1")
}
