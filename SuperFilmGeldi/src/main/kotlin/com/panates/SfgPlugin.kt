package com.panates

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class SfgPlugin: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(Sfg())
        registerExtractorAPI(SfgMixPlayHd())
        registerExtractorAPI(SfgMixTiger())
    }
}
