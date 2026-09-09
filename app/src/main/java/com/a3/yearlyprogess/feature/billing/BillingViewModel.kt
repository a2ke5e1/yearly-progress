package com.a3.yearlyprogess.feature.billing

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.feature.billing.model.PurchaseResult
import com.a3.yearlyprogess.feature.billing.model.PurchaseUiState
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    val products: StateFlow<List<ProductDetails>> = billingManager.productDetails

    private val _uiState = MutableStateFlow<PurchaseUiState>(PurchaseUiState.Idle)
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    init {
        billingManager.startConnection {
            viewModelScope.launch {
                billingManager.queryOneTimeProducts(listOf(BillingManager.PRODUCT_ID))
                val owned = billingManager.restorePurchases()
                if (owned.any { it.products.contains(BillingManager.PRODUCT_ID) }) {
                    _uiState.value = PurchaseUiState.Purchased
                }
            }
        }

        viewModelScope.launch {
            billingManager.purchaseEvents.collect { result ->
                _uiState.value = when (result) {
                    is PurchaseResult.Success -> PurchaseUiState.Purchased
                    is PurchaseResult.Error -> PurchaseUiState.Error(result.message)
                    PurchaseResult.Cancelled -> PurchaseUiState.Idle
                }
            }
        }
    }

    fun buy(
        activity: Activity,
        product: ProductDetails,
        offer: ProductDetails.OneTimePurchaseOfferDetails
    ) {
        billingManager.launchPurchaseFlow(
            activity = activity,
            productDetails = product,
            offer = offer
        )
    }
}