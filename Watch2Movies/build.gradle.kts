version = 3

cloudstream {
    authors     = listOf("Panates", "NeO")
    language    = "en"
    description = "Watch2Movies - Filmlere ve dizilere reklamsız, üyeliksiz erişim."

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 3
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=movies2watch.watch&sz=%size%"
}

dependencies {
    // Needed for runBlocking/Dispatchers/delay/withContext in W2mExtractor; provided by the host
    // app at runtime, not bundled into the .cs3 (same as NiceHttp/jsoup/jackson).
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}
