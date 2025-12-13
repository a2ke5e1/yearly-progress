package com.a3.yearlyprogess.core.util

import android.icu.util.ULocale
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.components.SelectableItem

@Composable
fun CalculationType.toSelectableItem(): SelectableItem<CalculationType> {
    val name = when (this) {
        CalculationType.ELAPSED -> stringResource(R.string.calculation_mode_elapsed)
        CalculationType.REMAINING -> stringResource(R.string.calculation_mode_remaining)
    }
    return SelectableItem(name = name, value = this)
}

@Composable
fun ULocale.toSelectableItem(): SelectableItem<ULocale> {
    val entries = stringArrayResource(R.array.app_calendar_type_entries)
    val values = stringArrayResource(R.array.app_calendar_type_values)

    val defaultULocale = ULocale.getDefault()
    val code = if (this == defaultULocale) {
        "default"
    } else {
        this.toString().substringAfter("@calendar=", "gregory")
    }

    val index = values.indexOf(code).takeIf { it >= 0 } ?: 0
    val name = entries[index]

    return SelectableItem(name = name, value = this)
}



