package com.newbie.trackmyspend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.newbie.trackmyspend.adapters.OnboardingAdapter
import com.newbie.trackmyspend.adapters.OnboardingItem
import com.newbie.trackmyspend.databinding.ActivityOnboardingBinding

class Onboarding : AppCompatActivity() {

    private lateinit var binding : ActivityOnboardingBinding
    private lateinit var dotsLayout: LinearLayout
    private lateinit var adapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupOnboardingItems()

        binding.nextButtonId.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < adapter.itemCount) {
                binding.viewPager.currentItem += 1
            } else {
                goToMainActivity()
            }
        }
        binding.previousButtonId.setOnClickListener {
            if (binding.viewPager.currentItem - 1 >= 0 ) {
                binding.viewPager.currentItem -= 1

            }
        }
    }

    private fun setupOnboardingItems() {
        val onboardingItems = listOf(
            OnboardingItem(1, R.drawable.display8, "Track Every Penny", "Easily record your spending, earnings, and transfers. Organize your finances by categories, dates, and custom clubs.", "#CC5F00"),
            OnboardingItem(2, R.drawable.display1  , "See What Matters", "Quickly view all transactions, filter them by category, or focus on specific clubs for a clearer understanding of your finances.", "#7700ff"),
            OnboardingItem(3, R.drawable.display6, "Speed Up Data Entry", "Create favorite entries for frequent transactions and autofill stored details with just a tap.", "#0077ff"),
            OnboardingItem(4, R.drawable._195527_3169208, "Ready to Take Control?", "Let’s simplify your expense tracking and make budgeting a breeze. Start now!", "#a00055")
        )

        adapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = adapter

    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        finish()
    }
}