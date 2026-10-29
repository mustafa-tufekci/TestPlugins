package com.panates

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class UgfPlugin: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(Ugf())
        registerExtractorAPI(UgfMailRu())
        registerExtractorAPI(UgfOdnoklassniki())
    }
}
