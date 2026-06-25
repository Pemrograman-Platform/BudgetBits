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

        val isEdit = arguments?.getBoolean("EXTRA_IS_EDIT", false) ?: false
        val editId = arguments?.getInt("EXTRA_ID", 0) ?: 0
        val editDate = arguments?.getString("EXTRA_DATE") ?: ""

        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            if (isEdit) {
                binding.btnSave.text = "Perbarui Transaksi"
            } else {
                if (checkedId == R.id.rbPemasukan) {
                    binding.btnSave.text = "Simpan Pemasukan"
                    binding.etTitle.hint = "Sumber Pemasukan (mis: Gaji)"
                } else {
                    binding.btnSave.text = "Simpan Pengeluaran"
                    binding.etTitle.hint = "Nama Pengeluaran (mis: Nasi Goreng)"
                }
            }
        }

        if (isEdit) {
            binding.etTitle.setText(arguments?.getString("EXTRA_TITLE"))
            binding.etAmount.setText(arguments?.getInt("EXTRA_AMOUNT", 0).toString())
            
            val editType = arguments?.getString("EXTRA_TYPE")
            if (editType == "Pemasukan") {
                binding.rbPemasukan.isChecked = true
            } else {
                binding.rbPengeluaran.isChecked = true
            }
            
            val editCategory = arguments?.getString("EXTRA_CATEGORY")
            for (i in 0 until binding.rgCategory.childCount) {
                val child = binding.rgCategory.getChildAt(i)
                if (child is RadioButton && child.text.toString() == editCategory) {
                    child.isChecked = true
                    break
                }
            }
            binding.btnSave.text = "Perbarui Transaksi"
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val amountStr = binding.etAmount.text.toString().trim()
            
            // Get Type
            val selectedTypeId = binding.rgType.checkedRadioButtonId
            val type = if (selectedTypeId == R.id.rbPemasukan) "Pemasukan" else "Pengeluaran"

            // Get Category
            val selectedCategoryId = binding.rgCategory.checkedRadioButtonId
            if (selectedCategoryId == -1) {
                Toast.makeText(requireContext(), "Harap pilih kategori transaksi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val radioButton = view.findViewById<RadioButton>(selectedCategoryId)
            val category = radioButton?.text.toString()

            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua kolom!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()?.toInt() ?: 0

            // Cek Batas Anggaran (Budget Limit Warning)
            val budget = dbHelper.getIntConfig(com.app.budgetbits.data.DatabaseHelper.KEY_BUDGET)
            val currentExpense = dbHelper.getTotalExpenseThisMonth()

            val originalAmount = if (isEdit) arguments?.getInt("EXTRA_AMOUNT", 0) ?: 0 else 0
            val originalType = if (isEdit) arguments?.getString("EXTRA_TYPE") ?: "Pengeluaran" else "Pengeluaran"

            val expenseAfterThis = if (type == "Pengeluaran") {
                val base = currentExpense - (if (originalType == "Pengeluaran") originalAmount else 0)
                base + amount
            } else {
                currentExpense
            }

            val isExceeding = type == "Pengeluaran" && budget > 0 && expenseAfterThis > budget

            if (isExceeding) {
                val excess = expenseAfterThis - budget
                val rupiahFormat = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
                rupiahFormat.maximumFractionDigits = 0
                val excessText = rupiahFormat.format(excess).replace("Rp", "Rp ")

                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Peringatan Anggaran")
                    .setMessage("Mencatat pengeluaran ini akan membuat total pengeluaran bulan ini melebihi rencana anggaran bulanan Anda sebesar $excessText.\n\nApakah Anda tetap ingin menyimpan?")
                    .setPositiveButton("Tetap Simpan") { _, _ ->
                        executeSave(isEdit, editId, title, category, amount, editDate, type)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            } else {
                executeSave(isEdit, editId, title, category, amount, editDate, type)
            }
        }
    }

    private fun executeSave(isEdit: Boolean, editId: Int, title: String, category: String, amount: Int, editDate: String, type: String) {
        val success = if (isEdit) {
            val transaction = Transaction(
                id = editId,
                title = title,
                category = category,
                amount = amount,
                date = editDate,
                location = arguments?.getString("EXTRA_LOCATION") ?: "Lokasi tidak ditentukan",
                type = type
            )
            dbHelper.updateTransaction(transaction)
        } else {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val transaction = Transaction(
                id = 0,
                title = title,
                category = category,
                amount = amount,
                date = date,
                location = "Lokasi tidak ditentukan",
                type = type
            )
            dbHelper.insertTransaction(transaction)
        }

        if (success) {
            val msg = if (isEdit) "Transaksi berhasil diperbarui!" else (if (type == "Pemasukan") "Pemasukan berhasil ditambah!" else "Pengeluaran berhasil dicatat!")
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } else {
            val msg = if (isEdit) "Gagal memperbarui transaksi!" else "Gagal menyimpan transaksi!"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}