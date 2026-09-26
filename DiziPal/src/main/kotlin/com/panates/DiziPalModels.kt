package com.panates

import com.fasterxml.jackson.annotation.JsonProperty

data class DzpSearchEnvelope(
    @JsonProperty("data") val data: DzpSearchData? = null
)

data class DzpSearchData(
    @JsonProperty("state")  val state: Boolean?              = null,
    @JsonProperty("result") val result: List<DzpSearchItem>? = null
)

data class DzpSearchItem(
    @JsonProperty("used_slug")         val usedSlug: String?     = null,
    @JsonProperty("used_type")         val usedType: String?     = null,
    @JsonProperty("object_name")       val objectName: String?   = null,
    @JsonProperty("object_poster_url") val poster: String?       = null,
    @JsonProperty("object_release_year") val year: Int?          = null
)
