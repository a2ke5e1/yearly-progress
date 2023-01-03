package com.a3.yearlyprogess.screens

import android.animation.ValueAnimator
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.FragmentWidgetScreenBinding
import com.a3.yearlyprogess.helper.ProgressPercentage.Companion.formatProgress
import com.a3.yearlyprogess.helper.ProgressPercentage.Companion.formatProgressStyle
import com.a3.yearlyprogess.helper.ProgressPercentageV2
import com.a3.yearlyprogess.mAdview.CustomAdView.Companion.updateViewWithNativeAdview
import com.a3.yearlyprogess.mWidgets.*
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWidgetScreenBinding.inflate(inflater, container, false)
        return binding.root

    }

    private lateinit var adLoader: AdLoader
    private lateinit var nativeAdView: NativeAdView

    private lateinit var progressTextViewYear: TextView
    private lateinit var progressTextViewMonth: TextView
    private lateinit var progressTextViewDay: TextView
    private lateinit var progressTextViewWeek: TextView

    private lateinit var textViewYear: TextView
    private lateinit var textViewDay: TextView
    private lateinit var textViewMonth: TextView
    private lateinit var textViewWeek: TextView

    private lateinit var progressBarYear: ProgressBar
    private lateinit var progressBarMonth: ProgressBar
    private lateinit var progressBarDay: ProgressBar
    private lateinit var progressBarWeek: ProgressBar

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
        updateWidgetInfo(5)
    }

    private fun updateWidgetInfo(i: Long) {
        lifecycleScope.launch(Dispatchers.IO) {

            while (true) {


                val progressTextYear = ProgressPercentageV2.getProgress(ProgressPercentageV2.YEAR)
                val progressTextMonth = ProgressPercentageV2.getProgress(ProgressPercentageV2.MONTH)
                val progressTextDay = ProgressPercentageV2.getProgress(ProgressPercentageV2.DAY)
                val progressTextWeek = ProgressPercentageV2.getProgress(ProgressPercentageV2.WEEK)

                val progressYear = progressTextYear.roundToInt()
                val progressMonth = progressTextMonth.roundToInt()
                val progressDay = progressTextDay.roundToInt()
                val progressWeek = progressTextWeek.roundToInt()


                lifecycleScope.launch(Dispatchers.Main) {
                    progressTextViewYear.text = formatProgressStyle(progressTextYear)
                    progressTextViewMonth.text = formatProgressStyle(progressTextMonth)
                    progressTextViewDay.text = formatProgressStyle(progressTextDay)
                    progressTextViewWeek.text = formatProgressStyle(progressTextWeek)

                    progressBarYear.progress = progressYear
                    progressBarMonth.progress = progressMonth
                    progressBarDay.progress = progressDay
                    progressBarWeek.progress = progressWeek

                    textViewYear.text = ProgressPercentageV2.getYear().toString()
                    textViewMonth.text = ProgressPercentageV2.getMonth(isLong = false)
                    textViewWeek.text = ProgressPercentageV2.getWeek(isLong = false)
                    textViewDay.text = ProgressPercentageV2.getDay(formatted = true)


                    // All In One Widget
                    allInOneProgressTextViewYear.text = formatProgress(progressYear)
                    allInOneProgressTextViewMonth.text = formatProgress(progressMonth)
                    allInOneProgressTextViewDay.text = formatProgress(progressDay)
                    allInOneProgressTextViewWeek.text = formatProgress(progressWeek)

                    allInOneProgressBarYear.progress = progressYear
                    allInOneProgressBarMonth.progress = progressMonth
                    allInOneProgressBarDay.progress = progressDay
                    allInOneProgressBarWeek.progress = progressWeek

                    allInOneTitleTextViewYear.text = ProgressPercentageV2.getYear().toString()
                    allInOneTitleTextViewMonth.text = ProgressPercentageV2.getMonth(isLong = false)
                    allInOneTitleTextViewDay.text = ProgressPercentageV2.getDay(formatted = true)
                    allInOneTitleTextViewWeek.text = ProgressPercentageV2.getWeek(isLong = false)

                }
                delay(i * 1000)
            }
        }

    }

    private fun startAnimationWidget() {
        animatedUpdateProgressTextView(progressTextViewYear, ProgressPercentageV2.YEAR)
        animatedUpdateProgressTextView(progressTextViewMonth, ProgressPercentageV2.MONTH)
        animatedUpdateProgressTextView(progressTextViewDay, ProgressPercentageV2.DAY)
        animatedUpdateProgressTextView(progressTextViewWeek, ProgressPercentageV2.WEEK)

        animatedUpdateProgressBarView(progressBarYear, ProgressPercentageV2.YEAR)
        animatedUpdateProgressBarView(progressBarMonth, ProgressPercentageV2.MONTH)
        animatedUpdateProgressBarView(progressBarDay, ProgressPercentageV2.DAY)
        animatedUpdateProgressBarView(progressBarWeek, ProgressPercentageV2.WEEK)

        animatedUpdateProgressTextView(
            allInOneProgressTextViewYear,
            ProgressPercentageV2.YEAR,
            true
        )
        animatedUpdateProgressTextView(
            allInOneProgressTextViewMonth,
            ProgressPercentageV2.MONTH,
            true
        )
        animatedUpdateProgressTextView(allInOneProgressTextViewDay, ProgressPercentageV2.DAY, true)
        animatedUpdateProgressTextView(
            allInOneProgressTextViewWeek,
            ProgressPercentageV2.WEEK,
            true
        )

        animatedUpdateProgressBarView(allInOneProgressBarYear, ProgressPercentageV2.YEAR)
        animatedUpdateProgressBarView(allInOneProgressBarMonth, ProgressPercentageV2.MONTH)
        animatedUpdateProgressBarView(allInOneProgressBarDay, ProgressPercentageV2.DAY)
        animatedUpdateProgressBarView(allInOneProgressBarWeek, ProgressPercentageV2.WEEK)
    }

    private fun initProgressBarsTextViews(view: View) {

        binding.widgetYearDemo.findViewById<TextView>(R.id.widgetType).text =
            context?.getString(R.string.year)
        binding.widgetMonthDemo.findViewById<TextView>(R.id.widgetType).text =
            context?.getString(R.string.month)
        binding.widgetWeekDemo.findViewById<TextView>(R.id.widgetType).text =
            context?.getString(R.string.week)
        binding.widgetDayDemo.findViewById<TextView>(R.id.widgetType).text =
            context?.getString(R.string.day)

        progressTextViewDay = binding.widgetDayDemo.findViewById<TextView>(R.id.widgetProgress)
        progressTextViewWeek = binding.widgetWeekDemo.findViewById<TextView>(R.id.widgetProgress)
        progressTextViewMonth = binding.widgetMonthDemo.findViewById<TextView>(R.id.widgetProgress)
        progressTextViewYear = binding.widgetYearDemo.findViewById<TextView>(R.id.widgetProgress)

        textViewYear = binding.widgetYearDemo.findViewById<TextView>(R.id.widgetCurrentValue)
        textViewMonth = binding.widgetMonthDemo.findViewById<TextView>(R.id.widgetCurrentValue)
        textViewDay = binding.widgetDayDemo.findViewById<TextView>(R.id.widgetCurrentValue)
        textViewWeek = binding.widgetWeekDemo.findViewById<TextView>(R.id.widgetCurrentValue)

        progressBarYear = binding.widgetYearDemo.findViewById<ProgressBar>(R.id.widgetProgressBar)
        progressBarMonth = binding.widgetMonthDemo.findViewById<ProgressBar>(R.id.widgetProgressBar)
        progressBarDay = binding.widgetDayDemo.findViewById<ProgressBar>(R.id.widgetProgressBar)
        progressBarWeek = binding.widgetWeekDemo.findViewById<ProgressBar>(R.id.widgetProgressBar)



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
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Handle the failure by logging, altering the UI, and so on.
                    adFrame.removeAllViews()
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_BOTTOM_RIGHT)
                    // Methods in the NativeAdOptions.Builder class can be
                    // used here to specify individual options settings.
                    .build()
            )
            .build()
        // Load Ad
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun showWidgetMenu() {
        // Showing menu for user to add Day widget to user Launcher's Home Screen
        binding.btnAddDayWidget.setOnClickListener {
            requestPinAppWidget(requireContext(), DayWidget::class.java)
        }

        // Showing menu for user to add Month widget to user Launcher's Home Screen
        binding.btnAddMonthWidget.setOnClickListener {
            requestPinAppWidget(requireContext(), MonthWidget::class.java)
        }

        // Showing menu for user to add Year widget to user Launcher's Home Screen
        binding.btnAddYearWidget.setOnClickListener {
            requestPinAppWidget(requireContext(), YearWidget::class.java)
        }

        // Showing menu for user to add Week widget to user Launcher's Home Screen
        binding.btnAddWeekWidget.setOnClickListener {
            requestPinAppWidget(requireContext(), WeekWidget::class.java)
        }

        // Showing menu for user to add All In One widget to user Launcher's Home Screen
        binding.btnAddAllInOneWidget.setOnClickListener {
            requestPinAppWidget(requireContext(), AllInWidget::class.java)
        }
    }

    private fun animatedUpdateProgressTextView(
        textView: TextView,
        type: Int,
        isAllInOne: Boolean = false
    ) {
        val progressTextAnimator =
            if (isAllInOne) {
                ValueAnimator.ofInt(0, ProgressPercentageV2.getProgress(type).roundToInt())
            } else {
                ValueAnimator.ofFloat(0F, ProgressPercentageV2.getProgress(type).toFloat())
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
            ValueAnimator.ofInt(0, ProgressPercentageV2.getProgress(type).roundToInt())
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

    private fun requestPinAppWidget(context: Context, widget: Class<*>) {

        val unsupportedLauncherMessage =
            "Your Launcher does not support this feature. Please add Widget manually"

        val unsupportedLauncherToast =
            Toast.makeText(context, unsupportedLauncherMessage, Toast.LENGTH_LONG)


        val mAppWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)

        val myProvider = ComponentName(requireContext(), widget)
        if (mAppWidgetManager.isRequestPinAppWidgetSupported) {
            val pinnedWidgetCallbackIntent = Intent(context, widget)
            val successCallback = PendingIntent.getBroadcast(
                context,
                0,
                pinnedWidgetCallbackIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            mAppWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
        } else {
            unsupportedLauncherToast.show()
        }
    }
}