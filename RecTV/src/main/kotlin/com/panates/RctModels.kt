package com.panates

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class RctItem(
    @JsonProperty("id")          val id: Int,
    @JsonProperty("type")        val type: String?           = null,
    @JsonProperty("title")       val title: String,
    @JsonProperty("label")       val label: String?          = null,
    @JsonProperty("sublabel")    val sublabel: String?       = null,
    @JsonProperty("description") val description: String?    = null,
    @JsonProperty("year")        val year: Int?              = null,
    @JsonProperty("imdb")        val imdb: Double?           = null,
    @JsonProperty("rating")      val rating: Float?          = null,
    @JsonProperty("duration")    val duration: String?       = null,
    @JsonProperty("image")       val image: String?          = null,
    @JsonProperty("genres")      val genres: List<RctGenre>? = null,
    @JsonProperty("trailer")     val trailer: RctTrailer?    = null,
    @JsonProperty("sources")     val sources: List<RctSource> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RctGenre(
    @JsonProperty("id")    val id: Int,
    @JsonProperty("title") val title: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RctTrailer(
    @JsonProperty("id")    val id: Int?     = null,
    @JsonProperty("type")  val type: String? = null,
    @JsonProperty("url")   val url: String?  = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RctSource(
    @JsonProperty("id")      val id: Int?        = null,
    @JsonProperty("title")   val title: String?  = null,
    @JsonProperty("type")    val type: String?   = null,
    @JsonProperty("url")     val url: String?    = null,
    @JsonProperty("enc_url") val encUrl: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RctSearch(
    @JsonProperty("channels") val channels: List<RctItem>? = emptyList(),
    @JsonProperty("posters")  val posters: List<RctItem>?  = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RctDizi(
    @JsonProperty("id")       val id: Int,
    @JsonProperty("title")    val title: String,
    @JsonProperty("episodes") val episodes: List<RctEpisode> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RctEpisode(
    @JsonProperty("id")       val id: Int,
    @JsonProperty("title")    val title: String,
    @JsonProperty("sources")  val sources: List<RctSource> = emptyList()
)
