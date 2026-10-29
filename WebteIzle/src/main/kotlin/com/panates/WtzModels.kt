package com.panates

import com.fasterxml.jackson.annotation.JsonProperty


data class WtzDataAlternatif(
    @JsonProperty("status") val status: String,
    @JsonProperty("data")   val data: List<WtzEmbedData>,
)


data class WtzEmbedData(
    @JsonProperty("id")       val id: Int,
    @JsonProperty("baslik")   val baslik: String,
    @JsonProperty("kalitesi") val kalitesi: Int,
)

data class WtzTrack(
    @JsonProperty("kind")  val kind: String?,
    @JsonProperty("file")  val file: String?,
    @JsonProperty("label") val label: String?,
)
