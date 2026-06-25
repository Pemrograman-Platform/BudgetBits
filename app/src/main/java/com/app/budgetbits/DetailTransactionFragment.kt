package com.app.budgetbits

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.app.budgetbits.databinding.FragmentDetailTransactionBinding
import java.text.NumberFormat
import java.util.Locale

class DetailTransactionFragment : Fragment(R.layout.fragment_detail_transaction) {

    private var _binding: FragmentDetailTransactionBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: com.app.budgetbits.data.DatabaseHelper

    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailTransactionBinding.bind(view)
        dbHelper = com.app.budgetbits.data.DatabaseHelper(requireContext())

        val id = arguments?.getInt("EXTRA_ID", 0) ?: 0
        val title = arguments?.getString("EXTRA_TITLE")
        val category = arguments?.getString("EXTRA_CATEGORY")
        val date = arguments?.getString("EXTRA_DATE")
        val amount = arguments?.getInt("EXTRA_AMOUNT", 0) ?: 0
        val location = arguments?.getString("EXTRA_LOCATION") ?: "Lokasi tidak diketahui"
        val type = arguments?.getString("EXTRA_TYPE") ?: "Pengeluaran"

        binding.tvDetailTitle.text = title
        binding.tvDetailCategory.text = category
        
        val formattedDate = try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            val parsedDate = inputFormat.parse(date ?: "")
            if (parsedDate != null) outputFormat.format(parsedDate) else date
        } catch (e: Exception) {
            date
        }
        binding.tvDetailDate.text = formattedDate
        binding.tvDetailLocation.text = location

        val amountText = rupiahFormat.format(amount).replace("Rp", "Rp ")
        if (type == "Pemasukan") {
            binding.tvDetailAmount.text = "+ $amountText"
            binding.tvDetailAmount.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        } else {
            binding.tvDetailAmount.text = "- $amountText"
            binding.tvDetailAmount.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnDelete.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Hapus Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus transaksi ini? Tindakan ini akan menyesuaikan saldo Anda kembali.")
                .setPositiveButton("Hapus") { _, _ ->
                    val success = dbHelper.deleteTransaction(id)
                    if (success) {
                        android.widget.Toast.makeText(requireContext(), "Transaksi berhasil dihapus!", android.widget.Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        android.widget.Toast.makeText(requireContext(), "Gagal menghapus transaksi!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        binding.btnEdit.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("EXTRA_ID", id)
                putString("EXTRA_TITLE", title)
                putString("EXTRA_CATEGORY", category)
                putString("EXTRA_DATE", date)
                putInt("EXTRA_AMOUNT", amount)
                putString("EXTRA_LOCATION", location)
                putString("EXTRA_TYPE", type)
                putBoolean("EXTRA_IS_EDIT", true)
            }
            val editFragment = AddTransactionFragment()
            editFragment.arguments = bundle
            (activity as? MainActivity)?.replaceFragment(editFragment, true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}