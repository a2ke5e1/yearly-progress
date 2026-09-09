package com.a3.yearlyprogess.feature.billing.model

/**
 * UI-facing state for the purchase screen.
 */
sealed interface PurchaseUiState {
    object Loading : PurchaseUiState
    object Idle : PurchaseUiState
    object Purchased : PurchaseUiState
    data class Error(val message: String) : PurchaseUiState
}