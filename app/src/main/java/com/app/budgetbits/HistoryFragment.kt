package com.app.budgetbits

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.budgetbits.adapter.TransactionAdapter
import com.app.budgetbits.data.DatabaseHelper
import com.app.budgetbits.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)
        dbHelper = DatabaseHelper(requireContext())

        val transactions = dbHelper.getAllTransactions()

        // Logika Pengecekan Empty State
        if (transactions.isEmpty()) {
            binding.rvHistory.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvHistory.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
        }

        val adapter = TransactionAdapter(transactions) { clickedTransaction ->
            val bundle = Bundle().apply {
                putString("EXTRA_TITLE", clickedTransaction.title)
                putString("EXTRA_CATEGORY", clickedTransaction.category)
                putString("EXTRA_DATE", clickedTransaction.date)
                putInt("EXTRA_AMOUNT", clickedTransaction.amount)
                putString("EXTRA_LOCATION", clickedTransaction.location)
                putString("EXTRA_TYPE", clickedTransaction.type)
            }

            val detailFragment = DetailTransactionFragment()
            detailFragment.arguments = bundle
            (activity as? MainActivity)?.replaceFragment(detailFragment, true)
        }

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}