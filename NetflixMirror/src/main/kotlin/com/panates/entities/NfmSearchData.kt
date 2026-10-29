package com.panates.entities

data class NfmSearchData(
    val head: String,
    val searchResult: List<NfmSearchResult>,
    val type: Int
)