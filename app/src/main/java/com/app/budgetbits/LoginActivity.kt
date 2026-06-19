package com.app.budgetbits // Pastikan package ini sama dengan punyamu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.budgetbits.databinding.ActivityLoginBinding // Import ViewBinding

class LoginActivity : AppCompatActivity() {

    // Deklarasi variabel binding
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLogin.setOnClickListener {
            // Berpindah ke MainActivity menggunakan Intent
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }
        // Memberikan aksi pada teks "Daftar di sini"
        binding.tvDaftar.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            // Di sini kita tidak pakai finish() agar user bisa tekan tombol "Back" di HP untuk kembali ke Login
        }
    }
}