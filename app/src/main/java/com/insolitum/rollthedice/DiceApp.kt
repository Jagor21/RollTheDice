package com.insolitum.rollthedice

import android.app.Application
import com.kochava.tracker.Tracker
import com.kochava.tracker.log.LogLevel

class DiceApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Tracker.getInstance().startWithAppGuid(this, getString(R.string.app_guid_kochava))
        Tracker.getInstance().setLogLevel(LogLevel.DEBUG)
        val currentInstallAttribution = Tracker.getInstance().installAttribution
        if(!currentInstallAttribution.isRetrieved) {
            Tracker.getInstance().retrieveInstallAttribution { installAttribution ->
                val pref = this.getSharedPref()
                val editor = pref.edit()
                installAttribution.toJson()
                editor.putString(INSTALL_ATTRIBUTION_SP_KEY, installAttribution.toJson().toString())
                editor.commit()
            }
        }
    }
}