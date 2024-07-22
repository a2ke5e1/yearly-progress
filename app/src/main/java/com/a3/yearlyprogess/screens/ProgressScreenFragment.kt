package com.a3.yearlyprogess.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.ad.CustomAdView.Companion.updateViewWithNativeAdview
import com.a3.yearlyprogess.components.DayNightLightProgressView
import com.a3.yearlyprogess.data.SunriseSunsetApi
import com.a3.yearlyprogess.databinding.FragmentScreenProgressBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ProgressScreenFragment : Fragment() {

    private var _binding: FragmentScreenProgressBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var sunriseSunsetApi: SunriseSunsetApi

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                setupDayNightLightProgressView(binding.root)

            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentScreenProgressBinding.inflate(inflater, container, false)

        sunriseSunsetApi =
            Retrofit.Builder().baseUrl(DayNightLightProgressView.SUNRISE_SUNSET_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                // .client(okHttpClient)
                .build().create(SunriseSunsetApi::class.java)

        return binding.root

    }

    private lateinit var adLoader: AdLoader
    private lateinit var nativeAdView: NativeAdView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher!!.launch(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        setupDayNightLightProgressView(view)

        val adFrame: LinearLayout = view.findViewById(R.id.ad_frame)
        adLoader = AdLoader.Builder(requireContext(), getString(R.string.admob_native_ad_unit))
            .forNativeAd { ad: NativeAd ->
                // Show the ad.

                if (!adLoader.isLoading) {
                    nativeAdView = updateViewWithNativeAdview(adFrame, ad)
                }
                if (isDetached) {
                    ad.destroy()
                    return@forNativeAd
                }
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Handle the failure by logging, altering the UI, and so on.
                    adFrame.removeAllViews()
                }
            }).withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_BOTTOM_RIGHT)
                    // Methods in the NativeAdOptions.Builder class can be
                    // used here to specify individual options settings.
                    .build()
            ).build()
        adLoader.loadAd(AdRequest.Builder().build())

    }

    private fun setupDayNightLightProgressView(view: View) {
        val dayLight: DayNightLightProgressView = view.findViewById(R.id.dayLightProgressView)
        val nightLight: DayNightLightProgressView = view.findViewById(R.id.nightLightProgressView)

        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 2000, 10f
        ) { location ->

            lifecycleScope.launch(Dispatchers.IO) {

                val cal = Calendar.getInstance()
                cal.timeInMillis = System.currentTimeMillis()

                cal.add(Calendar.DATE, -1)

                val startDateRange =
                    "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DATE)}"
                cal.add(Calendar.DATE, 2)
                val endDateRange =
                    "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DATE)}"

                val response = sunriseSunsetApi.getSunriseSunset(
                    location.latitude, location.longitude, startDateRange, endDateRange
                )
                if (response.isSuccessful && response.body() != null) {
                    dayLight.loadSunriseSunset(response.body()!!)
                    nightLight.loadSunriseSunset(response.body()!!)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            nativeAdView.destroy()
        } catch (ex: UninitializedPropertyAccessException) {
            Log.d("Initialization Error", ex.message.toString())
        }
        _binding = null
    }


}

