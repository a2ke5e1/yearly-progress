package com.a3.yearlyprogess.screens

import android.Manifest
import android.animation.ValueAnimator
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RemoteViews
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.MainActivity
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.SubscriptionStatus
import com.a3.yearlyprogess.TimePeriod
import com.a3.yearlyprogess.YearlyProgressSubscriptionManager
import com.a3.yearlyprogess.ad.CustomAdView.Companion.updateViewWithNativeAdview
import com.a3.yearlyprogess.calculateProgress
import com.a3.yearlyprogess.databinding.FragmentWidgetScreenBinding
import com.a3.yearlyprogess.getCurrentPeriodValue
import com.a3.yearlyprogess.loadSunriseSunset
import com.a3.yearlyprogess.widgets.ui.AllInWidget
import com.a3.yearlyprogess.widgets.ui.DayLightWidget
import com.a3.yearlyprogess.widgets.ui.DayNightWidget
import com.a3.yearlyprogess.widgets.ui.DayWidget
import com.a3.yearlyprogess.widgets.ui.MonthWidget
import com.a3.yearlyprogess.widgets.ui.NightLightWidget
import com.a3.yearlyprogess.widgets.ui.StandaloneWidget
import com.a3.yearlyprogess.widgets.ui.WeekWidget
import com.a3.yearlyprogess.widgets.ui.YearWidget
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.a3.yearlyprogess.widgets.ui.util.toFormattedTimePeriod
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** A simple [Fragment] subclass as the second destination in the navigation. */
class WidgetScreenFragment : Fragment() {

  private var _binding: FragmentWidgetScreenBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding
    get() = _binding!!

  private lateinit var billingManager: YearlyProgressSubscriptionManager

  override fun onAttach(context: Context) {
    super.onAttach(context)
    billingManager = (context as MainActivity).billingManager
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {

    _binding = FragmentWidgetScreenBinding.inflate(inflater, container, false)
    return binding.root
  }

  private lateinit var adLoader: AdLoader
  private lateinit var nativeAdView: NativeAdView

  private lateinit var allInOneProgressTextViewYear: TextView
  private lateinit var allInOneProgressTextViewMonth: TextView
  private lateinit var allInOneProgressTextViewDay: TextView
  private lateinit var allInOneProgressTextViewWeek: TextView

  private lateinit var allInOneProgressBarYear: ProgressBar
  private lateinit var allInOneProgressBarMonth: ProgressBar
  private lateinit var allInOneProgressBarDay: ProgressBar
  private lateinit var allInOneProgressBarWeek: ProgressBar

  private lateinit var allInOneTitleTextViewYear: TextView
  private lateinit var allInOneTitleTextViewDay: TextView
  private lateinit var allInOneTitleTextViewMonth: TextView
  private lateinit var allInOneTitleTextViewWeek: TextView

  private var isSunriseSunsetDataAvailable = false

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    isSunriseSunsetDataAvailable = loadSunriseSunset(requireContext()) != null

    // Initialize and Load Ad

    billingManager.shouldShowAds.observe(viewLifecycleOwner) {
      when (it) {
        SubscriptionStatus.Subscribed -> {
          try {
            nativeAdView.destroy()
          } catch (ex: UninitializedPropertyAccessException) {
            Log.d("Initialization Error", ex.message.toString())
          }
        }
        SubscriptionStatus.Loading -> {}
        else -> showAds()
      }
    }

    // Show Widget Menu
    showWidgetMenu()

    // Initialize TextView and Progress Bar
    initProgressBarsTextViews(view)

    // Init Progress Bar and Text with animation
    startAnimationWidget()

    // Update Widget every 5 seconds
    updateWidgetInfo(1)
  }

  private fun updateStandaloneWidgetRemoteView(
      context: Context,
      container: FrameLayout,
      widgetType: TimePeriod
  ): View {

    val appContext = requireActivity().applicationContext

    val widgetRemoteView =
        StandaloneWidget.standaloneWidgetRemoteView(appContext, widgetType)
            .apply(appContext, container)
    widgetRemoteView.findViewById<FrameLayout>(R.id.background).setOnClickListener {}
    container.removeAllViews()
    container.addView(widgetRemoteView)

    return widgetRemoteView
  }

  private fun updateStandaloneWidgetRemoteView(
      context: Context,
      container: FrameLayout,
      dayLight: Boolean,
  ): View {

    val appContext = requireActivity().applicationContext

    val widgetRemoteView =
        DayNightWidget.dayNightLightWidgetRemoteView(appContext, dayLight)
            .apply(appContext, container)
    widgetRemoteView.findViewById<FrameLayout>(R.id.background).setOnClickListener {}
    container.removeAllViews()
    container.addView(widgetRemoteView)
    return widgetRemoteView
  }

  private fun updateWidgetInfo(i: Long) {
    lifecycleScope.launch(Dispatchers.IO) {
      delay(700) // Wait 700 millisecond for animation to complete
      while (true) {

        // Loads user preferences and set default values if not set

        val progressTextYear = calculateProgress(requireContext(), TimePeriod.YEAR)
        val progressTextMonth = calculateProgress(requireContext(), TimePeriod.MONTH)
        val progressTextDay = calculateProgress(requireContext(), TimePeriod.DAY)
        val progressTextWeek = calculateProgress(requireContext(), TimePeriod.WEEK)

        val progressYear = progressTextYear.roundToInt()
        val progressMonth = progressTextMonth.roundToInt()
        val progressDay = progressTextDay.roundToInt()
        val progressWeek = progressTextWeek.roundToInt()

        lifecycleScope.launch(Dispatchers.Main) {
          context?.let {
            updateStandaloneWidgetRemoteView(it, binding.widgetYearContainer, TimePeriod.YEAR)
            updateStandaloneWidgetRemoteView(it, binding.widgetMonthContainer, TimePeriod.MONTH)
            updateStandaloneWidgetRemoteView(it, binding.widgetWeekContainer, TimePeriod.WEEK)
            updateStandaloneWidgetRemoteView(it, binding.widgetDayContainer, TimePeriod.DAY)

            if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && isSunriseSunsetDataAvailable) {
              updateStandaloneWidgetRemoteView(it, binding.widgetDaylightContainer, true)
              updateStandaloneWidgetRemoteView(it, binding.widgetNightlightContainer, false)
            }
          }

          // All In One Widget
          allInOneProgressTextViewYear.text = (progressYear.toDouble().styleFormatted(0))
          allInOneProgressTextViewMonth.text = (progressMonth).toDouble().styleFormatted(0)
          allInOneProgressTextViewDay.text = (progressDay).toDouble().styleFormatted(0)
          allInOneProgressTextViewWeek.text = (progressWeek).toDouble().styleFormatted(0)

          allInOneProgressBarYear.progress = progressYear
          allInOneProgressBarMonth.progress = progressMonth
          allInOneProgressBarDay.progress = progressDay
          allInOneProgressBarWeek.progress = progressWeek

          val dayCurrentValue =
              getCurrentPeriodValue(TimePeriod.DAY).toFormattedTimePeriod(TimePeriod.DAY)
          val weekCurrentValue =
              getCurrentPeriodValue(TimePeriod.WEEK).toFormattedTimePeriod(TimePeriod.WEEK)
          val monthCurrentValue =
              getCurrentPeriodValue(TimePeriod.MONTH).toFormattedTimePeriod(TimePeriod.MONTH)
          val yearCurrentValue =
              getCurrentPeriodValue(TimePeriod.YEAR).toFormattedTimePeriod(TimePeriod.YEAR)

          allInOneTitleTextViewYear.text = yearCurrentValue
          allInOneTitleTextViewMonth.text = monthCurrentValue
          allInOneTitleTextViewDay.text = weekCurrentValue
          allInOneTitleTextViewWeek.text = dayCurrentValue
        }
        delay(i * 1000)
      }
    }
  }

  private fun startAnimationWidget() {

    val yearRemoteView =
        updateStandaloneWidgetRemoteView(
            requireContext(), binding.widgetYearContainer, TimePeriod.YEAR)

    val monthRemoteView =
        updateStandaloneWidgetRemoteView(
            requireContext(), binding.widgetMonthContainer, TimePeriod.MONTH)

    val weekRemoteView =
        updateStandaloneWidgetRemoteView(
            requireContext(), binding.widgetWeekContainer, TimePeriod.WEEK)

    val dayRemoteView =
        updateStandaloneWidgetRemoteView(
            requireContext(), binding.widgetDayContainer, TimePeriod.DAY)

    val dayLightRemoteView =
        updateStandaloneWidgetRemoteView(requireContext(), binding.widgetDaylightContainer, true)

    val nightLightRemoteView =
        updateStandaloneWidgetRemoteView(requireContext(), binding.widgetNightlightContainer, false)

    animatedUpdateProgressBarView(
        yearRemoteView.findViewById(R.id.widgetProgressBar), TimePeriod.YEAR)
    animatedUpdateProgressTextView(
        yearRemoteView.findViewById(R.id.widgetProgress), TimePeriod.YEAR)

    animatedUpdateProgressBarView(
        monthRemoteView.findViewById(R.id.widgetProgressBar), TimePeriod.MONTH)
    animatedUpdateProgressTextView(
        monthRemoteView.findViewById(R.id.widgetProgress), TimePeriod.MONTH)

    animatedUpdateProgressBarView(
        weekRemoteView.findViewById(R.id.widgetProgressBar), TimePeriod.WEEK)
    animatedUpdateProgressTextView(
        weekRemoteView.findViewById(R.id.widgetProgress), TimePeriod.WEEK)

    animatedUpdateProgressBarView(
        dayRemoteView.findViewById(R.id.widgetProgressBar), TimePeriod.DAY)
    animatedUpdateProgressTextView(dayRemoteView.findViewById(R.id.widgetProgress), TimePeriod.DAY)

    animatedUpdateProgressTextView(allInOneProgressTextViewYear, TimePeriod.YEAR, true)
    animatedUpdateProgressTextView(allInOneProgressTextViewMonth, TimePeriod.MONTH, true)
    animatedUpdateProgressTextView(allInOneProgressTextViewDay, TimePeriod.DAY, true)
    animatedUpdateProgressTextView(allInOneProgressTextViewWeek, TimePeriod.WEEK, true)

    animatedUpdateProgressBarView(allInOneProgressBarYear, TimePeriod.YEAR)
    animatedUpdateProgressBarView(allInOneProgressBarMonth, TimePeriod.MONTH)
    animatedUpdateProgressBarView(allInOneProgressBarDay, TimePeriod.DAY)
    animatedUpdateProgressBarView(allInOneProgressBarWeek, TimePeriod.WEEK)

    if (ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED && isSunriseSunsetDataAvailable) {
      animatedUpdateProgressTextView(
          dayLightRemoteView.findViewById(R.id.widgetProgress), TimePeriod.DAY, dayLight = true)
      animatedUpdateProgressBarView(
          dayLightRemoteView.findViewById(R.id.widgetProgressBar), TimePeriod.DAY, dayLight = true)

      animatedUpdateProgressTextView(
          nightLightRemoteView.findViewById(R.id.widgetProgress), TimePeriod.DAY, dayLight = false)
      animatedUpdateProgressBarView(
          nightLightRemoteView.findViewById(R.id.widgetProgressBar),
          TimePeriod.DAY,
          dayLight = false)
    }
  }

  private fun initProgressBarsTextViews(view: View) {
    allInOneProgressTextViewYear = view.findViewById<TextView>(R.id.progressTextYear)
    allInOneProgressTextViewMonth = view.findViewById<TextView>(R.id.progressTextMonth)
    allInOneProgressTextViewDay = view.findViewById<TextView>(R.id.progressTextDay)
    allInOneProgressTextViewWeek = view.findViewById<TextView>(R.id.progressTextWeek)

    allInOneProgressBarYear = view.findViewById<ProgressBar>(R.id.progressBarYear)
    allInOneProgressBarMonth = view.findViewById<ProgressBar>(R.id.progressBarMonth)
    allInOneProgressBarDay = view.findViewById<ProgressBar>(R.id.progressBarDay)
    allInOneProgressBarWeek = view.findViewById<ProgressBar>(R.id.progressBarWeek)

    allInOneTitleTextViewYear = view.findViewById<TextView>(R.id.progressYearTitle)
    allInOneTitleTextViewDay = view.findViewById<TextView>(R.id.progressTitle)
    allInOneTitleTextViewMonth = view.findViewById<TextView>(R.id.progressMonthTitle)
    allInOneTitleTextViewWeek = view.findViewById<TextView>(R.id.progressWeekTitle)
  }

  private fun showAds() {

    // Initialize Ad Loader
    val adFrame: LinearLayout = binding.adFrame
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
    // Load Ad
    adLoader.loadAd(AdRequest.Builder().build())
  }

  private fun showWidgetMenu() {
    // Showing menu for user to add Day widget to user Launcher's Home Screen
    binding.btnAddDayWidget.setOnClickListener {
      requestPinAppWidget(
          requireContext(),
          DayWidget::class.java,
          StandaloneWidget.standaloneWidgetRemoteView(requireContext(), TimePeriod.DAY))
    }

    // Showing menu for user to add Month widget to user Launcher's Home Screen
    binding.btnAddMonthWidget.setOnClickListener {
      requestPinAppWidget(
          requireContext(),
          MonthWidget::class.java,
          StandaloneWidget.standaloneWidgetRemoteView(requireContext(), TimePeriod.MONTH))
    }

    // Showing menu for user to add Year widget to user Launcher's Home Screen
    binding.btnAddYearWidget.setOnClickListener {
      requestPinAppWidget(
          requireContext(),
          YearWidget::class.java,
          StandaloneWidget.standaloneWidgetRemoteView(requireContext(), TimePeriod.YEAR))
    }

    // Showing menu for user to add Week widget to user Launcher's Home Screen
    binding.btnAddWeekWidget.setOnClickListener {
      requestPinAppWidget(
          requireContext(),
          WeekWidget::class.java,
          StandaloneWidget.standaloneWidgetRemoteView(requireContext(), TimePeriod.WEEK))
    }

    // Showing menu for user to add All In One widget to user Launcher's Home Screen
    binding.btnAddAllInOneWidget.setOnClickListener {
      requestPinAppWidget(
          requireContext(),
          AllInWidget::class.java,
          AllInWidget.AllInOneWidgetRemoteView(requireContext()))
    }

    binding.btnAddDaylightWidget.setOnClickListener {
      requestPinAppWidget(
          requireContext(),
          DayLightWidget::class.java,
          DayNightWidget.dayNightLightWidgetRemoteView(requireContext(), true))
    }

    binding.btnAddNightlightWidget.setOnClickListener {
      requestPinAppWidget(
          requireContext(),
          NightLightWidget::class.java,
          DayNightWidget.dayNightLightWidgetRemoteView(requireContext(), false))
    }
  }

  private fun animatedUpdateProgressTextView(
      textView: TextView,
      type: TimePeriod,
      isAllInOne: Boolean = false,
      dayLight: Boolean? = null
  ) {
    val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

    val decimalPlace: Int =
        pref.getInt(requireContext().getString(R.string.widget_widget_decimal_point), 2)

    var progressTextAnimator =
        if (isAllInOne) {
          ValueAnimator.ofInt(0, calculateProgress(requireContext(), type).roundToInt())
        } else {
          ValueAnimator.ofFloat(0F, calculateProgress(requireContext(), type).toFloat())
        }

    if (dayLight != null) {
      val sunriseSunsetResponse = loadSunriseSunset(requireContext()) ?: return
      val (startTime, endTime) = sunriseSunsetResponse.getStartAndEndTime(dayLight)
      val progress = calculateProgress(requireContext(), startTime, endTime)
      progressTextAnimator = ValueAnimator.ofFloat(0F, progress.toFloat())
    }

    progressTextAnimator.duration = 600
    progressTextAnimator.addUpdateListener {
      textView.text =
          if (isAllInOne) {
            (it.animatedValue as Int).toDouble().styleFormatted(0)
          } else {
            ((it.animatedValue as Float).toDouble().styleFormatted(decimalPlace))
          }
      textView.requestLayout()
    }
    progressTextAnimator.start()
  }

  private fun animatedUpdateProgressBarView(
      progressBarView: ProgressBar,
      type: TimePeriod,
      dayLight: Boolean? = null
  ) {
    var progressViewAnimator =
        ValueAnimator.ofInt(0, calculateProgress(requireContext(), type).roundToInt())

    if (dayLight != null) {
      val sunriseSunsetResponse = loadSunriseSunset(requireContext()) ?: return
      val (startTime, endTime) = sunriseSunsetResponse.getStartAndEndTime(dayLight)
      val progress = calculateProgress(requireContext(), startTime, endTime)
      progressViewAnimator = ValueAnimator.ofInt(0, progress.roundToInt())
    }

    progressViewAnimator.duration = 600
    progressViewAnimator.addUpdateListener {
      progressBarView.progress = it.animatedValue as Int
      progressBarView.requestLayout()
    }
    progressViewAnimator.start()
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

  /** https://sigute.medium.com/android-oreo-widget-pinning-in-kotlin-398d529eab28 */
  private fun requestPinAppWidget(
      context: Context,
      widget: Class<*>,
      remoteViews: RemoteViews? = null
  ) {
    val mAppWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
    val myProvider = ComponentName(requireContext(), widget)
    if (!mAppWidgetManager.isRequestPinAppWidgetSupported) {
      Toast.makeText(context, getString(R.string.unsupported_launcher), Toast.LENGTH_LONG).show()
      return
    }

    var bundle: Bundle? = null
    if (remoteViews != null) {
      bundle = Bundle()
      bundle.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, remoteViews)
    }

    mAppWidgetManager.requestPinAppWidget(myProvider, bundle, null)
  }
}
