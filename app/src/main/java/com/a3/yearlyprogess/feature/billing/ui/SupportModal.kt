package com.a3.yearlyprogess.feature.billing.ui


import android.app.Activity
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.BuildConfig
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.util.CommunityUtil
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.a3.yearlyprogess.feature.billing.PurchaseViewModel
import com.a3.yearlyprogess.feature.billing.model.PurchaseUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun  SupportDeveloperModal(
    open: Boolean = false,
    onDismissRequest: () -> Unit = {},
) {
    if (!open) return
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Box {
            SupportDeveloperModalContent()
            IconButton(
                onClick = onDismissRequest, modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    Icons.Default.Close, contentDescription = "Close about dialog box",
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SupportDeveloperModalContent() {
    val viewModel: PurchaseViewModel = hiltViewModel()
    val activity = LocalActivity.current
    val products by viewModel.products.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.clip(MaterialTheme.shapes.largeIncreased),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) { }


        Column(modifier = Modifier.padding(16.dp)) {
            when (val state = uiState) {
                is PurchaseUiState.Purchased -> Text("Purchase complete — content unlocked.")
                is PurchaseUiState.Error -> Text("Error: ${state.message}")
                PurchaseUiState.Idle -> {
                    if (products.isEmpty()) {
                        CircularProgressIndicator()
                    }
                }
            }

            products.forEach { product ->
                product.oneTimePurchaseOfferDetailsList
                    .orEmpty()
                    .forEach { offer ->
                        val purchaseOptionId = offer.purchaseOptionId

                        Button(
                            onClick = {
                                activity?.let {
                                    viewModel.buy(it, product, offer)
                                }
                            }
                        ) {
                            Text(
                                "Buy ${purchaseOptionId} — ${offer.formattedPrice}"
                            )
                        }
                    }
            }
        }

    }
}