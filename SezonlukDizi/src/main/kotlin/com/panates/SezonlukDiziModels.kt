package com.panates

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * ajax/dataAlternatif{N}.asp response — list of embed sources.
 */
data class Kaynak(
    @JsonProperty("status") val status: String,
    @JsonProperty("data") val data: List<Veri> = emptyList()
)

/**
 * A single embed source.
 */
data class Veri(
    @JsonProperty("baslik") val baslik: String,
    @JsonProperty("id") val id: Int,
    @JsonProperty("kalite") val kalite: Int? = null
)

/**
 * Version numbers scraped from /js/site.min.js
 * (dataAlternatif{N}.asp / dataEmbed{N}.asp).
 */
data class AspData(
    val alternatif: String,
    val embed: String
)

/**
 * Parsed `sources:[{file:"..."}]` payload of streamruby players.
 */
data class Ruby(
    @JsonProperty("file") val file: String
)

/**
 * ajax/arama.asp search response.
 */
data class SearchApiResponse(
    @JsonProperty("status") val status: String?,
    @JsonProperty("results") val results: SearchApiResults?
)

data class SearchApiResults(
    @JsonProperty("diziler") val diziler: SearchApiCategory?
)

data class SearchApiCategory(
    @JsonProperty("results") val results: List<SearchApiItem>?
)

data class SearchApiItem(
    @JsonProperty("did") val did: Int?,
    @JsonProperty("title") val title: String?,
    @JsonProperty("url") val url: String?,
    @JsonProperty("image") val image: String?,
    @JsonProperty("imdb") val imdb: Any?
)
