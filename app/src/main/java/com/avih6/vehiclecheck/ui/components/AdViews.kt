package com.avih6.vehiclecheck.ui.components

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.avih6.vehiclecheck.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val adUnitId = if (com.avih6.vehiclecheck.BuildConfig.DEBUG) "ca-app-pub-3940256099942544/6300978111" else "ca-app-pub-6647546375254792/8250052303"
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun NativeAdView(nativeAd: NativeAd, modifier: Modifier = Modifier) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val textColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
    val bodyColor = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.DKGRAY

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = MaterialTheme.shapes.medium
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            factory = { context ->
                val adView = LayoutInflater.from(context)
                    .inflate(R.layout.ad_native_result, null) as NativeAdView
                
                populateNativeAdView(nativeAd, adView, textColor, bodyColor)
                adView
            },
            update = { adView ->
                populateNativeAdView(nativeAd, adView, textColor, bodyColor)
            }
        )
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView, textColor: Int, bodyColor: Int) {
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    adView.mediaView = null
    adView.priceView = adView.findViewById(R.id.ad_price)
    adView.starRatingView = adView.findViewById(R.id.ad_stars)
    adView.storeView = adView.findViewById(R.id.ad_store)
    adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

    val headline = adView.headlineView as? TextView
    headline?.text = nativeAd.headline
    headline?.setTextColor(textColor)
    
    if (nativeAd.body == null) {
        adView.bodyView?.visibility = View.INVISIBLE
    } else {
        adView.bodyView?.visibility = View.VISIBLE
        val body = adView.bodyView as? TextView
        body?.text = nativeAd.body
        body?.setTextColor(bodyColor)
    }

    if (nativeAd.callToAction == null) {
        adView.callToActionView?.visibility = View.INVISIBLE
    } else {
        adView.callToActionView?.visibility = View.VISIBLE
        (adView.callToActionView as Button).text = nativeAd.callToAction
    }

    // Icon handling
    val iconImageView = adView.iconView as? ImageView
    if (nativeAd.icon != null && nativeAd.icon?.drawable != null) {
        iconImageView?.setImageDrawable(nativeAd.icon?.drawable)
        iconImageView?.visibility = View.VISIBLE
    } else if (nativeAd.images.isNotEmpty() && nativeAd.images[0].drawable != null) {
        iconImageView?.setImageDrawable(nativeAd.images[0].drawable)
        iconImageView?.visibility = View.VISIBLE
    } else {
        iconImageView?.visibility = View.GONE
    }

    adView.setNativeAd(nativeAd)
}