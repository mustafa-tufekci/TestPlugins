package com.panates

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class CizgiMaxPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(CizgiMax())
        registerExtractorAPI(CmxSibNet())
        registerExtractorAPI(CmxDrive())
        registerExtractorAPI(CmxCizgiDuo())
        registerExtractorAPI(CmxCizgiPass())
    }
}
