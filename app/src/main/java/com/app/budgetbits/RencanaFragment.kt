package com.app.budgetbits

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.budgetbits.data.DatabaseHelper
import com.app.budgetbits.databinding.FragmentRencanaBinding

class RencanaFragment : Fragment(R.layout.fragment_rencana) {

    private var _binding: FragmentRencanaBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRencanaBinding.bind(view)
        dbHelper = DatabaseHelper(requireContext())

        // Load current values
        val currentSaldo = dbHelper.getIntConfig(DatabaseHelper.KEY_SALDO)
        val currentBudget = dbHelper.getIntConfig(DatabaseHelper.KEY_BUDGET)

        binding.etSaldo.setText(currentSaldo.toString())
        binding.etBudget.setText(currentBudget.toString())

        binding.btnSaveRencana.setOnClickListener {
            val newSaldo = binding.etSaldo.text.toString().toIntOrNull() ?: 0
            val newBudget = binding.etBudget.text.toString().toIntOrNull() ?: 0

            dbHelper.updateConfig(DatabaseHelper.KEY_SALDO, newSaldo)
            dbHelper.updateConfig(DatabaseHelper.KEY_BUDGET, newBudget)

            Toast.makeText(requireContext(), "Rencana berhasil disimpan!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}