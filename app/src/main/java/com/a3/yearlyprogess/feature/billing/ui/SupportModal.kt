package com.a3.yearlyprogess.feature.billing.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.a3.yearlyprogess.core.util.segmentedShapes
import com.a3.yearlyprogess.feature.billing.PurchaseViewModel
import com.a3.yearlyprogess.feature.billing.model.PurchaseUiState
import com.android.billingclient.api.ProductDetails


private fun tierDisplayName(purchaseOptionId: String?): String = when (purchaseOptionId) {
    "supporter" -> "Ad-Free Experience"
    "super-supporter" -> "Support Development"
    "support-more" -> "Support the Developer"
    else -> purchaseOptionId ?: "Donate"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportDeveloperModal(
    open: Boolean = false,
    onDismissRequest: () -> Unit = {},
) {
    if (!open) return
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Box {
            SupportDeveloperModalContent()
            IconButton(
                onClick = onDismissRequest,
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close dialog",
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
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (uiState) {
                is PurchaseUiState.Loading -> {
                    SupportHeader(
                        title = "Support the Developer",
                        subtitle = "Donate any amount to unlock an\nad-free experience forever",
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    LoadingContent()
                }

                is PurchaseUiState.Purchased -> {
                    PurchasedContent()
                }

                is PurchaseUiState.Error -> {
                    SupportHeader(
                        title = "Support the Developer",
                        subtitle = "Donate any amount to unlock an\nad-free experience forever",
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ErrorContent(
                        message = (uiState as PurchaseUiState.Error).message,
                        onRetry = { viewModel.retry() },
                    )
                }

                is PurchaseUiState.Idle -> {
                    SupportHeader(
                        title = "Support the Developer",
                        subtitle = "Donate any amount to unlock an\nad-free experience forever",
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Collect all offers across all products
                    val allOffers = products.flatMap { product ->
                        product.oneTimePurchaseOfferDetailsList
                            .orEmpty()
                            .map { offer -> product to offer }
                    }

                    if (allOffers.isEmpty()) {
                        UnavailableContent()
                    } else {
                        IdleContent(
                            offers = allOffers,
                            onBuy = { product, offer ->
                                activity?.let { viewModel.buy(it, product, offer) }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportHeader(
    title: String,
    subtitle: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Outlined.VolunteerActivism,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IdleContent(
    offers: List<Pair<ProductDetails, ProductDetails.OneTimePurchaseOfferDetails>>,
    onBuy: (ProductDetails, ProductDetails.OneTimePurchaseOfferDetails) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Donation button group using segmentedShapes
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            offers.forEachIndexed { index, (product, offer) ->
                val shape = segmentedShapes(index = index, count = offers.size)
                Button(
                    onClick = { onBuy(product, offer) },
                    shape = shape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = tierDisplayName(offer.purchaseOptionId),
                            style = MaterialTheme.typography.bodyMediumEmphasized,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = offer.formattedPrice,
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "One-time purchase · No subscription",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PurchasedContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Celebration,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Thank You!",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your generous donation means the world.\nYou now enjoy an ad-free experience forever!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You're already supporting us —\nthank you for being amazing!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UnavailableContent(
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Empty state card
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Donation options are currently unavailable.\nPlease try again later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = onRetry) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Try Again")
        }
    }
}