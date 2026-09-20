package com.panates

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class PanatesPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(SezonlukDizi())
    }
}
