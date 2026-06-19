package com.app.budgetbits.model

data class Transaction(
    val id: Int,
    val title: String,
    val category: String, 
    val amount: Int,
    val date: String,
    val location: String = "Lokasi tidak ditentukan",
    val type: String = "Pengeluaran" // "Pemasukan" atau "Pengeluaran"
)