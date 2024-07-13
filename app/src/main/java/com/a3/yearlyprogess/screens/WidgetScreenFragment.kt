package com.a3.yearlyprogess.screens

import android.animation.ValueAnimator
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.YearlyProgressManager
import com.a3.yearlyprogess.YearlyProgressManager.Companion.formatProgress
import com.a3.yearlyprogess.YearlyProgressManager.Companion.formatProgressStyle
import com.a3.yearlyprogess.ad.CustomAdView.Companion.updateViewWithNativeAdview
import com.a3.yearlyprogess.databinding.FragmentWidgetScreenBinding
import com.a3.yearlyprogess.widgets.manager.updateManager.WidgetUpdateAlarmHandler
import com.a3.yearlyprogess.widgets.ui.AllInWidget
import com.a3.yearlyprogess.widgets.ui.DayWidget
import com.a3.yearlyprogess.widgets.ui.MonthWidget
import com.a3.yearlyprogess.widgets.ui.StandaloneWidget
import com.a3.yearlyprogess.widgets.ui.WeekWidget
import com.a3.yearlyprogess.widgets.ui.YearWidget
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class WidgetScreenFragment : Fragment() {

    private var _binding: FragmentWidgetScreenBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize and Load Ad
        showAds()

        // Show Widget Menu
        showWidgetMenu()

        // Initialize TextView and Progress Bar
        initProgressBarsTextViews(view)

        // Init Progress Bar and Text with animation
        startAnimationWidget()

        // Update Widget every 5 seconds
        updateWidgetInfo(1)
    }

    private fun updateWidgetRemoteView(context: Context, container: FrameLayout, widgetType: Int): View {
        val widgetRemoteView =
            StandaloneWidget.standaloneWidgetRemoteView(context, widgetType)
                .apply(
                    activity, container
                )
        widgetRemoteView.findViewById<FrameLayout>(R.id.background).setOnClickListener {  }
        container.removeAllViews()
        container.addView(widgetRemoteView)

        return widgetRemoteView
    }


    private fun updateWidgetInfo(i: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(5000) // Wait 5 seconds for animation to complete
            while (true) {

                // Loads user preferences and set default values if not set
                YearlyProgressManager(requireContext()).setDefaultWeek()
                YearlyProgressManager(requireContext()).setDefaultCalculationMode()


                val progressTextYear = YearlyProgressManager.getProgress(YearlyProgressManager.YEAR)
                val progressTextMonth =
                    YearlyProgressManager.getProgress(YearlyProgressManager.MONTH)
                val progressTextDay = YearlyProgressManager.getProgress(YearlyProgressManager.DAY)
                val progressTextWeek = YearlyProgressManager.getProgress(YearlyProgressManager.WEEK)

                val progressYear = progressTextYear.roundToInt()
                val progressMonth = progressTextMonth.roundToInt()
                val progressDay = progressTextDay.roundToInt()
                val progressWeek = progressTextWeek.roundToInt()


                lifecycleScope.launch(Dispatchers.Main) {

                    context?.let {
                        updateWidgetRemoteView(
                            it,
                            binding.widgetYearContainer,
                            WidgetUpdateAlarmHandler.YEAR_WIDGET_SERVICE
                        )
                        updateWidgetRemoteView(
                            it,
                            binding.widgetMonthContainer,
                            WidgetUpdateAlarmHandler.MONTH_WIDGET_SERVICE
                        )
                        updateWidgetRemoteView(
                            it,
                            binding.widgetWeekContainer,
                            WidgetUpdateAlarmHandler.WEEK_WIDGET_SERVICE
                        )
                        updateWidgetRemoteView(
                            it,
                            binding.widgetDayContainer,
                            WidgetUpdateAlarmHandler.DAY_WIDGET_SERVICE
                        )
                    }


                    // All In One Widget
                    allInOneProgressTextViewYear.text = formatProgress(progressYear)
                    allInOneProgressTextViewMonth.text = formatProgress(progressMonth)
                    allInOneProgressTextViewDay.text = formatProgress(progressDay)
                    allInOneProgressTextViewWeek.text = formatProgress(progressWeek)

                    allInOneProgressBarYear.progress = progressYear
                    allInOneProgressBarMonth.progress = progressMonth
                    allInOneProgressBarDay.progress = progressDay
                    allInOneProgressBarWeek.progress = progressWeek

                    allInOneTitleTextViewYear.text = YearlyProgressManager.getYear().toString()
                    allInOneTitleTextViewMonth.text = YearlyProgressManager.getMonth(isLong = false)
                    allInOneTitleTextViewDay.text = YearlyProgressManager.getDay(formatted = true)
                    allInOneTitleTextViewWeek.text = YearlyProgressManager.getWeek(isLong = false)

                }
                delay(i * 1000)
            }
        }

    }

    private fun startAnimationWidget() {

        val yearRemoteView = updateWidgetRemoteView(
            requireContext(),
            binding.widgetYearContainer,
            WidgetUpdateAlarmHandler.YEAR_WIDGET_SERVICE
        )
        val monthRemoteView = updateWidgetRemoteView(
            requireContext(),
            binding.widgetMonthContainer,
            WidgetUpdateAlarmHandler.YEAR_WIDGET_SERVICE
        )
        val weekRemoteView = updateWidgetRemoteView(
            requireContext(),
            binding.widgetWeekContainer,
            WidgetUpdateAlarmHandler.YEAR_WIDGET_SERVICE
        )
        val dayRemoteView = updateWidgetRemoteView(
            requireContext(),
            binding.widgetDayContainer,
            WidgetUpdateAlarmHandler.YEAR_WIDGET_SERVICE
        )

        animatedUpdateProgressBarView(yearRemoteView.findViewById(R.id.widgetProgressBar), YearlyProgressManager.YEAR)
        animatedUpdateProgressTextView(yearRemoteView.findViewById(R.id.widgetProgress), YearlyProgressManager.YEAR)

        animatedUpdateProgressBarView(monthRemoteView.findViewById(R.id.widgetProgressBar), YearlyProgressManager.MONTH)
        animatedUpdateProgressTextView(monthRemoteView.findViewById(R.id.widgetProgress), YearlyProgressManager.MONTH)

        animatedUpdateProgressBarView(weekRemoteView.findViewById(R.id.widgetProgressBar), YearlyProgressManager.WEEK)
        animatedUpdateProgressTextView(weekRemoteView.findViewById(R.id.widgetProgress), YearlyProgressManager.WEEK)

        animatedUpdateProgressBarView(dayRemoteView.findViewById(R.id.widgetProgressBar), YearlyProgressManager.DAY)
        animatedUpdateProgressTextView(dayRemoteView.findViewById(R.id.widgetProgress), YearlyProgressManager.DAY)



        animatedUpdateProgressTextView(
            allInOneProgressTextViewYear, YearlyProgressManager.YEAR, true
        )
        animatedUpdateProgressTextView(
            allInOneProgressTextViewMonth, YearlyProgressManager.MONTH, true
        )
        animatedUpdateProgressTextView(allInOneProgressTextViewDay, YearlyProgressManager.DAY, true)
        animatedUpdateProgressTextView(
            allInOneProgressTextViewWeek, YearlyProgressManager.WEEK, true
        )

        animatedUpdateProgressBarView(allInOneProgressBarYear, YearlyProgressManager.YEAR)
        animatedUpdateProgressBarView(allInOneProgressBarMonth, YearlyProgressManager.MONTH)
        animatedUpdateProgressBarView(allInOneProgressBarDay, YearlyProgressManager.DAY)
        animatedUpdateProgressBarView(allInOneProgressBarWeek, YearlyProgressManager.WEEK)
    }

    private fun initProgressBarsTextViews(view: View) {
        YearlyProgressManager(requireContext()).setDefaultWeek()
        YearlyProgressManager(requireContext()).setDefaultCalculationMode()





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
        // Load Ad
        adLoader.loadAd(AdRequest.Builder().build())
    }


    private fun showWidgetMenu() {
        // Showing menu for user to add Day widget to user Launcher's Home Screen
        binding.btnAddDayWidget.setOnClickListener {
            requestPinAppWidget(
                requireContext(),
                DayWidget::class.java,
                WidgetUpdateAlarmHandler.DAY_WIDGET_SERVICE
            )
        }

        // Showing menu for user to add Month widget to user Launcher's Home Screen
        binding.btnAddMonthWidget.setOnClickListener {
            requestPinAppWidget(
                requireContext(),
                MonthWidget::class.java,
                WidgetUpdateAlarmHandler.MONTH_WIDGET_SERVICE
            )
        }

        // Showing menu for user to add Year widget to user Launcher's Home Screen
        binding.btnAddYearWidget.setOnClickListener {
            requestPinAppWidget(
                requireContext(),
                YearWidget::class.java,
                WidgetUpdateAlarmHandler.YEAR_WIDGET_SERVICE
            )
        }

        // Showing menu for user to add Week widget to user Launcher's Home Screen
        binding.btnAddWeekWidget.setOnClickListener {
            requestPinAppWidget(
                requireContext(),
                WeekWidget::class.java,
                WidgetUpdateAlarmHandler.WEEK_WIDGET_SERVICE
            )
        }

        // Showing menu for user to add All In One widget to user Launcher's Home Screen
        binding.btnAddAllInOneWidget.setOnClickListener {
            requestPinAppWidget(
                requireContext(),
                AllInWidget::class.java,
                WidgetUpdateAlarmHandler.ALL_IN_WIDGET_SERVICE
            )
        }
    }

    private fun animatedUpdateProgressTextView(
        textView: TextView, type: Int, isAllInOne: Boolean = false
    ) {
        val progressTextAnimator = if (isAllInOne) {
            ValueAnimator.ofInt(0, YearlyProgressManager.getProgress(type).roundToInt())
        } else {
            ValueAnimator.ofFloat(0F, YearlyProgressManager.getProgress(type).toFloat())
        }
        progressTextAnimator.duration = 600
        progressTextAnimator.addUpdateListener {
            textView.text = if (isAllInOne) {
                formatProgress(it.animatedValue as Int)
            } else {
                formatProgressStyle((it.animatedValue as Float).toDouble())
            }
            textView.requestLayout()
        }
        progressTextAnimator.start()
    }

    private fun animatedUpdateProgressBarView(progressBarView: ProgressBar, type: Int) {
        val progressViewAnimator =
            ValueAnimator.ofInt(0, YearlyProgressManager.getProgress(type).roundToInt())
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

    /**
     * https://sigute.medium.com/android-oreo-widget-pinning-in-kotlin-398d529eab28
     */
    private fun requestPinAppWidget(context: Context, widget: Class<*>, widgetServiceType: Int) {
        val mAppWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        val myProvider = ComponentName(requireContext(), widget)
        if (!mAppWidgetManager.isRequestPinAppWidgetSupported) {
            Toast.makeText(
                context, getString(R.string.unsupported_launcher), Toast.LENGTH_LONG
            ).show()
            return
        }

        var remoteViews: RemoteViews? = null

        if (widgetServiceType == WidgetUpdateAlarmHandler.ALL_IN_WIDGET_SERVICE) {
            remoteViews = AllInWidget.AllInOneWidgetRemoteView(context)
        } else if (widgetServiceType < WidgetUpdateAlarmHandler.ALL_IN_WIDGET_SERVICE) {
            remoteViews = StandaloneWidget.standaloneWidgetRemoteView(context, widgetServiceType)
        }

        var bundle: Bundle? = null
        if (remoteViews != null) {
            bundle = Bundle()
            bundle.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, remoteViews)
        }

        mAppWidgetManager.requestPinAppWidget(myProvider, bundle, null)
    }
}