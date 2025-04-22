package com.a3.yearlyprogess.components.dialogbox

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.DialogPermissionBinding
import com.a3.yearlyprogess.ui.theme.YearlyProgressTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PermissionMessageDialog(
    @DrawableRes private val icon: Int,
    private val title: String,
    private val message: String,
    private val positiveButtonAction: (dialog: DialogFragment) -> Unit,
) : DialogFragment() {
  @SuppressLint("SetTextI18n")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return activity?.let {
      val builder = MaterialAlertDialogBuilder(it)
      val inflater = requireActivity().layoutInflater
      val binding = DialogPermissionBinding.inflate(inflater)

      binding.permissionIcon.setImageResource(icon)
      binding.permissionTitle.text = title
      binding.permissionMessage.text = message

      binding.positiveButton.setOnClickListener {
        positiveButtonAction(this)
        dismiss()
      }

      binding.neutralButton.setOnClickListener { dismiss() }

      builder.setView(binding.root)
      builder.create()
    } ?: throw IllegalStateException("Activity cannot be null")
  }
}

@Composable
fun PermissionRationalDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    iconPainter: Painter,
    title: String,
    body: String
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = {},
      text = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(
                  painter = iconPainter,
                  contentDescription = null,
                  tint = colorResource(R.color.widget_text_color))
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            color = colorResource(R.color.widget_text_color)))
                Text(text = body, style = MaterialTheme.typography.bodySmall)
              }
            }
      },
      confirmButton = {
        FilledTonalButton(onClick = onConfirm) { Text(stringResource(R.string.i_understand)) }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.dismiss)) }
      })
}

@Preview
@Composable
fun PermissionRationalDialogPreview() {
  YearlyProgressTheme {
    PermissionRationalDialog(
        onDismiss = {},
        onConfirm = {},
        iconPainter = painterResource(R.drawable.ic_outline_edit_calendar_24),
        title = stringResource(R.string.calendar_permission_title),
        body = stringResource(R.string.calendar_permission_message))
  }
}
