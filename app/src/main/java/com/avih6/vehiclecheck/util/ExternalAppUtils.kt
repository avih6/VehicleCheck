package com.avih6.vehiclecheck.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object ExternalAppUtils {
    const val DISABLED_PERMIT_APP_PACKAGE = "com.avih6.disabledpermitcheck"

    /**
     * Opens the dedicated Disabled Permit Check app (בדיקת תו נכה) if installed,
     * or opens its Google Play Store page.
     */
    fun openDisabledPermitApp(context: Context) {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(DISABLED_PERMIT_APP_PACKAGE)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            try {
                val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$DISABLED_PERMIT_APP_PACKAGE")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(playStoreIntent)
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$DISABLED_PERMIT_APP_PACKAGE")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
        }
    }
}
