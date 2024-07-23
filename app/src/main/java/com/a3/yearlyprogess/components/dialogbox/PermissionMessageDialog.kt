package com.a3.yearlyprogess.components.dialogbox

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.fragment.app.DialogFragment
import com.a3.yearlyprogess.BuildConfig
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.DialogPermissionBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class PermissionMessageDialog(
    @DrawableRes
    private val icon: Int,
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

            binding.neutralButton.setOnClickListener {
                dismiss()
            }

            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}