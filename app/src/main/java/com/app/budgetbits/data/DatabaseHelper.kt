package com.app.budgetbits.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.app.budgetbits.model.Transaction

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "budgetbits.db"
        private const val DATABASE_VERSION = 2

        // Table Transactions
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_CATEGORY = "category"
        private const val COL_AMOUNT = "amount"
        private const val COL_DATE = "date"
        private const val COL_LOCATION = "location"
        private const val COL_TYPE = "type"

        // Table Config (for Balance and Budget)
        private const val TABLE_CONFIG = "config"
        private const val COL_KEY = "key_name"
        private const val COL_VALUE = "key_value"

        const val KEY_SALDO = "saldo"
        const val KEY_BUDGET = "budget"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTransactionTable = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_CATEGORY TEXT NOT NULL,
                $COL_AMOUNT INTEGER NOT NULL,
                $COL_DATE TEXT NOT NULL,
                $COL_LOCATION TEXT NOT NULL,
                $COL_TYPE TEXT NOT NULL DEFAULT 'Pengeluaran'
            )
        """.trimIndent()

        val createConfigTable = """
            CREATE TABLE $TABLE_CONFIG (
                $COL_KEY TEXT PRIMARY KEY,
                $COL_VALUE INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTransactionTable)
        db.execSQL(createConfigTable)

        // Initialize default values
        db.execSQL("INSERT INTO $TABLE_CONFIG ($COL_KEY, $COL_VALUE) VALUES ('$KEY_SALDO', 0)")
        db.execSQL("INSERT INTO $TABLE_CONFIG ($COL_KEY, $COL_VALUE) VALUES ('$KEY_BUDGET', 0)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONFIG")
        onCreate(db)
    }

    // --- TRANSACTION METHODS ---

    fun insertTransaction(transaction: Transaction): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put(COL_TITLE, transaction.title)
                put(COL_CATEGORY, transaction.category)
                put(COL_AMOUNT, transaction.amount)
                put(COL_DATE, transaction.date)
                put(COL_LOCATION, transaction.location)
                put(COL_TYPE, transaction.type)
            }
            val result = db.insert(TABLE_TRANSACTIONS, null, values)
            
            if (result != -1L) {
                // Update balance automatically
                val currentSaldo = getIntConfig(KEY_SALDO)
                if (transaction.type == "Pemasukan") {
                    updateConfig(KEY_SALDO, currentSaldo + transaction.amount)
                } else {
                    updateConfig(KEY_SALDO, currentSaldo - transaction.amount)
                }
                db.setTransactionSuccessful()
            }
            return result != -1L
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getAllTransactions(): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRANSACTIONS ORDER BY $COL_ID DESC", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Transaction(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // --- CONFIG METHODS (SALDO & BUDGET) ---

    fun updateConfig(key: String, value: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_VALUE, value)
        }
        db.update(TABLE_CONFIG, values, "$COL_KEY = ?", arrayOf(key))
    }

    fun getIntConfig(key: String): Int {
        val db = readableDatabase
        val cursor = db.query(TABLE_CONFIG, arrayOf(COL_VALUE), "$COL_KEY = ?", arrayOf(key), null, null, null)
        var value = 0
        if (cursor.moveToFirst()) {
            value = cursor.getInt(cursor.getColumnIndexOrThrow(COL_VALUE))
        }
        cursor.close()
        return value
    }

    fun getTotalExpenseThisMonth(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COL_AMOUNT) FROM $TABLE_TRANSACTIONS WHERE $COL_TYPE = 'Pengeluaran'", null)
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }
}