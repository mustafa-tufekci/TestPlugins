package com.panates

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class RfmPlugin: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(Rfm())
        registerExtractorAPI(RfmOdnoklassniki())
        registerExtractorAPI(RfmOkRuSsl())
        registerExtractorAPI(RfmOkRuHttp())
    }
}
