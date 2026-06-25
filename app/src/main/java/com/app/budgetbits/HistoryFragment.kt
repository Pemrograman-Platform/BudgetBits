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
    private var allTransactions: List<com.app.budgetbits.model.Transaction> = emptyList()
    private var currentSearchQuery = ""
    private var currentCategoryFilter = "Semua"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)
        dbHelper = DatabaseHelper(requireContext())

        allTransactions = dbHelper.getAllTransactions()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())

        // Set up Listeners
        setupSearchListener()
        setupChipFilterListener()

        // Initial populate
        filterAndSearch()
    }

    private fun setupSearchListener() {
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString() ?: ""
                filterAndSearch()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupChipFilterListener() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<com.google.android.material.chip.Chip>(checkedIds[0])
                currentCategoryFilter = selectedChip?.text.toString()
            } else {
                currentCategoryFilter = "Semua"
            }
            filterAndSearch()
        }
    }

    private fun filterAndSearch() {
        val filteredList = allTransactions.filter { t ->
            val matchesSearch = t.title.contains(currentSearchQuery, ignoreCase = true) || 
                                t.category.contains(currentSearchQuery, ignoreCase = true)

            val matchesCategory = if (currentCategoryFilter == "Semua") {
                true
            } else {
                val cat = t.category.lowercase()
                val filter = currentCategoryFilter.lowercase()
                if (filter.startsWith("transport") && cat.startsWith("transport")) {
                    true
                } else {
                    cat == filter
                }
            }

            matchesSearch && matchesCategory
        }

        setupAdapter(filteredList)
    }

    private fun setupAdapter(list: List<com.app.budgetbits.model.Transaction>) {
        if (list.isEmpty()) {
            binding.rvHistory.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvHistory.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
        }

        val adapter = TransactionAdapter(list) { clickedTransaction ->
            val bundle = Bundle().apply {
                putInt("EXTRA_ID", clickedTransaction.id)
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

        binding.rvHistory.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}