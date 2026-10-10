package com.panates

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class DizillaPlugin: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(Dizilla())
        registerExtractorAPI(Hotlinger())
        registerExtractorAPI(PlayRu())
        registerExtractorAPI(FourPichive())
        registerExtractorAPI(Pichive())
    }
}