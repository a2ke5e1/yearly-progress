package com.a3.yearlyprogess.core.util

import android.app.Activity
import android.content.Context
import com.a3.yearlyprogess.BuildConfig
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Interface to provide flexibility in consent management.
 * If you want to change the starting point or implementation later,
 * you can create a new class implementing this interface.
 */
interface IConsentManager {
    fun gatherConsent(
        activity: Activity,
        onConsentGathered: (Error?) -> Unit
    )
    fun isPrivacyOptionsRequired(): Boolean
    fun showPrivacyOptionsForm(
        activity: Activity,
        onFormDismissed: (Error?) -> Unit
    )
    fun canRequestAds(): Boolean
}

/**
 * Implementation of Google's User Messaging Platform (UMP).
 */
class ConsentManager(context: Context) : IConsentManager {

    companion object {
        private const val TEST_DEVICE_ID = "231158506D258DB9BEC8E8086377082B"
    }

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    /**
     * Helper variable to determine if ads can be requested.
     */
    override fun canRequestAds(): Boolean {
        return consentInformation.canRequestAds()
    }

    /**
     * Helper variable to determine if privacy options are required.
     */
    override fun isPrivacyOptionsRequired(): Boolean {
        return consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    /**
     * Standard gathering process for UMP.
     */
    override fun gatherConsent(
        activity: Activity,
        onConsentGathered: (Error?) -> Unit
    ) {
        // For testing purposes, you can uncomment this to force a geography or reset consent
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId(TEST_DEVICE_ID)
            .build()

        val params = ConsentRequestParameters.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    setConsentDebugSettings(debugSettings)
                }
            }
            .build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    // Consent has been gathered.
                    onConsentGathered(
                        if (formError != null) Error("${formError.errorCode}: ${formError.message}") else null
                    )
                }
            },
            { requestConsentError ->
                onConsentGathered(Error("${requestConsentError.errorCode}: ${requestConsentError.message}"))
            }
        )
    }

    /**
     * Shows the privacy options form (typically from a settings menu).
     */
    override fun showPrivacyOptionsForm(
        activity: Activity,
        onFormDismissed: (Error?) -> Unit
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            onFormDismissed(
                if (formError != null) Error("${formError.errorCode}: ${formError.message}") else null
            )
        }
    }
}
