package com.panates

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class DbxPlugin: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(Dbx())
    }
}