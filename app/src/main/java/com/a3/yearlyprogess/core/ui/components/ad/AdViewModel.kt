package com.a3.yearlyprogess.core.ui.components.ad

import androidx.lifecycle.ViewModel
import com.a3.yearlyprogess.feature.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdViewModel @Inject constructor(
    billingManager: BillingManager
) : ViewModel() {
    val isAdFree = billingManager.isAdFree
}