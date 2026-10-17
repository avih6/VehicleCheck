package com.avih6.vehiclecheck.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.avih6.vehiclecheck.BuildConfig
import com.avih6.vehiclecheck.R
import com.avih6.vehiclecheck.data.CarGalleryImage

object ReportUtils {
    const val SUPPORT_EMAIL = "av6development@gmail.com"

    private fun getAppAndDeviceInfo(context: Context): AppAndDeviceInfo {
        val packageInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) { null }
        val versionName = packageInfo?.versionName ?: BuildConfig.VERSION_NAME
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.longVersionCode ?: BuildConfig.VERSION_CODE.toLong()
        } else {
            @Suppress("DEPRECATION")
            (packageInfo?.versionCode?.toLong() ?: BuildConfig.VERSION_CODE.toLong())
        }
        val appName = context.getString(R.string.app_name)
        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
        val osVersion = Build.VERSION.RELEASE
        val apiLevel = Build.VERSION.SDK_INT.toString()

        return AppAndDeviceInfo(
            appName = appName,
            versionName = versionName,
            versionCode = versionCode.toString(),
            osVersion = osVersion,
            apiLevel = apiLevel,
            deviceModel = deviceModel
        )
    }

    private data class AppAndDeviceInfo(
        val appName: String,
        val versionName: String,
        val versionCode: String,
        val osVersion: String,
        val apiLevel: String,
        val deviceModel: String
    )

    fun sendVehicleDataReport(
        context: Context,
        formattedPlate: String,
        make: String,
        model: String,
        year: String,
        trimOrCommercial: String
    ) {
        val info = getAppAndDeviceInfo(context)
        val subject = context.getString(R.string.report_email_subject, info.appName, formattedPlate)
        val body = context.getString(
            R.string.report_email_body,
            formattedPlate,
            listOf(make, model).filter { it.isNotBlank() }.joinToString(" "),
            year.ifBlank { "-" },
            trimOrCommercial.ifBlank { "-" },
            info.appName,
            info.versionName,
            info.versionCode,
            info.osVersion,
            info.apiLevel,
            info.deviceModel
        )

        dispatchEmail(context, subject, body, context.getString(R.string.report_email_chooser))
    }

    fun sendImageReport(
        context: Context,
        image: CarGalleryImage,
        vehicleOrSearchContext: String
    ) {
        val info = getAppAndDeviceInfo(context)
        val subjectContext = vehicleOrSearchContext.ifBlank { image.title }
        val subject = context.getString(R.string.report_image_email_subject, info.appName, subjectContext)

        val directUrl = image.imageUrl.ifBlank { image.thumbUrl }
        val sourceUrl = image.descriptionUrl.ifBlank { directUrl }

        val body = context.getString(
            R.string.report_image_email_body,
            image.title.ifBlank { "-" },
            directUrl.ifBlank { "-" },
            sourceUrl.ifBlank { "-" },
            vehicleOrSearchContext.ifBlank { "-" },
            info.appName,
            info.versionName,
            info.versionCode,
            info.osVersion,
            info.apiLevel,
            info.deviceModel
        )

        dispatchEmail(context, subject, body, context.getString(R.string.report_image_email_chooser))
    }

    private fun dispatchEmail(context: Context, subject: String, body: String, chooserTitle: String) {
        val mailtoUri = Uri.parse(
            "mailto:$SUPPORT_EMAIL" +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(body)
        )
        val emailIntent = Intent(Intent.ACTION_SENDTO, mailtoUri).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            val chooser = Intent.createChooser(emailIntent, chooserTitle)
            context.startActivity(chooser)
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                context.startActivity(Intent.createChooser(fallbackIntent, chooserTitle))
            } catch (_: Exception) {
                Toast.makeText(context, context.getString(R.string.report_email_no_app), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
