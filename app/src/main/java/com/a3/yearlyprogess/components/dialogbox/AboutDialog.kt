package com.a3.yearlyprogess.components.dialogbox

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.a3.yearlyprogess.BuildConfig
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.DialogAboutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

data class Credits(val name: String, val username: String, val language: String) {
  fun getClickableCredit(): SpannableString {
    val credit = SpannableString("$name - $language")
    val link = "https://t.me/$username"
    val urlSpan = URLSpan(link)
    credit.setSpan(urlSpan, 0, credit.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return credit
  }
}

class AboutDialog : DialogFragment() {
  private val credits =
      listOf(
          Credits("ASG13043", "ASG13043", "हिंदी"),
          Credits("mojienjoyment", "mojienjoyment", "فارسی"),
          Credits("Matteo", "Sgattocuki", "Italian"))

  @SuppressLint("SetTextI18n")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return activity?.let {
      val builder = MaterialAlertDialogBuilder(it)
      val inflater = requireActivity().layoutInflater
      val binding = DialogAboutBinding.inflate(inflater)

      binding.buildVersion.text =
          ContextCompat.getString(requireContext(), R.string.version) +
              " ${BuildConfig.VERSION_NAME}"
      val creditsText = SpannableStringBuilder("")
      for (credit in credits) {
        creditsText.append(credit.getClickableCredit())
        creditsText.append("\n")
      }
      binding.translationCredit.text = creditsText
      binding.translationCredit.movementMethod = LinkMovementMethod.getInstance()

      val githubLink = SpannableString("Source code available on GitHub")
      val urlSpan = URLSpan("https://github.com/a2ke5e1/yearly-progress")
      githubLink.setSpan(urlSpan, 0, githubLink.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

      binding.sourceCode.text = githubLink
      binding.sourceCode.movementMethod = LinkMovementMethod.getInstance()

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
        val shareMessage =
            """
            Check out ${getString(R.string.app_name)} (https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID} )
            You can have awesome widgets to see progress of day, month and year with material you support.
            """
                .trimIndent()
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        this.startActivity(Intent.createChooser(intent, "Share"))
        this.dismiss()
      }
      binding.dismissButton.setOnClickListener { dismiss() }

      builder.setView(binding.root)
      builder.create()
    } ?: throw IllegalStateException("Activity cannot be null")
  }
}
