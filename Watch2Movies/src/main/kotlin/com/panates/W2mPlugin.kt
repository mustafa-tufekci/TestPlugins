package com.panates

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class W2mPlugin: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(W2m())
        registerExtractorAPI(W2mExtractor("https://hanatyury.online/", context))
        registerExtractorAPI(W2mExtractor("https://pepepeyo.xyz/",     context))
        registerExtractorAPI(W2mExtractor("https://zizicoi.online/",   context))
        registerExtractorAPI(W2mExtractor("https://watch2movies.net/", context))
    }
}
