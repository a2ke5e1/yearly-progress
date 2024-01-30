package com.a3.yearlyprogess.components.dialogbox

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.a3.yearlyprogess.BuildConfig
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.DialogAboutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class AboutDialog : DialogFragment() {
    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            val inflater = requireActivity().layoutInflater
            val binding = DialogAboutBinding.inflate(inflater)

            binding.buildVersion.text = "Version: ${BuildConfig.VERSION_NAME}"
            binding.telegramLink.setOnClickListener {
                val url = "https://t.me/phycalc"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
                this.dismiss()
            }
            binding.shareLink.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                val shareMessage = """
            Check out ${getString(R.string.app_name)} (https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID} )
            You can have awesome widgets to see progress of day, month and year with material you support.
            """.trimIndent()
                intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                this.startActivity(Intent.createChooser(intent, "Share"))
                this.dismiss()
            }
            binding.dismissButton.setOnClickListener {
                dismiss()
            }

            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}