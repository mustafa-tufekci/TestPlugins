package com.panates

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * A player source entry from the episode page dropdown
 * (`select.woca-linkpages-dd option`).
 */
data class VideoSource(
    val name: String,
    val url: String
)

/**
 * CryptoJS.AES.decrypt("<cipherText>","<password>") payload
 * found inside molystream embed pages.
 */
data class CryptoPayload(
    val cipherText: String,
    val password: String
)

/**
 * Dave's WordPress Live Search (dwls_search) AJAX response
 * (`wp-admin/admin-ajax.php?s=..&action=dwls_search`).
 */
data class DbxAjaxSearchResponse(
    @JsonProperty("results") val results: List<DbxAjaxSearchResult> = emptyList()
)

data class DbxAjaxSearchResult(
    @JsonProperty("post_title") val postTitle: String = "",
    @JsonProperty("permalink") val permalink: String = "",
    @JsonProperty("attachment_thumbnail") val attachmentThumbnail: String? = null
)
