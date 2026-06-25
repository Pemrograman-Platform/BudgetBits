package com.app.budgetbits.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.budgetbits.databinding.ItemTransactionBinding
import com.app.budgetbits.model.Transaction
import java.text.NumberFormat
import java.util.Locale

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit // Fungsi klik
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    inner class TransactionViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.tvTitle.text = transaction.title
            
            val displayDate = try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(transaction.date)
                if (date != null) outputFormat.format(date) else transaction.date
            } catch (e: Exception) {
                transaction.date
            }
            binding.tvCategoryDate.text = "${transaction.category} • $displayDate"
            
            val amountText = rupiahFormat.format(transaction.amount).replace("Rp", "Rp ")
            if (transaction.type == "Pemasukan") {
                binding.tvAmount.text = "+ $amountText"
                binding.tvAmount.setTextColor(android.graphics.Color.parseColor("#2E7D32")) // Green
            } else {
                binding.tvAmount.text = "- $amountText"
                binding.tvAmount.setTextColor(android.graphics.Color.parseColor("#D32F2F")) // Red
            }

            // Saat satu kotak (root) diklik
            binding.root.setOnClickListener {
                onItemClick(transaction)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size
}