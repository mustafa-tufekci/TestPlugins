package com.panates

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class W2mPlugin: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(W2m())
        registerExtractorAPI(W2mExtractor("https://movies2watch.watch/", context))
        registerExtractorAPI(W2mExtractor("https://0123movie.space/",    context))
        registerExtractorAPI(W2mExtractor("https://player.videasy.to/",  context))
        registerExtractorAPI(W2mExtractor("https://vidfast.pro/",        context))
        registerExtractorAPI(W2mExtractor("https://vidsrc.cc/",          context))
    }
}
