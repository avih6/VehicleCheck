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

    private fun isEnglishLocale(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].language == "en"
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale.language == "en"
        }
    }

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
        val makeModelStr = listOf(make, model).filter { it.isNotBlank() }.joinToString(" ")

        val body = context.getString(
            R.string.report_email_body,
            formattedPlate,
            makeModelStr,
            year.ifBlank { "-" },
            trimOrCommercial.ifBlank { "-" },
            info.appName,
            info.versionName,
            info.versionCode,
            info.osVersion,
            info.apiLevel,
            info.deviceModel
        )

        val isEn = isEnglishLocale(context)
        val htmlBody = if (isEn) {
            """
                <p><br/><br/><br/></p>
                <p>--- Please write your message above this line (describe what is incorrect) ---</p>
                <br/>
                <h3>--- Vehicle Details ---</h3>
                <p><b>License Plate:</b> $formattedPlate</p>
                <p><b>Make &amp; Model:</b> $makeModelStr</p>
                <p><b>Year:</b> ${year.ifBlank { "-" }}</p>
                <p><b>Trim / Commercial Name:</b> ${trimOrCommercial.ifBlank { "-" }}</p>
                <br/>
                <h3>--- System Information ---</h3>
                <p><b>App Name:</b> ${info.appName}</p>
                <p><b>App Version:</b> ${info.versionName} (${info.versionCode})</p>
                <p><b>Android OS:</b> ${info.osVersion} (API ${info.apiLevel})</p>
                <p><b>Device:</b> ${info.deviceModel}</p>
            """.trimIndent()
        } else {
            """
                <p><br/><br/><br/></p>
                <p>--- כתבו את הודעתכם מעל קו זה (נא לפרט מה המידע השגוי) ---</p>
                <br/>
                <h3>--- פרטי הרכב ---</h3>
                <p><b>מספר רכב:</b> $formattedPlate</p>
                <p><b>יצרן ודגם:</b> $makeModelStr</p>
                <p><b>שנת ייצור:</b> ${year.ifBlank { "-" }}</p>
                <p><b>כינוי מסחרי / רמת גימור:</b> ${trimOrCommercial.ifBlank { "-" }}</p>
                <br/>
                <h3>--- מידע טכני ---</h3>
                <p><b>שם האפליקציה:</b> ${info.appName}</p>
                <p><b>גרסת אפליקציה:</b> ${info.versionName} (${info.versionCode})</p>
                <p><b>גרסת אנדרואיד:</b> ${info.osVersion} (API ${info.apiLevel})</p>
                <p><b>דגם מכשיר:</b> ${info.deviceModel}</p>
            """.trimIndent()
        }

        dispatchEmail(context, subject, body, htmlBody, context.getString(R.string.report_email_chooser))
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

        val isEn = isEnglishLocale(context)
        val htmlBody = if (isEn) {
            """
                <p><br/><br/><br/></p>
                <p>--- Please write your message above this line (describe the image issue) ---</p>
                <br/>
                <h3>--- Reported Image Details ---</h3>
                <p><b>Wikimedia File:</b><br/>${image.title.ifBlank { "-" }}</p>
                <br/>
                <p><b>Direct Image URL:</b></p>
                <p><a href="$directUrl" target="_blank" style="color: #1a73e8; text-decoration: underline;">$directUrl</a></p>
                <br/>
                <p><b>Source Page URL:</b></p>
                <p><a href="$sourceUrl" target="_blank" style="color: #1a73e8; text-decoration: underline;">$sourceUrl</a></p>
                <br/>
                <p><b>Associated Vehicle / Search:</b><br/>${vehicleOrSearchContext.ifBlank { "-" }}</p>
                <br/>
                <h3>--- System Information ---</h3>
                <p><b>App Name:</b> ${info.appName}</p>
                <p><b>App Version:</b> ${info.versionName} (${info.versionCode})</p>
                <p><b>Android OS:</b> ${info.osVersion} (API ${info.apiLevel})</p>
                <p><b>Device:</b> ${info.deviceModel}</p>
            """.trimIndent()
        } else {
            """
                <p><br/><br/><br/></p>
                <p>--- כתבו את הודעתכם מעל קו זה (נא לפרט מדוע התמונה שגויה) ---</p>
                <br/>
                <h3>--- פרטי התמונה המדווחת ---</h3>
                <p><b>שם קובץ בוויקימדיה:</b><br/>${image.title.ifBlank { "-" }}</p>
                <br/>
                <p><b>קישור ישיר לתמונה:</b></p>
                <p><a href="$directUrl" target="_blank" style="color: #1a73e8; text-decoration: underline;">$directUrl</a></p>
                <br/>
                <p><b>קישור לעמוד המקור בוויקימדיה:</b></p>
                <p><a href="$sourceUrl" target="_blank" style="color: #1a73e8; text-decoration: underline;">$sourceUrl</a></p>
                <br/>
                <p><b>רכב / חיפוש משויך:</b><br/>${vehicleOrSearchContext.ifBlank { "-" }}</p>
                <br/>
                <h3>--- מידע טכני ---</h3>
                <p><b>שם האפליקציה:</b> ${info.appName}</p>
                <p><b>גרסת אפליקציה:</b> ${info.versionName} (${info.versionCode})</p>
                <p><b>גרסת אנדרואיד:</b> ${info.osVersion} (API ${info.apiLevel})</p>
                <p><b>דגם מכשיר:</b> ${info.deviceModel}</p>
            """.trimIndent()
        }

        dispatchEmail(context, subject, body, htmlBody, context.getString(R.string.report_image_email_chooser))
    }

    private fun dispatchEmail(context: Context, subject: String, body: String, htmlBody: String? = null, chooserTitle: String) {
        val mailtoUri = Uri.parse(
            "mailto:$SUPPORT_EMAIL" +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(body)
        )
        val emailIntent = Intent(Intent.ACTION_SENDTO, mailtoUri).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            if (!htmlBody.isNullOrBlank()) {
                putExtra(Intent.EXTRA_HTML_TEXT, htmlBody)
            }
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
                    if (!htmlBody.isNullOrBlank()) {
                        putExtra(Intent.EXTRA_HTML_TEXT, htmlBody)
                    }
                }
                context.startActivity(Intent.createChooser(fallbackIntent, chooserTitle))
            } catch (_: Exception) {
                Toast.makeText(context, context.getString(R.string.report_email_no_app), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
