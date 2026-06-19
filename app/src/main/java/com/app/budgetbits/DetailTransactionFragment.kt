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

    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailTransactionBinding.bind(view)

        val title = arguments?.getString("EXTRA_TITLE")
        val category = arguments?.getString("EXTRA_CATEGORY")
        val date = arguments?.getString("EXTRA_DATE")
        val amount = arguments?.getInt("EXTRA_AMOUNT", 0) ?: 0
        val location = arguments?.getString("EXTRA_LOCATION") ?: "Lokasi tidak diketahui"
        val type = arguments?.getString("EXTRA_TYPE") ?: "Pengeluaran"

        binding.tvDetailTitle.text = title
        binding.tvDetailCategory.text = category
        binding.tvDetailDate.text = date
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}