package com.panates

data class CanliKanal(
    val id: String,
    val name: String,
    val category: String,
    val streamUrl: String,
    val posterUrl: String? = null,
    val description: String? = null,
    val dynamicResolver: String? = null
)
