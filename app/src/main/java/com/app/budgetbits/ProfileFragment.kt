package com.app.budgetbits

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.app.budgetbits.data.DatabaseHelper
import com.app.budgetbits.databinding.FragmentProfileBinding

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        dbHelper = DatabaseHelper(requireContext())

        loadUserData()
        setupStatistics()
        setupClickListeners()
    }

    private fun loadUserData() {
        val sharedPrefs = requireContext().getSharedPreferences("budgetbits_prefs", android.content.Context.MODE_PRIVATE)
        val name = sharedPrefs.getString("USER_NAME", "Siti Aminah")
        val email = sharedPrefs.getString("USER_EMAIL", "siti.aminah@email.com")
        binding.tvProfileName.text = name
        binding.tvProfileEmail.text = email
    }

    private fun setupStatistics() {
        val transactions = dbHelper.getAllTransactions()
        binding.tvTotalTransactions.text = transactions.size.toString()
        
        // Menentukan kategori terbanyak (khusus pengeluaran)
        val favCategory = transactions.filter { it.type == "Pengeluaran" }
            .groupBy { it.category }
            .maxByOrNull { it.value.size }?.key ?: "-"
        binding.tvFavCategory.text = favCategory
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.btnSecurity.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnTheme.setOnClickListener {
            showThemeSelectionDialog()
        }

        binding.btnNotification.setOnClickListener {
            showNotificationSettingsDialog()
        }

        binding.btnHelp.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:support@budgetbits.com")
                putExtra(Intent.EXTRA_SUBJECT, "Bantuan Aplikasi BudgetBits")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Aplikasi email tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }

        // Logika untuk tombol Logout
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Keluar Akun")
                .setMessage("Apakah Anda yakin ingin keluar dari akun Anda?")
                .setPositiveButton("Ya") { _, _ ->
                    val sharedPrefs = requireContext().getSharedPreferences("budgetbits_prefs", android.content.Context.MODE_PRIVATE)
                    sharedPrefs.edit().clear().apply()

                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun showEditProfileDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Ubah Nama Profil")
        
        val input = android.widget.EditText(requireContext())
        input.setText(binding.tvProfileName.text)
        builder.setView(input)

        builder.setPositiveButton("Simpan") { _, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                val sharedPrefs = requireContext().getSharedPreferences("budgetbits_prefs", android.content.Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("USER_NAME", newName).apply()
                binding.tvProfileName.text = newName
                Toast.makeText(requireContext(), "Profil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Ubah Password")

        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val inputCurrent = android.widget.EditText(requireContext()).apply {
            hint = "Password Saat Ini"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val inputNew = android.widget.EditText(requireContext()).apply {
            hint = "Password Baru"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(inputCurrent)
        layout.addView(inputNew)
        builder.setView(layout)

        builder.setPositiveButton("Simpan") { _, _ ->
            val current = inputCurrent.text.toString().trim()
            val newPass = inputNew.text.toString().trim()

            val sharedPrefs = requireContext().getSharedPreferences("budgetbits_prefs", android.content.Context.MODE_PRIVATE)
            val savedPassword = sharedPrefs.getString("USER_PASSWORD", "")

            if (current.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(requireContext(), "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (savedPassword!!.isNotEmpty() && current != savedPassword) {
                Toast.makeText(requireContext(), "Password saat ini salah!", Toast.LENGTH_SHORT).show()
            } else {
                sharedPrefs.edit().putString("USER_PASSWORD", newPass).apply()
                Toast.makeText(requireContext(), "Password berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun showThemeSelectionDialog() {
        val sharedPrefs = requireContext().getSharedPreferences("budgetbits_prefs", android.content.Context.MODE_PRIVATE)
        val currentTheme = sharedPrefs.getInt("THEME_MODE", 0)
        
        val options = arrayOf("Mengikuti Sistem (Default)", "Mode Terang", "Mode Gelap")
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Tema Aplikasi")
            .setSingleChoiceItems(options, currentTheme) { dialog, which ->
                sharedPrefs.edit().putInt("THEME_MODE", which).apply()
                
                when (which) {
                    1 -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
                    2 -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
                    else -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                
                Toast.makeText(requireContext(), "Tema diubah!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showNotificationSettingsDialog() {
        val sharedPrefs = requireContext().getSharedPreferences("budgetbits_prefs", android.content.Context.MODE_PRIVATE)
        val isEnabled = sharedPrefs.getBoolean("NOTIFICATIONS_ENABLED", true)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Pengaturan Notifikasi")
            .setMessage("Apakah Anda ingin mengaktifkan pengingat harian untuk mencatat keuangan?")
            .setPositiveButton(if (isEnabled) "Nonaktifkan" else "Aktifkan") { _, _ ->
                sharedPrefs.edit().putBoolean("NOTIFICATIONS_ENABLED", !isEnabled).apply()
                val statusText = if (!isEnabled) "diaktifkan" else "dinonaktifkan"
                Toast.makeText(requireContext(), "Pengingat harian $statusText!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}