package com.panates

import com.fasterxml.jackson.annotation.JsonProperty


data class UgfAjaxSource(
    @JsonProperty("status")      val status: String,
    @JsonProperty("iframe")      val iframe: String,
    @JsonProperty("alternative") val alternative: String,
)
