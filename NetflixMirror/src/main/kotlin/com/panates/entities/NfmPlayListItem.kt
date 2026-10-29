package com.panates.entities

data class NfmPlayListItem(
    val image: String,
    val sources: List<NfmSource>,
    val tracks: List<NfmTracks>?,
    val title: String
)