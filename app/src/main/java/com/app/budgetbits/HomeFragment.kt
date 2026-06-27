package com.app.budgetbits

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.budgetbits.adapter.TransactionAdapter
import com.app.budgetbits.data.DatabaseHelper
import com.app.budgetbits.databinding.FragmentHomeBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.NumberFormat
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        dbHelper = DatabaseHelper(requireContext())

        // Ambil nama dari SharedPreferences
        val sharedPrefs = requireContext().getSharedPreferences("budgetbits_prefs", android.content.Context.MODE_PRIVATE)
        val name = sharedPrefs.getString("USER_NAME", "Siti Aminah")
        binding.tvWelcomeName.text = "Halo, $name!"

        // Aksi pindah ke AddTransactionFragment melalui tombol melayang (FAB)
        binding.fabTambah.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(AddTransactionFragment(), true)
        }

        binding.btnActionCatat.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(AddTransactionFragment(), true)
        }

        binding.btnActionRencana.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(RencanaFragment(), true)
        }

        binding.btnActionEkspor.setOnClickListener {
            exportTransactionsToCSV()
        }

        binding.tvSeeAll.setOnClickListener {
            (activity as? MainActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_history
        }

        // Memanggil fungsi untuk menampilkan data dan grafik
        updateSummary()
        setupPieChart()
        setupRecentTransactions()
        loadFinancialTipFromJSON()
    }

    private fun updateSummary() {
        val saldo = dbHelper.getIntConfig(DatabaseHelper.KEY_SALDO)
        val budget = dbHelper.getIntConfig(DatabaseHelper.KEY_BUDGET)
        val expense = dbHelper.getTotalExpenseThisMonth()

        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        formatter.maximumFractionDigits = 0

        binding.tvBalance.text = formatter.format(saldo).replace("Rp", "Rp ")
        binding.tvTotalExpense.text = formatter.format(expense).replace("Rp", "Rp ")

        // Update Budget Progress
        if (budget > 0) {
            val percentage = (expense.toFloat() / budget.toFloat() * 100).toInt()
            binding.tvBudgetPercentage.text = "$percentage%"
            binding.pbBudget.progress = if (percentage > 100) 100 else percentage
            binding.tvBudgetUsed.text = formatter.format(expense).replace("Rp", "Rp ")
            binding.tvBudgetLimit.text = formatter.format(budget).replace("Rp", "Rp ")
        } else {
            binding.tvBudgetPercentage.text = "0%"
            binding.pbBudget.progress = 0
            binding.tvBudgetUsed.text = "Rp 0"
            binding.tvBudgetLimit.text = "Rp 0"
        }
    }

    private fun setupRecentTransactions() {
        // Ambil 3 transaksi terbaru dari Database
        val transactions = dbHelper.getRecentTransactions(3)
        val adapter = TransactionAdapter(transactions) { clickedTransaction ->
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

        binding.rvRecent.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecent.adapter = adapter
    }

    // Fungsi khusus untuk mengatur Grafik Lingkaran (Pie Chart)
    private fun setupPieChart() {
        // Hanya ambil transaksi bertipe Pengeluaran untuk grafik
        val transactions = dbHelper.getAllTransactions().filter { it.type == "Pengeluaran" }
        
        if (transactions.isEmpty()) {
            binding.pieChart.clear()
            binding.pieChart.centerText = "Belum ada pengeluaran"
            binding.pieChart.invalidate()
            return
        }

        val categoryMap = transactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }

        // 1. Siapkan data persentase
        val entries = ArrayList<PieEntry>()
        categoryMap.forEach { (category, total) ->
            entries.add(PieEntry(total, category))
        }

        // 2. Masukkan data ke Dataset
        val dataSet = PieDataSet(entries, "")

        // 3. Atur warna-warni untuk setiap potongan grafik
        val colors = ArrayList<Int>()
        colors.add(Color.parseColor("#4CAF50")) 
        colors.add(Color.parseColor("#FFC107")) 
        colors.add(Color.parseColor("#03A9F4"))
        colors.add(Color.parseColor("#FF5252")) 
        colors.add(Color.parseColor("#9C27B0"))
        dataSet.colors = colors

        // Atur gaya teks di dalam grafik
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f
        dataSet.sliceSpace = 3f

        // 4. Terapkan data ke dalam komponen PieChart di XML
        val data = PieData(dataSet)
        binding.pieChart.data = data

        // 5. Animasi dan Tampilan Tambahan
        binding.pieChart.description.isEnabled = false
        binding.pieChart.centerText = "Pengeluaran"
        binding.pieChart.setCenterTextSize(14f)
        binding.pieChart.animateY(1000)
        binding.pieChart.invalidate()
    }

    private fun exportTransactionsToCSV() {
        val transactions = dbHelper.getAllTransactions()
        if (transactions.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "Belum ada transaksi untuk diekspor!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val csvHeader = "ID,Judul,Kategori,Jumlah,Tanggal,Lokasi,Tipe\n"
        val csvBody = StringBuilder()
        transactions.forEach { t ->
            val cleanTitle = t.title.replace(",", " ")
            val cleanLocation = t.location.replace(",", " ")
            csvBody.append("${t.id},$cleanTitle,${t.category},${t.amount},${t.date},$cleanLocation,${t.type}\n")
        }
        
        val csvData = csvHeader + csvBody.toString()

        try {
            val file = java.io.File(requireContext().cacheDir, "BudgetBits_Transaksi.csv")
            file.writeText(csvData)

            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "Ekspor Transaksi via"))

        } catch (e: Exception) {
            android.widget.Toast.makeText(requireContext(), "Gagal mengekspor file: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFinancialTipFromJSON() {
        try {
            // Membaca file tips.json dari folder assets
            val jsonString = requireContext().assets.open("tips.json")
                .bufferedReader()
                .use { it.readText() }

            // Parsing JSON manual menggunakan JSONObject/JSONArray bawaan Android
            val jsonArray = org.json.JSONArray(jsonString)
            if (jsonArray.length() > 0) {
                // Pilih satu tips secara acak
                val randomIndex = (0 until jsonArray.length()).random()
                val tipObject = jsonArray.getJSONObject(randomIndex)
                val title = tipObject.getString("title")
                val description = tipObject.getString("description")

                // Bind ke komponen UI di XML
                binding.tvTipTitle.text = title
                binding.tvTipDescription.text = description
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvTipTitle.text = "Tips Keuangan"
            binding.tvTipDescription.text = "Kelola anggaran bulanan Anda agar keuangan tetap sehat dan aman!"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}