package com.a3.yearlyprogess.ad

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.a3.yearlyprogess.R
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class CustomAdView {
  companion object {
    fun updateViewWithNativeAdview(
        adFrame: LinearLayout,
        ad: NativeAd,
    ): NativeAdView {
      val inflater =
          adFrame.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
      val nativeAdView = inflater.inflate(R.layout.custom_adview, null) as NativeAdView
      val callToAction = nativeAdView.findViewById<MaterialButton>(R.id.ctn)
      val headline = nativeAdView.findViewById<TextView>(R.id.title)
      val body = nativeAdView.findViewById<TextView>(R.id.body)
      val provider = nativeAdView.findViewById<TextView>(R.id.provider)
      val icon = nativeAdView.findViewById<ShapeableImageView>(R.id.icon)

      // The AdLoader has finished loading ads.

      headline.text = ad.headline
      body.text = ad.body
      if (ad.store != null) {
        provider.text = "${ad.store} · ${ad.price}"
      } else {
        provider.visibility = View.GONE
      }
      if (ad.icon != null) {
        icon.setImageDrawable(ad.icon!!.drawable)
      } else {
        icon.visibility = View.GONE
      }
      callToAction.text = ad.callToAction

      nativeAdView.callToActionView = callToAction
      nativeAdView.headlineView = headline
      nativeAdView.bodyView = body
      nativeAdView.storeView = provider
      nativeAdView.iconView = icon

      nativeAdView.setNativeAd(ad)
      adFrame.removeAllViews()
      adFrame.addView(nativeAdView)

      return nativeAdView
    }
  }
}
