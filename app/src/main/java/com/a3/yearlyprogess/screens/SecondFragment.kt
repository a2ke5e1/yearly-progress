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
import com.a3.yearlyprogess.databinding.FragmentSecondBinding
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.helper.format
import com.a3.yearlyprogess.mAdview.updateViewWithNativeAdview
import com.a3.yearlyprogess.mwidgets.DayWidget
import com.a3.yearlyprogess.mwidgets.MonthWidget
import com.a3.yearlyprogess.mwidgets.WeekWidget
import com.a3.yearlyprogess.mwidgets.YearWidget
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
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
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
        UpdateWidgetInfo(5)
    }

    private fun UpdateWidgetInfo(i: Long) {
        lifecycleScope.launch(Dispatchers.IO) {

            while (true) {

                val progressPercentage = ProgressPercentage()

                val progressTextYear = progressPercentage.getPercent(ProgressPercentage.YEAR)
                val progressTextMonth = progressPercentage.getPercent(ProgressPercentage.MONTH)
                val progressTextDay = progressPercentage.getPercent(ProgressPercentage.DAY)
                val progressTextWeek = progressPercentage.getPercent(ProgressPercentage.WEEK)

                val progressYear = progressTextYear.roundToInt()
                val progressMonth = progressTextMonth.roundToInt()
                val progressDay = progressTextDay.roundToInt()
                val progressWeek = progressTextWeek.roundToInt()


                lifecycleScope.launch(Dispatchers.Main) {
                    progressTextViewYear.text = percentString(progressTextYear)
                    progressTextViewMonth.text = percentString(progressTextMonth)
                    progressTextViewDay.text = percentString(progressTextDay)
                    progressTextViewWeek.text = percentString(progressTextWeek)

                    progressBarYear.progress = progressYear
                    progressBarMonth.progress = progressMonth
                    progressBarDay.progress = progressDay
                    progressBarWeek.progress = progressWeek

                    textViewYear.text = progressPercentage.getYear()
                    textViewMonth.text = progressPercentage.getMonth(str = true)
                    textViewWeek.text = progressPercentage.getWeek(str = true)
                    textViewDay.text = progressPercentage.getDay(custom = true)

                }
                delay(i * 1000)
            }
        }

    }

    private fun startAnimationWidget() {
        animatedUpdateProgressTextView(progressTextViewYear, ProgressPercentage.YEAR)
        animatedUpdateProgressTextView(progressTextViewMonth, ProgressPercentage.MONTH)
        animatedUpdateProgressTextView(progressTextViewDay, ProgressPercentage.DAY)
        animatedUpdateProgressTextView(progressTextViewWeek, ProgressPercentage.WEEK)

        animatedUpdateProgressBarView(progressBarYear, ProgressPercentage.YEAR)
        animatedUpdateProgressBarView(progressBarMonth, ProgressPercentage.MONTH)
        animatedUpdateProgressBarView(progressBarDay, ProgressPercentage.DAY)
        animatedUpdateProgressBarView(progressBarWeek, ProgressPercentage.WEEK)
    }

    private fun initProgressBarsTextViews(view: View) {
        progressTextViewYear = view.findViewById<TextView>(R.id.progress_text_year)
        progressTextViewMonth = view.findViewById<TextView>(R.id.progress_text_month)
        progressTextViewDay = view.findViewById<TextView>(R.id.progress_text_day)
        progressTextViewWeek = view.findViewById<TextView>(R.id.progress_text_week)

        progressBarYear = view.findViewById<ProgressBar>(R.id.progress_bar_year)
        progressBarMonth = view.findViewById<ProgressBar>(R.id.progress_bar_month)
        progressBarDay = view.findViewById<ProgressBar>(R.id.progress_bar_day)
        progressBarWeek = view.findViewById<ProgressBar>(R.id.progress_bar_week)

        textViewYear = view.findViewById<TextView>(R.id.text_year)
        textViewMonth = view.findViewById<TextView>(R.id.text_month)
        textViewDay = view.findViewById<TextView>(R.id.text_day)
        textViewWeek = view.findViewById<TextView>(R.id.text_week)
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
    }

    private fun animatedUpdateProgressTextView(textView: TextView, type: Int) {
        val progressTextAnimator =
            ValueAnimator.ofFloat(0F, ProgressPercentage().getPercent(type).toFloat())
        progressTextAnimator.duration = 600
        progressTextAnimator.addUpdateListener {
            textView.text = percentString((it.animatedValue as Float).toDouble())
            textView.requestLayout()
        }
        progressTextAnimator.start()
    }

    private fun animatedUpdateProgressBarView(progressBarView: ProgressBar, type: Int) {
        val progressViewAnimator =
            ValueAnimator.ofInt(0, ProgressPercentage().getPercent(type).roundToInt())
        progressViewAnimator.duration = 600
        progressViewAnimator.addUpdateListener {
            progressBarView.progress = it.animatedValue as Int
            progressBarView.requestLayout()
        }
        progressViewAnimator.start()
    }

    private fun percentString(progress: Double): String {
        return "${progress.format(2)}%"
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