package com.a3.yearlyprogess.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.ad.CustomAdView.Companion.updateViewWithNativeAdview
import com.a3.yearlyprogess.components.DayNightLightProgressView
import com.a3.yearlyprogess.components.dialogbox.PermissionMessageDialog
import com.a3.yearlyprogess.data.SunriseSunsetApi
import com.a3.yearlyprogess.data.models.SunriseSunsetResponse
import com.a3.yearlyprogess.databinding.FragmentScreenProgressBinding
import com.a3.yearlyprogess.loadSunriseSunset
import com.a3.yearlyprogess.provideSunriseSunsetApi
import com.a3.yearlyprogess.storeSunriseSunset
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.gson.Gson
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
    private val sunriseSunsetApi: SunriseSunsetApi = provideSunriseSunsetApi()


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                setupDayNightLightProgressView(binding.root)

            } else {

                locationPermissionDialog.show(parentFragmentManager, "")

            }
        }


    private lateinit var locationPermissionDialog: PermissionMessageDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScreenProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var adLoader: AdLoader
    private lateinit var nativeAdView: NativeAdView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationPermissionDialog = PermissionMessageDialog(
            icon = R.drawable.ic_location_on_24,
            title = getString(R.string.location_permission_title),
            message = getString(R.string.location_permission_message)
        ) {
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }


        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                setupDayNightLightProgressView(view)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                locationPermissionDialog.show(parentFragmentManager, "")
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }




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


        dayLight.visibility = View.GONE
        nightLight.visibility = View.GONE

        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Get the list of available location providers
        val providers = locationManager.allProviders.filter {
            locationManager.isProviderEnabled(it)
        }

        if (providers.isEmpty()) {
            Toast.makeText(context, "Your device does not have any location provider", Toast.LENGTH_LONG).show()
            return
        }

        locationManager.requestLocationUpdates(
            providers.first(), 43_200_000, 200_000f //  12hrs, 200 KM
        ) { location ->
            Log.d("Location", location.toString())
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
                    storeSunriseSunset(requireContext(), response.body()!!)
                    dayLight.loadSunriseSunset(response.body()!!)
                    nightLight.loadSunriseSunset(response.body()!!)
                    launch(Dispatchers.Main) {
                        dayLight.visibility = View.VISIBLE
                        nightLight.visibility = View.VISIBLE
                    }
                } else {
                    val cachedSunriseSunset = loadSunriseSunset(requireContext())

                    if (cachedSunriseSunset == null) {
                        launch(Dispatchers.Main) {
                            dayLight.visibility = View.GONE
                            nightLight.visibility = View.GONE
                        }
                        return@launch
                    }

                    // get current date
                    cal.timeInMillis = System.currentTimeMillis()
                    val currentDate =
                        "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DATE)}"

                    if (cachedSunriseSunset.results[1].date != currentDate) {
                        launch(Dispatchers.Main) {
                            dayLight.visibility = View.GONE
                            nightLight.visibility = View.GONE
                        }
                    }

                    dayLight.loadSunriseSunset(cachedSunriseSunset)
                    nightLight.loadSunriseSunset(cachedSunriseSunset)
                    launch(Dispatchers.Main) {
                        dayLight.visibility = View.VISIBLE
                        nightLight.visibility = View.VISIBLE
                    }

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

