package com.a3.yearlyprogess.feature.billing

import com.a3.yearlyprogess.feature.billing.model.PurchaseResult
import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide wrapper around Google Play Billing for one-time (non-consumable) products.
 *
 * This is a client-only implementation (no backend): purchases are acknowledged
 * directly via BillingClient.acknowledgePurchase() after checking isAcknowledged,
 * per Google's guidance for apps without a secure backend. No purchase signature
 * verification is performed, so a modified APK could in theory inject a forged
 * purchase object — acceptable risk for many small apps, but worth adding back
 * (see purchase.signature / purchase.originalJson) if the product is high-value.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    // Dedicated scope since this is a singleton with no natural lifecycle owner.
    // SupervisorJob so one failed purchase-processing coroutine doesn't cancel others.
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>(extraBufferCapacity = 1)
    val purchaseEvents: SharedFlow<PurchaseResult> = _purchaseEvents.asSharedFlow()

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails: StateFlow<List<ProductDetails>> = _productDetails.asStateFlow()

    private val _isAdFree = MutableStateFlow(false)
    val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()


    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .enableAutoServiceReconnection()
        .build()

    /**
     * Starts the billing connection if not already connected/ready.
     * Safe to call multiple times (e.g. from recomposition) — guarded by isReady.
     */
    fun startConnection(onReady: () -> Unit = {}) {
        if (billingClient.isReady) {
            onReady()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    onReady()
                } else {
                    managerScope.launch {
                        _purchaseEvents.emit(PurchaseResult.Error(billingResult.debugMessage))
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // No-op: enableAutoServiceReconnection() handles retries internally.
                // Use this only for logging/UI state if you want to reflect
                // "disconnected" somewhere — do not call startConnection() here.
                Log.d(TAG, "Billing service disconnected")
            }
        })
    }

    /**
     * Query product details for one-time (INAPP) products by product ID,
     * as configured in Play Console.
     */
    suspend fun queryOneTimeProducts(productIds: List<String>) {
        val products = productIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()

        val result = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params)
        }

        if (result.billingResult.responseCode == BillingResponseCode.OK) {
            _productDetails.value = result.productDetailsList ?: emptyList()
        } else {
            _purchaseEvents.emit(PurchaseResult.Error(result.billingResult.debugMessage))
        }
    }

    /**
     * Launches the Play purchase flow for the given product. Must be called
     * with a live Activity (required by the Billing Library) — pass it in at
     * call time from the UI layer rather than holding a reference long-term.
     */
    fun launchPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offer: ProductDetails.OneTimePurchaseOfferDetails
    ) {

        val offerToken = offer.offerToken
            ?: run {
                managerScope.launch {
                    _purchaseEvents.emit(
                        PurchaseResult.Error(
                            "Purchase option is missing an offer token."
                        )
                    )
                }
                return
            }

        val productDetailsParams =
            BillingFlowParams.ProductDetailsParams
                .newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()

        val billingFlowParams =
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(productDetailsParams)
                )
                .build()

        val result = billingClient.launchBillingFlow(
            activity,
            billingFlowParams
        )

        if (result.responseCode != BillingResponseCode.OK) {
            managerScope.launch {
                _purchaseEvents.emit(
                    PurchaseResult.Error(result.debugMessage)
                )
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    managerScope.launch {
                        processPurchase(purchase)
                    }
                }
            }
            BillingResponseCode.USER_CANCELED -> {
                managerScope.launch { _purchaseEvents.emit(PurchaseResult.Cancelled) }
            }
            else -> {
                managerScope.launch {
                    _purchaseEvents.emit(PurchaseResult.Error(billingResult.debugMessage))
                }
            }
        }
    }

    private suspend fun processPurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            return
        }

        val result = acknowledgeIfNeeded(purchase)
        if (result is PurchaseResult.Success &&
            purchase.products.contains(PRODUCT_ID)
        ) {
            _isAdFree.value = true
        }
        _purchaseEvents.emit(result)
    }

    /**
     * Acknowledges a purchase if it hasn't been acknowledged already.
     * Client-only acknowledgement path, per Google's guidance for apps
     * without a secure backend.
     */
    suspend fun acknowledgeIfNeeded(purchase: Purchase): PurchaseResult {
        if (purchase.isAcknowledged) {
            return PurchaseResult.Success(purchase)
        }

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        val result = withContext(Dispatchers.IO) {
            billingClient.acknowledgePurchase(params)
        }

        return if (result.responseCode == BillingResponseCode.OK) {
            PurchaseResult.Success(purchase)
        } else {
            PurchaseResult.Error(result.debugMessage)
        }
    }

    /**
     * Queries existing (already-owned) one-time purchases. Call this on app
     * start to restore entitlements after reinstall/new device, and to catch
     * purchases that succeeded but never got acknowledged (e.g. app killed
     * mid-flow). This — not a locally stored flag — should be your source of
     * truth for whether content is unlocked, since Google can revoke/refund
     * independently of your app's local state.
     */
    suspend fun restorePurchases(): List<Purchase> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val result = withContext(Dispatchers.IO) {
            billingClient.queryPurchasesAsync(params)
        }

        result.purchasesList.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                !purchase.isAcknowledged
            ) {
                acknowledgeIfNeeded(purchase)
            }
        }

        _isAdFree.value = result.purchasesList.any { purchase ->
            purchase.products.contains(PRODUCT_ID) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }

        return result.purchasesList
    }

    /**
     * Optional explicit teardown. For most apps this isn't necessary — process
     * death cleans up the BillingClient with nothing else outliving it. Only
     * call this if you've wired it to a real "app is fully closing" signal
     * (e.g. ActivityLifecycleCallbacks tracking the last activity destroyed).
     */
    fun endConnection() {
        managerScope.cancel()
        billingClient.endConnection()
    }

    companion object {
        private const val TAG = "BillingManager"
        const val PRODUCT_ID = "ad_free_support"

    }
}