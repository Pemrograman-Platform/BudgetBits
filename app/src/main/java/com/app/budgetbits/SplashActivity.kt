package com.app.budgetbits

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_splash)

        // Delay 2 detik (2000 ms) sebelum pindah ke screen berikutnya
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPrefs = getSharedPreferences("budgetbits_prefs", MODE_PRIVATE)
            val isLoggedIn = sharedPrefs.getBoolean("IS_LOGGED_IN", false)
            if (isLoggedIn) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }
}