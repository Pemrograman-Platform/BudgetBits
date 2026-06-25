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
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                android.widget.Toast.makeText(this, "Harap isi email/username dan password!", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPrefs = getSharedPreferences("budgetbits_prefs", MODE_PRIVATE)
            val registeredEmail = sharedPrefs.getString("USER_EMAIL", "")
            val registeredPassword = sharedPrefs.getString("USER_PASSWORD", "")

            // Jika sudah ada user terdaftar, lakukan validasi
            if (!registeredEmail.isNullOrEmpty() && !registeredPassword.isNullOrEmpty()) {
                val registeredUser = registeredEmail.substringBefore("@")
                if (username != registeredEmail && username != registeredUser) {
                    android.widget.Toast.makeText(this, "Username atau Email salah!", android.widget.Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (password != registeredPassword) {
                    android.widget.Toast.makeText(this, "Password salah!", android.widget.Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else {
                // Jika belum ada user terdaftar (baru pasang app), buat credential baru dari apa yang diinputkan
                sharedPrefs.edit().putString("USER_PASSWORD", password).apply()
            }

            val displayName = if (username.contains("@")) username.substringBefore("@") else username

            sharedPrefs.edit().apply {
                putBoolean("IS_LOGGED_IN", true)
                putString("USER_NAME", displayName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
                putString("USER_EMAIL", if (username.contains("@")) username else "$username@email.com")
                apply()
            }

            // Berpindah ke MainActivity menggunakan Intent
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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