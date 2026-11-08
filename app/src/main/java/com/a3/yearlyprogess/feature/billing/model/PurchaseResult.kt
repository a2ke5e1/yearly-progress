package com.a3.yearlyprogess.feature.billing.model

import com.android.billingclient.api.Purchase

/**
 * Result of a purchase attempt, emitted from BillingManager to observers (ViewModel).
 */
sealed interface PurchaseResult {
    data class Success(val purchase: Purchase) : PurchaseResult
    data class Error(val message: String) : PurchaseResult
    object Cancelled : PurchaseResult
}