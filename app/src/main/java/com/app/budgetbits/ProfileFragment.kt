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

        setupStatistics()
        setupClickListeners()
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
            Toast.makeText(requireContext(), "Fitur Keamanan dalam pengembangan", Toast.LENGTH_SHORT).show()
        }

        binding.btnTheme.setOnClickListener {
            Toast.makeText(requireContext(), "Tema akan tersedia di versi berikutnya", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(requireContext(), "Pengaturan Notifikasi dibuka", Toast.LENGTH_SHORT).show()
        }

        binding.btnHelp.setOnClickListener {
            Toast.makeText(requireContext(), "Menghubungi Pusat Bantuan...", Toast.LENGTH_SHORT).show()
        }

        // Logika untuk tombol Logout
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Keluar")
                .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya") { _, _ ->
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
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
            val newName = input.text.toString()
            if (newName.isNotEmpty()) {
                binding.tvProfileName.text = newName
                Toast.makeText(requireContext(), "Profil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}