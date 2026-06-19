package com.app.budgetbits

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.budgetbits.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Kalau ditekan Daftar, langsung masuk ke Home/Beranda utama
        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // Flag ini gunanya supaya kalau pencet "Back" dari Beranda, dia gak balik ke form Daftar/Login lagi
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}