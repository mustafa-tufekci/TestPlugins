version = 2

cloudstream {
    authors     = listOf("Panates", "NeO")
    language    = "en"
    description = "!!! YAPIM AŞAMASINDA !!! Watch2Movies is a Free Movies streaming site with zero ads. We let you watch movies online without having to register or paying, with over 10000 movies and TV-Series. You can also Download full movies from Watch2Movies and watch it later if you want."

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 0
    tvTypes = listOf("Movie")
    iconUrl = "https://www.google.com/s2/favicons?domain=watch2movies.net&sz=%size%"
}

dependencies {
    // Needed for runBlocking/Dispatchers/delay/withContext in W2mExtractor; provided by the host
    // app at runtime, not bundled into the .cs3 (same as NiceHttp/jsoup/jackson).
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}
