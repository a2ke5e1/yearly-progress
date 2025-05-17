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
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.ad.CustomAdView.Companion.updateViewWithNativeAdview
import com.a3.yearlyprogess.cacheLocation
import com.a3.yearlyprogess.cacheSunriseSunset
import com.a3.yearlyprogess.components.dialogbox.PermissionMessageDialog
import com.a3.yearlyprogess.data.SunriseSunsetApi
import com.a3.yearlyprogess.data.models.SunriseSunsetResponse
import com.a3.yearlyprogess.databinding.FragmentScreenProgressBinding
import com.a3.yearlyprogess.getCurrentDate
import com.a3.yearlyprogess.getDateRange
import com.a3.yearlyprogess.loadCachedLocation
import com.a3.yearlyprogess.loadCachedSunriseSunset
import com.a3.yearlyprogess.provideSunriseSunsetApi
import com.a3.yearlyprogess.screens.ProgressScreenFragment.Resource
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


sealed class SunriseSunsetState {
  data object Loading : SunriseSunsetState()
  data class Error(val message: String) : SunriseSunsetState()
  data class Success(
    val data: SunriseSunsetResponse
  ) : SunriseSunsetState()
}

class ProgressScreenViewModel : ViewModel() {

  private val sunriseSunsetApi: SunriseSunsetApi = provideSunriseSunsetApi()
  private val _state = MutableLiveData<SunriseSunsetState>(SunriseSunsetState.Loading)
  val sunriseSunsetState get()= _state
  private val firebaseCrashlytics = Firebase.crashlytics

  fun fetchSunriseSunset(
    context: Context,
    location: Location
  ) {

    val cached = loadCachedSunriseSunset(context)
    if (cached != null && cached.results[1].date == getCurrentDate()) {
      _state.postValue(SunriseSunsetState.Success(cached))
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      val result =
        try {
          val response =
            sunriseSunsetApi.getSunriseSunset(
              location.latitude, location.longitude, getDateRange(-1), getDateRange(1))
          response
            .body()
            ?.takeIf { response.isSuccessful && it.status == "OK" }
            ?.let {
              cacheSunriseSunset(context, it)
              Resource.Success(it)
            } ?: Resource.Error(response.message())
        } catch (e: Exception) {
          Resource.Error(e.message ?: "Unknown error")
        }

      when (result) {
        is Resource.Success -> _state.postValue(
          if (result.data != null) SunriseSunsetState.Success(result.data) else
            SunriseSunsetState.Error(context.getString(R.string.failed_to_load_sunset_sunrise_time),)
        )
        is Resource.Error -> {
          firebaseCrashlytics.log("Failed to load sunset data: \n ${result.message}")
          _state.postValue(SunriseSunsetState.Error(context.getString(R.string.failed_to_load_sunset_sunrise_time),))
        }
      }
    }
  }


}


class ProgressScreenFragment : Fragment() {
  private lateinit var binding: FragmentScreenProgressBinding
  private val progressScreenViewModel: ProgressScreenViewModel by viewModels()

  private lateinit var locationPermissionDialog: PermissionMessageDialog
  private lateinit var adLoader: AdLoader
  private lateinit var nativeAdView: NativeAdView

  private val requestPermissionLauncher =
      registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
          setupDayNightLightProgressView()
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

    progressScreenViewModel.sunriseSunsetState.observe(viewLifecycleOwner) { state ->
      when (state) {
        is SunriseSunsetState.Loading -> {
          binding.loadingIndicator.visibility = View.VISIBLE
          binding.dayLightProgressView.visibility = View.GONE
          binding.nightLightProgressView.visibility = View.GONE
        }
        is SunriseSunsetState.Success -> {
          binding.loadingIndicator.visibility = View.GONE
          binding.dayLightProgressView.apply {
            visibility = View.VISIBLE
            loadSunriseSunset(state.data)
          }
          binding.nightLightProgressView.apply {
            visibility = View.VISIBLE
            loadSunriseSunset(state.data)
          }
        }
        is SunriseSunsetState.Error -> {
          binding.loadingIndicator.visibility = View.GONE
          binding.dayLightProgressView.visibility = View.GONE
          binding.nightLightProgressView.visibility = View.GONE
          Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
        }
      }
    }



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
          setupDayNightLightProgressView()
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
      setupDayNightLightProgressView()
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
        setupSunriseSunsetViews(it)
      }

      locationManager.requestLocationUpdates(
          providers.find { it == LocationManager.GPS_PROVIDER } ?: providers.first(),
          2000,
          1000f) { location ->
              context?.let { cacheLocation(it, location) }
              setupSunriseSunsetViews(location)
          }
    } else {
      userLocationPref.userLocationPref?.let {
        val location =
            Location("").apply {
              latitude = it.lat.toDouble()
              longitude = it.lon.toDouble()
            }
        setupSunriseSunsetViews(location)
      }
    }
  }

  private fun setupSunriseSunsetViews(
      location: Location
  ) {
    context?.let { progressScreenViewModel.fetchSunriseSunset(it, location) }
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
