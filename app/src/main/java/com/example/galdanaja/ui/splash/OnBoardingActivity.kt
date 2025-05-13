package com.example.galdanaja.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.galdanaja.R
import com.example.galdanaja.databinding.ActivityOnBoardingBinding

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingBinding

    private val onboardingData = listOf(
        OnboardingItem(R.drawable.onboarding_1, R.string.onboarding_1, R.string.sub_onboarding1),
        OnboardingItem(R.drawable.onboarding_2, R.string.onboarding_2, R.string.sub_onboarding2),
        OnboardingItem(R.drawable.onboarding_3, R.string.onboarding_3, R.string.sub_onboarding3)
    )

    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tampilkan halaman pertama
        updateOnboardingPage()

        binding.skipButton.setOnClickListener {
            val intent = Intent(this, EndOnBoardingActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.nextButton.setOnClickListener {
            if (currentPage < onboardingData.size - 1) {
                currentPage++
                updateOnboardingPage()
            } else {
                val intent = Intent(this, EndOnBoardingActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun updateOnboardingPage() {
        val item = onboardingData[currentPage]
        binding.imageView3.setImageResource(item.imageRes)
        binding.textView2.setText(item.titleRes)
        binding.textView3.setText(item.descRes)

        updateDotIndicator()
    }

    data class OnboardingItem(
        val imageRes: Int,
        val titleRes: Int,
        val descRes: Int
    )



    private fun updateDotIndicator() {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3)
        dots.forEachIndexed { index, button ->
            button.isEnabled = (index == currentPage)
        }
    }

}
