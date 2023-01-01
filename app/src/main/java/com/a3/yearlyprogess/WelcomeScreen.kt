package com.a3.yearlyprogess

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import androidx.appcompat.app.AppCompatActivity
import com.a3.yearlyprogess.databinding.ActivityFirstScreenBinding

class WelcomeScreen : AppCompatActivity() {

    private lateinit var binding: ActivityFirstScreenBinding
    private val TOS_URL = "https://www.a3group.co.in/yearly_progress/TOS"
    private val PP_URL = "https://www.a3group.co.in/yearly_progress/privacy_policy"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val TOSSpan = URLSpan(TOS_URL)
        val privacyPolicySpan = URLSpan(PP_URL)
        val pref = getSharedPreferences(MainActivity.YEARLY_PROGRESS_PREF, MODE_PRIVATE)
        val edit = pref.edit()

        val spannable = SpannableString(binding.textView2.text.toString())
        spannable.setSpan(
            TOSSpan,
            29,
            45,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            privacyPolicySpan,
            50,
            binding.textView2.text.length - 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.textView2.apply {
            text = spannable
            movementMethod = LinkMovementMethod.getInstance()
        }

        binding.materialButton.setOnClickListener {
            edit.putBoolean(MainActivity.FIRST_LAUNCH, false).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


    }
}