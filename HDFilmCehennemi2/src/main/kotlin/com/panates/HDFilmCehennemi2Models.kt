package com.panates

import com.fasterxml.jackson.annotation.JsonProperty

data class Hdc2SearchItem(
    @JsonProperty("id") val id: String?,
    @JsonProperty("title") val title: String?,
    @JsonProperty("slug") val slug: String?,
    @JsonProperty("contentableType") val contentableType: String?,
    @JsonProperty("posterUrl") val posterUrl: String?,
    @JsonProperty("releaseYear") val releaseYear: String?,
    @JsonProperty("imdbRating") val imdbRating: Double?
)

data class Hdc2LoadMoreResponse(
    @JsonProperty("html") val html: String?
)
