package com.app.budgetbits

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.budgetbits.data.DatabaseHelper
import com.app.budgetbits.databinding.FragmentAddTransactionBinding
import com.app.budgetbits.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionFragment : Fragment(R.layout.fragment_add_transaction) {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddTransactionBinding.bind(view)
        dbHelper = DatabaseHelper(requireContext())

        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbPemasukan) {
                binding.btnSave.text = "Simpan Pemasukan"
                binding.etTitle.hint = "Sumber Pemasukan (mis: Gaji)"
            } else {
                binding.btnSave.text = "Simpan Pengeluaran"
                binding.etTitle.hint = "Nama Pengeluaran (mis: Nasi Goreng)"
            }
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val amountStr = binding.etAmount.text.toString()
            
            // Get Type
            val selectedTypeId = binding.rgType.checkedRadioButtonId
            val type = if (selectedTypeId == R.id.rbPemasukan) "Pemasukan" else "Pengeluaran"

            // Get Category
            val selectedCategoryId = binding.rgCategory.checkedRadioButtonId
            val radioButton = view.findViewById<RadioButton>(selectedCategoryId)
            val category = radioButton?.text.toString()

            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua kolom!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()?.toInt() ?: 0
            val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            
            val transaction = Transaction(
                id = 0,
                title = title,
                category = category,
                amount = amount,
                date = date,
                location = "Lokasi tidak ditentukan",
                type = type
            )

            val success = dbHelper.insertTransaction(transaction)
            if (success) {
                val msg = if (type == "Pemasukan") "Pemasukan berhasil ditambah!" else "Pengeluaran berhasil dicatat!"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Gagal menyimpan transaksi!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}