package com.app.budgetbits

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class PowerConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_POWER_CONNECTED) {
            Toast.makeText(context, "Charger Terhubung! Yuk catat keuangan Anda sekarang ⚡", Toast.LENGTH_LONG).show()
        } else if (action == Intent.ACTION_POWER_DISCONNECTED) {
            Toast.makeText(context, "Charger Dicabut! Pastikan baterai cukup untuk mengelola anggaran 🔋", Toast.LENGTH_SHORT).show()
        }
    }
}
