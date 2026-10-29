package com.panates.entities

data class NfmEpisodesData(
    val episodes: List<NfmEpisode>?,
    val nextPage: Int,
    val nextPageSeason: String,
    val nextPageShow: Int,
)