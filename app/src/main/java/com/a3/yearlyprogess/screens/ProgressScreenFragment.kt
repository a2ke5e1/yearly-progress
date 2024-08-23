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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.a3.yearlyprogess.MainActivity
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.SubscriptionStatus
import com.a3.yearlyprogess.YearlyProgressSubscriptionManager
import com.a3.yearlyprogess.ad.CustomAdView.Companion.updateViewWithNativeAdview
import com.a3.yearlyprogess.cacheLocation
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/** A simple [Fragment] subclass as the default destination in the navigation. */
class ProgressScreenFragment : Fragment() {

  private var _binding: FragmentScreenProgressBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding
    get() = _binding!!

  private val sunriseSunsetApi: SunriseSunsetApi = provideSunriseSunsetApi()

  private val requestPermissionLauncher =
      registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
          // Permission is granted. Continue the action or workflow in your
          // app.
          setupDayNightLightProgressView(
              binding.dayLightProgressView, binding.nightLightProgressView)
        } else {

          locationPermissionDialog.show(parentFragmentManager, "location_permission_dialog")
        }
      }

  private lateinit var locationPermissionDialog: PermissionMessageDialog

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    _binding = FragmentScreenProgressBinding.inflate(inflater, container, false)
    return binding.root
  }

  private lateinit var adLoader: AdLoader
  private lateinit var nativeAdView: NativeAdView
  private lateinit var billingManager: YearlyProgressSubscriptionManager

  override fun onAttach(context: Context) {
    super.onAttach(context)
    billingManager = (context as MainActivity).billingManager
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val dayLight: DayNightLightProgressView = binding.dayLightProgressView
    val nightLight: DayNightLightProgressView = binding.nightLightProgressView

    dayLight.visibility = View.GONE
    nightLight.visibility = View.GONE

    locationPermissionDialog =
        PermissionMessageDialog(
            icon = R.drawable.ic_location_on_24,
            title = getString(R.string.location_permission_title),
            message = getString(R.string.location_permission_message)) {
              requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }

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
        // You can directly ask for the permission.
        // The registered ActivityResultCallback gets the result of this request.
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
      }
    }

    val adFrame: LinearLayout = view.findViewById(R.id.ad_frame)
    billingManager.shouldShowAds.observe(viewLifecycleOwner) {
      Log.d("Subscription Status", it.toString())
      when (it) {
        SubscriptionStatus.Subscribed -> {
          try {
            nativeAdView.destroy()
          } catch (ex: UninitializedPropertyAccessException) {
            Log.d("Initialization Error", ex.message.toString())
          }
        }
        SubscriptionStatus.Loading -> {}
        else -> {
          // Show ads
          adLoader =
              AdLoader.Builder(requireContext(), getString(R.string.admob_native_ad_unit))
                  .forNativeAd { ad: NativeAd ->
                    // Show the ad.

                    if (!adLoader.isLoading) {
                      adFrame.visibility = View.VISIBLE
                      nativeAdView = updateViewWithNativeAdview(adFrame, ad)
                    }
                    if (isDetached) {
                      adFrame.visibility = View.GONE
                      ad.destroy()
                      return@forNativeAd
                    }
                  }
                  .withAdListener(
                      object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                          // Handle the failure by logging, altering the UI, and so on.
                          adFrame.visibility = View.GONE
                          adFrame.removeAllViews()
                        }
                      })
                  .withNativeAdOptions(
                      NativeAdOptions.Builder()
                          .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_BOTTOM_RIGHT)
                          // Methods in the NativeAdOptions.Builder class can be
                          // used here to specify individual options settings.
                          .build())
                  .build()
          adLoader.loadAd(AdRequest.Builder().build())
        }
      }
    }
  }

  private fun setupDayNightLightProgressView(
      dayLight: DayNightLightProgressView,
      nightLight: DayNightLightProgressView
  ) {

    val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (ActivityCompat.checkSelfPermission(
        requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED) {
      return
    }

    // Get the list of available location providers
    val providers = locationManager.allProviders.filter { locationManager.isProviderEnabled(it) }

    if (providers.isEmpty()) {
      Toast.makeText(context, "Your device does not have any location provider", Toast.LENGTH_LONG)
          .show()
      return
    }

    locationManager.requestLocationUpdates(
        providers.find { it == LocationManager.GPS_PROVIDER } ?: providers.first(),
        2000,
        1_000f //  12hrs, 200 KM
        ) { location ->
          context?.let { cacheLocation(it, location) }
          lifecycleScope.launch(Dispatchers.IO) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis()

            cal.add(Calendar.DATE, -1)

            val startDateRange =
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DATE)}"
            cal.add(Calendar.DATE, 2)
            val endDateRange =
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DATE)}"

            val result: Resource<SunriseSunsetResponse> =
                try {
                  val response =
                      sunriseSunsetApi.getSunriseSunset(
                          location.latitude, location.longitude, startDateRange, endDateRange)
                  val result = response.body()
                  if (response.isSuccessful && result != null && result.status == "OK") {
                    storeSunriseSunset(requireContext(), result)
                    Resource.Success(result)
                  } else {
                    Resource.Error(response.message())
                  }
                } catch (e: Exception) {
                  Resource.Error(e.message ?: "Error Occurred")
                }

            when (result) {
              is Resource.Success -> {
                result.data?.let {
                  dayLight.loadSunriseSunset(it)
                  nightLight.loadSunriseSunset(it)

                  launch(Dispatchers.Main) {
                    dayLight.visibility = View.VISIBLE
                    nightLight.visibility = View.VISIBLE
                    binding.loadingIndicator?.visibility = View.GONE
                  }
                }
              }

              is Resource.Error -> {
                val cachedSunriseSunset = loadSunriseSunset(requireContext())

                if (cachedSunriseSunset == null) {
                  launch(Dispatchers.Main) {
                      Toast.makeText(
                          context,
                          getString(R.string.failed_to_load_sunset_sunrise_time), Toast.LENGTH_LONG).show()
                    dayLight.visibility = View.GONE
                    nightLight.visibility = View.GONE
                    binding.loadingIndicator?.visibility = View.GONE
                  }
                  return@launch
                }

                // get current date
                cal.timeInMillis = System.currentTimeMillis()
                val currentDate =
                    "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${
                                cal.get(
                                    Calendar.DATE
                                )
                            }"

                if (cachedSunriseSunset.results[1].date != currentDate) {
                  launch(Dispatchers.Main) {
                    dayLight.visibility = View.GONE
                    nightLight.visibility = View.GONE
                    binding.loadingIndicator?.visibility = View.GONE
                  }
                }

                cachedSunriseSunset.let {
                  dayLight.loadSunriseSunset(it)
                  nightLight.loadSunriseSunset(it)
                  launch(Dispatchers.Main) {
                    dayLight.visibility = View.VISIBLE
                    nightLight.visibility = View.VISIBLE
                    binding.loadingIndicator?.visibility = View.GONE
                  }
                }
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

  sealed class Resource<T>(val data: T?, val message: String?) {
    class Success<T>(data: T) : Resource<T>(data, null)

    class Error<T>(message: String) : Resource<T>(null, message)
  }
}
