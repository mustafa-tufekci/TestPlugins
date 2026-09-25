version = 16

cloudstream {
    description = "İnatBox - Film, dizi ve canlı yayın kanalları"
    authors = listOf("Panates", "JustRelaxable", "keyiflerolsun")
    status = 1
    tvTypes = listOf("Movie", "TvSeries", "Live")
    language = "tr"
    iconUrl = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEh3vCp6N1K4bECoYRQD-cisJF2_6V_Hk01ZhDmoPR2JuM8O5qr4MqrPO1munM9cRlleBBSK6odYhLtDBWv4E3vhPhynlmS5hVVtJZShHoGA5REQ8_3v8SIlccTEqzVQu2UJyNYQdJNrKIfWy66RQeT0D-CcmFCbHPz5023H6p2v5fv4NVloZ5Rqo_yGrIY/s320/iNat-Box-App.png"
}

dependencies {
    // Needed for Mutex/delay request throttling; provided by the host app at
    // runtime, not bundled into the .cs3 (same as NiceHttp/jsoup/jackson).
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}
