package com.panates

import com.fasterxml.jackson.annotation.JsonProperty

data class SlcSearchData(
    @JsonProperty("state")  val state  : Boolean?           = null,
    @JsonProperty("result") val result : List<SlcSearchItem> = emptyList(),
    @JsonProperty("message") val message : String?          = null,
    @JsonProperty("html")   val html   : String?            = null
)

data class SlcSearchItem(
    @JsonProperty("used_slug")        val slug   : String? = null,
    @JsonProperty("object_name")      val title  : String? = null,
    @JsonProperty("object_poster_url") val poster : String? = null,
    @JsonProperty("imdb_point")       val puan   : String? = null
)

data class SlcContentDetails(
    @JsonProperty("contentItem")    val contentItem : SlcMediaItem,
    @JsonProperty("RelatedResults") val relatedData : SlcRelatedData
)

data class SlcMediaItem(
    @JsonProperty("original_title") val originalTitle : String? = null,
    @JsonProperty("release_year")   val releaseYear   : Int?    = null,
    @JsonProperty("total_minutes")  val totalMinutes  : Int?    = null,
    @JsonProperty("poster_url")     val posterUrl     : String? = null,
    @JsonProperty("description")    val description   : String? = null,
    @JsonProperty("categories")     val categories    : String? = null,
    @JsonProperty("used_slug")      val usedSlug      : String? = null,
    @JsonProperty("imdb_point")     val imdbPoint     : Double? = null
)

data class SlcRelatedData(
    @JsonProperty("getContentTrailers")       val trailers       : SlcTrailerData?     = null,
    @JsonProperty("getMovieCastsById")        val cast           : SlcCastData?        = null,
    @JsonProperty("getMoviePartsById")        val movieParts     : SlcMoviePartsData?  = null,
    @JsonProperty("getSerieSeasonAndEpisodes") val seriesData    : SlcSeriesData?      = null,
    @JsonProperty("getEpisodeSources")        val episodeSources : SlcSourcesData?     = null
)

data class SlcSeriesData(
    @JsonProperty("result") val seasons : List<SlcSeasonItem>? = null
)

data class SlcSeasonItem(
    @JsonProperty("season_no") val seasonNo : Int?               = null,
    @JsonProperty("episodes")  val episodes : List<SlcEpisodeItem>? = null
)

data class SlcEpisodeItem(
    @JsonProperty("episode_no")   val episodeNo : Int?    = null,
    @JsonProperty("episode_text") val epText    : String? = null,
    @JsonProperty("used_slug")    val usedSlug  : String? = null
)

data class SlcSourcesData(
    @JsonProperty("state")  val state  : Boolean?           = null,
    @JsonProperty("result") val result : List<SlcSourceItem>? = null
)

data class SlcSourceItem(
    @JsonProperty("source_content") val sourceContent : String? = null,
    @JsonProperty("quality_name")   val qualityName   : String? = null
)

data class SlcVideoSource(
    val sourceContent : String,
    val quality       : String
)

data class SlcTrailerData(
    @JsonProperty("result") val result : List<SlcTrailer>? = null
)

data class SlcTrailer(
    @JsonProperty("trailer_url") val trailerUrl : String? = null
)

data class SlcCastData(
    @JsonProperty("result") val result : List<SlcCastMember>? = null
)

data class SlcCastMember(
    @JsonProperty("actor_name") val actorName : String? = null
)

data class SlcMoviePartsData(
    @JsonProperty("result") val result : List<SlcMoviePart>? = null
)

data class SlcMoviePart(
    @JsonProperty("original_title") val originalTitle : String? = null,
    @JsonProperty("used_slug")      val usedSlug      : String? = null
)

data class SlcApiResponse(
    @JsonProperty("response") val response : String? = null
)
