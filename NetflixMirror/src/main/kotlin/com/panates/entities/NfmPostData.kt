package com.panates.entities

data class NfmPostData(
    val desc: String?,
    val director: String?,
    val ua: String?,
    val episodes: List<NfmEpisode?>,
    val genre: String?,
    val nextPage: Int?,
    val nextPageSeason: String?,
    val nextPageShow: Int?,
    val season: List<NfmSeason>?,
    val title: String,
    val year: String,
    val cast: String?,
    val match: String?,
    val runtime: String?,
    val suggest: List<NfmId>?,
)

data class NfmId(val id: String)