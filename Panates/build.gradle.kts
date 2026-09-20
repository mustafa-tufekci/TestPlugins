version = 2

cloudstream {
    description = "Multi-source Turkish series plugin (SezonlukDizi, etc.)"
    authors = listOf("Panates")
    status = 1
    tvTypes = listOf("TvSeries", "Anime")
    language = "tr"
    iconUrl = "https://sezonlukdizi.cc/i/logo.png"
}

android {
    buildFeatures {
        buildConfig = true
    }
}
