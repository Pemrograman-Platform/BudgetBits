package com.app.budgetbits

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.budgetbits.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val powerConnectionReceiver = PowerConnectionReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Terapkan tema yang disimpan
        val sharedPrefs = getSharedPreferences("budgetbits_prefs", MODE_PRIVATE)
        val themeMode = sharedPrefs.getInt("THEME_MODE", 0)
        when (themeMode) {
            1 -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
            2 -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
            else -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), false)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment(), false)
                R.id.nav_history -> replaceFragment(HistoryFragment(), false)
                R.id.nav_profile -> replaceFragment(ProfileFragment(), false)
            }
            true
        }
    }

    fun replaceFragment(fragment: Fragment, isAppended: Boolean = false) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.setCustomAnimations(
            android.R.anim.fade_in,
            android.R.anim.fade_out,
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )

        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        
        if (isAppended) {
            fragmentTransaction.addToBackStack(null)
        }
        
        fragmentTransaction.commit()
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(powerConnectionReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(powerConnectionReceiver)
    }
}