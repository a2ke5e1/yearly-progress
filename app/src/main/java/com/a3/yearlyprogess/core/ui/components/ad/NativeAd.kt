package com.a3.yearlyprogess.core.ui.components.ad

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.style.CardCornerStyle
import com.a3.yearlyprogess.core.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import java.util.Arrays

@Composable
fun AdCard(
    style: AdCardStyle = AdCardDefaults.adCardStyle(), modifier: Modifier = Modifier
) {
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    val context = LocalContext.current
    var isDisposed by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        // Load the native ad when we launch this screen
        loadNativeAd(
            context = context,
            onAdLoaded = { ad ->
                // Handle the native ad being loaded.
                if (!isDisposed) {
                    nativeAd = ad
                } else {
                    // Destroy the native ad if loaded after the screen is disposed.
                    ad.destroy()
                }
            },
        )
        // Destroy the native ad to prevent memory leaks when we dispose of this screen.
        onDispose {
            isDisposed = true
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    // Display the native ad view with a user defined template.
    nativeAd?.let { adValue ->
        DisplayNativeAdView(
            adValue, style, modifier
        )
    }
}

fun loadNativeAd(context: Context, onAdLoaded: (NativeAd) -> Unit) {
    val TAG = "LoadNativeAd"
    val adLoader = AdLoader.Builder(context, context.getString(R.string.admob_native_ad_unit))
        .forNativeAd { nativeAd -> onAdLoaded(nativeAd) }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "Native ad failed to load: ${error.message}")
                Log.e(TAG, "Native ad failed to load: ${error}")
            }

            override fun onAdLoaded() {
                Log.d(TAG, "Native ad was loaded.")
            }

            override fun onAdImpression() {
                Log.d(TAG, "Native ad recorded an impression.")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Native ad was clicked.")
            }
        })
        .withNativeAdOptions(
            NativeAdOptions.Builder()
                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_BOTTOM_RIGHT)
                .build())
        .build()
    adLoader.loadAd(AdRequest.Builder().build())
}


data class AdCardStyle(
    val cardPadding: Dp,
    val backgroundColor: Color,
    val cornerStyle: CardCornerStyle,
)

object AdCardDefaults {

    @Composable
    fun adCardStyle(
        cardPadding: Dp = 18.dp,
        backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
        cornerStyle: CardCornerStyle = CardCornerStyle.Default,
    ): AdCardStyle = AdCardStyle(
        cardPadding = cardPadding,
        backgroundColor = backgroundColor,
        cornerStyle = cornerStyle,
    )
}


@Composable
        /** Display a native ad with a user defined template. */
fun DisplayNativeAdView(
    nativeAd: NativeAd,
    style: AdCardStyle = AdCardDefaults.adCardStyle(),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.wrapContentHeight(Alignment.Top)
    ) {
        // Call the NativeAdView composable to display the native ad.
        NativeAdView(
            nativeAd,
            modifier = Modifier
                .clip(style.cornerStyle.toShape())
                .background(style.backgroundColor)
        ) {
            NativeAdAttribution(
                text = "Ad",
                modifier = Modifier
                    .wrapContentWidth(Alignment.End)
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )

            // Inside the NativeAdView composable, display the native ad assets.
            Column(
                modifier = Modifier
                    .padding(style.cardPadding)
                    .align(Alignment.TopStart)
                    .wrapContentHeight(Alignment.Top)
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // If available, display the icon asset.
                    nativeAd.icon?.let { icon ->
                        NativeAdIconView(Modifier.size(64.dp)) {
                            icon.drawable?.toBitmap()?.let { bitmap ->
                                Image(bitmap = bitmap.asImageBitmap(), "Icon")
                            }
                        }
                    }
                    Column {
                        // If available, display the headline asset.
                        nativeAd.headline?.let {
                            NativeAdHeadlineView {
                                Text(text = it, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        nativeAd.body?.let {
                            NativeAdBodyView {
                                Text(
                                    text = it, style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            // If available, display the store asset.
                            nativeAd.store?.let {
                                NativeAdStoreView(
                                    Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Text(text = it, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            // If available, display the star rating asset.
                            nativeAd.starRating?.let {
                                NativeAdStarRatingView(
                                    Modifier.align(Alignment.CenterVertically)
                                ){
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Star Rating",
                                            Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "$it", style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                            // If available, display the price asset.
                            nativeAd.price?.let {
                                NativeAdPriceView(
                                    Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Text(text = it, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // If available, display the call to action asset.
                        // Note: The Jetpack Compose button implements a click handler which overrides the native
                        // ad click handler, causing issues. Use the NativeAdButton which does not implement a
                        // click handler. To handle native ad clicks, use the NativeAd AdListener onAdClicked
                        // callback.
                        nativeAd.callToAction?.let { callToAction ->
                            NativeAdCallToActionView { NativeAdButton(text = callToAction) }
                        }
                    }
                }
            }
        }
    }

}
