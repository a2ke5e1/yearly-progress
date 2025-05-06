package com.a3.yearlyprogess.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
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
import com.a3.yearlyprogess.cacheLocation
import com.a3.yearlyprogess.cacheSunriseSunset
import com.a3.yearlyprogess.components.DayNightLightProgressView
import com.a3.yearlyprogess.components.dialogbox.PermissionMessageDialog
import com.a3.yearlyprogess.data.SunriseSunsetApi
import com.a3.yearlyprogess.data.models.SunriseSunsetResponse
import com.a3.yearlyprogess.databinding.FragmentScreenProgressBinding
import com.a3.yearlyprogess.getCurrentDate
import com.a3.yearlyprogess.getDateRange
import com.a3.yearlyprogess.loadCachedLocation
import com.a3.yearlyprogess.loadCachedSunriseSunset
import com.a3.yearlyprogess.provideSunriseSunsetApi
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProgressScreenFragment : Fragment() {
  private lateinit var binding: FragmentScreenProgressBinding
  private val sunriseSunsetApi: SunriseSunsetApi = provideSunriseSunsetApi()

  private lateinit var locationPermissionDialog: PermissionMessageDialog
  private lateinit var adLoader: AdLoader
  private lateinit var nativeAdView: NativeAdView

  private val requestPermissionLauncher =
      registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
          setupDayNightLightProgressView(
              binding.dayLightProgressView, binding.nightLightProgressView)
        } else {
          locationPermissionDialog.show(parentFragmentManager, "location_permission_dialog")
        }
      }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    binding = FragmentScreenProgressBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val dayLight = binding.dayLightProgressView
    val nightLight = binding.nightLightProgressView

    dayLight.visibility = View.GONE
    nightLight.visibility = View.GONE

    locationPermissionDialog =
        PermissionMessageDialog(
            icon = R.drawable.ic_location_on_24,
            title = getString(R.string.location_permission_title),
            message = getString(R.string.location_permission_message)) {
              requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }

    val userLocationPref = UserLocationPref.load(requireContext())

    if (userLocationPref.automaticallyDetectLocation) {
      when {
        ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED -> {
          setupDayNightLightProgressView(dayLight, nightLight)
        }

        ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) -> {
          locationPermissionDialog.show(parentFragmentManager, "")
        }

        else -> {
          binding.dismissibleMessageView?.visibility = View.VISIBLE
          binding.callToAction?.setOnClickListener {
            locationPermissionDialog.show(parentFragmentManager, "location_permission_dialog")
          }
        }
      }
    } else {
      binding.dismissibleMessageView?.visibility = View.GONE
      setupDayNightLightProgressView(dayLight, nightLight)
    }

    setupAdLoader(view.findViewById(R.id.ad_frame))
  }

  private fun setupAdLoader(adFrame: LinearLayout) {
    adLoader =
        AdLoader.Builder(requireContext(), getString(R.string.admob_native_ad_unit))
            .forNativeAd { ad ->
              if (!adLoader.isLoading && !isDetached) {
                adFrame.visibility = View.VISIBLE
                nativeAdView = updateViewWithNativeAdview(adFrame, ad)
              } else {
                adFrame.visibility = View.GONE
                ad.destroy()
              }
            }
            .withAdListener(
                object : AdListener() {
                  override fun onAdFailedToLoad(adError: LoadAdError) {
                    adFrame.visibility = View.GONE
                    adFrame.removeAllViews()
                  }
                })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_BOTTOM_RIGHT)
                    .build())
            .build()

    adLoader.loadAd(AdRequest.Builder().build())
  }

  private fun setupDayNightLightProgressView(
      dayLight: DayNightLightProgressView,
      nightLight: DayNightLightProgressView
  ) {
    val userLocationPref = UserLocationPref.load(requireContext())
    val locationManager =
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = locationManager.allProviders.filter { locationManager.isProviderEnabled(it) }

    if (userLocationPref.automaticallyDetectLocation) {
      if (ActivityCompat.checkSelfPermission(
          requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
          PackageManager.PERMISSION_GRANTED)
          return

      if (providers.isEmpty()) {
        Toast.makeText(
                context, "Your device does not have any location provider", Toast.LENGTH_LONG)
            .show()
        return
      }

      binding.dismissibleMessageView?.visibility = View.GONE

      val cachedLocation = context?.let { loadCachedLocation(it) }
      cachedLocation?.let {
        lifecycleScope.launch(Dispatchers.IO) {
          fetchAndShowSunriseSunset(it, dayLight, nightLight)
        }
      }

      locationManager.requestLocationUpdates(
          providers.find { it == LocationManager.GPS_PROVIDER } ?: providers.first(),
          2000,
          1000f) { location ->
            context?.let { cacheLocation(it, location) }
            lifecycleScope.launch(Dispatchers.IO) {
              fetchAndShowSunriseSunset(location, dayLight, nightLight)
            }
          }
    } else {
      userLocationPref.userLocationPref?.let {
        val location =
            Location("").apply {
              latitude = it.lat.toDouble()
              longitude = it.lon.toDouble()
            }
        lifecycleScope.launch(Dispatchers.IO) {
          fetchAndShowSunriseSunset(location, dayLight, nightLight)
        }
      }
    }
  }

  private suspend fun fetchAndShowSunriseSunset(
      location: Location,
      dayLight: DayNightLightProgressView,
      nightLight: DayNightLightProgressView
  ) {
    lifecycleScope.launch(Dispatchers.Main) { binding.loadingIndicator?.visibility = View.VISIBLE }

    val cached = context?.let { loadCachedSunriseSunset(it) }
    if (cached != null && cached.results[1].date == getCurrentDate()) {
      showSunriseSunset(cached, dayLight, nightLight)
      return
    }

    val result =
        try {
          val response =
              sunriseSunsetApi.getSunriseSunset(
                  location.latitude, location.longitude, getDateRange(-1), getDateRange(1))
          response
              .body()
              ?.takeIf { response.isSuccessful && it.status == "OK" }
              ?.let {
                cacheSunriseSunset(requireContext(), it)
                Resource.Success(it)
              } ?: Resource.Error(response.message())
        } catch (e: Exception) {
          Resource.Error(e.message ?: "Unknown error")
        }

    when (result) {
      is Resource.Success -> showSunriseSunset(result.data, dayLight, nightLight)
      is Resource.Error -> {
        if (!isAdded) return
        lifecycleScope.launch(Dispatchers.Main) {
          Toast.makeText(
                  context,
                  getString(R.string.failed_to_load_sunset_sunrise_time),
                  Toast.LENGTH_LONG)
              .show()
          dayLight.visibility = View.GONE
          nightLight.visibility = View.GONE
        }
      }
    }
  }

  private fun showSunriseSunset(
      data: SunriseSunsetResponse?,
      dayLight: DayNightLightProgressView,
      nightLight: DayNightLightProgressView
  ) {
    data?.let {
      if (!isAdded) return
      dayLight.loadSunriseSunset(it)
      nightLight.loadSunriseSunset(it)
      lifecycleScope.launch(Dispatchers.Main) {
        dayLight.visibility = View.VISIBLE
        nightLight.visibility = View.VISIBLE
        binding.loadingIndicator?.visibility = View.GONE
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    try {
      nativeAdView.destroy()
    } catch (_: UninitializedPropertyAccessException) {}
  }

  sealed class Resource<T>(val data: T?, val message: String?) {
    class Success<T>(data: T) : Resource<T>(data, null)

    class Error<T>(message: String) : Resource<T>(null, message)
  }
}
