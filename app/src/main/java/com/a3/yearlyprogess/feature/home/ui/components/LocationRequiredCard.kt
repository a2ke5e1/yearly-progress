package com.a3.yearlyprogess.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.interaction.PressAnimationConfig
import com.a3.yearlyprogess.core.ui.interaction.applyPressGesture
import com.a3.yearlyprogess.core.ui.interaction.rememberPressInteractionState
import com.a3.yearlyprogess.core.ui.style.CardCornerStyle

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LocationRequiredCard(
    onGoToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    cornerStyle: CardCornerStyle = CardCornerStyle.Default,
    pressConfig: PressAnimationConfig = PressAnimationConfig()
) {
    val pressState = rememberPressInteractionState(pressConfig)
    val animatedCorners = pressState.animateCorners(default = cornerStyle)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cornerStyle.toAnimatedShape(animatedCorners))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .applyPressGesture(pressState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.location_required),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.location_card_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            shapes = ButtonDefaults.shapes(
                shape = RoundedCornerShape(12.dp),
                pressedShape = RoundedCornerShape(24.dp)
            ),
            onClick = onGoToSettings,
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.settings),
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.location_card_tooltip),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}