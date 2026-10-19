version = 1

cloudstream {
    description = "YouTube - Video, kanal, oynatma listesi ve canlı yayınlar"
    authors = listOf("KaifTaufiq", "Panates")
    status = 1
    tvTypes = listOf("Other", "Live", "TvSeries")
    iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/09/YouTube_full-color_icon_%282017%29.svg/3840px-YouTube_full-color_icon_%282017%29.svg.png"
    language = "en"
}

dependencies {
    implementation("com.github.teamnewpipe:NewPipeExtractor:v0.25.2")
}
