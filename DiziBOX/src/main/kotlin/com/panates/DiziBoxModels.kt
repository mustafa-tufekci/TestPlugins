package com.panates

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
