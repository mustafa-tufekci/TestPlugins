version = 1

cloudstream {
    authors     = listOf("Panates", "NeO")
    language    = "hi"
    description = "Netflix, PrimeVideo Content in Multiple Languages"

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 3
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=iosmirror.cc&sz=%size%"
}

dependencies {
    // Needed for kotlinx.coroutines.delay in the verification bypass loop; provided by
    // the host app at runtime, not bundled into the .cs3 (same as NiceHttp/jsoup/jackson).
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}