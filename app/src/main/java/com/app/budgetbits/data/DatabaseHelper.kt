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
        private const val DATABASE_VERSION = 3

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
                // Update balance automatically directly on the active transaction db
                var currentSaldo = 0
                val cursor = db.query(TABLE_CONFIG, arrayOf(COL_VALUE), "$COL_KEY = ?", arrayOf(KEY_SALDO), null, null, null)
                if (cursor.moveToFirst()) {
                    currentSaldo = cursor.getInt(cursor.getColumnIndexOrThrow(COL_VALUE))
                }
                cursor.close()

                val nextSaldo = if (transaction.type == "Pemasukan") {
                    currentSaldo + transaction.amount
                } else {
                    currentSaldo - transaction.amount
                }

                val configValues = ContentValues().apply {
                    put(COL_VALUE, nextSaldo)
                }
                db.update(TABLE_CONFIG, configValues, "$COL_KEY = ?", arrayOf(KEY_SALDO))
                
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
        val cursor = db.rawQuery("SELECT SUM($COL_AMOUNT) FROM $TABLE_TRANSACTIONS WHERE $COL_TYPE = 'Pengeluaran' AND strftime('%Y-%m', $COL_DATE) = strftime('%Y-%m', 'now')", null)
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    fun getRecentTransactions(limit: Int): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRANSACTIONS ORDER BY $COL_ID DESC LIMIT ?", arrayOf(limit.toString()))
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

    fun deleteTransaction(id: Int): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            var transaction: Transaction? = null
            val cursor = db.query(TABLE_TRANSACTIONS, null, "$COL_ID = ?", arrayOf(id.toString()), null, null, null)
            if (cursor.moveToFirst()) {
                transaction = Transaction(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE))
                )
            }
            cursor.close()

            if (transaction != null) {
                var currentSaldo = 0
                val saldoCursor = db.query(TABLE_CONFIG, arrayOf(COL_VALUE), "$COL_KEY = ?", arrayOf(KEY_SALDO), null, null, null)
                if (saldoCursor.moveToFirst()) {
                    currentSaldo = saldoCursor.getInt(saldoCursor.getColumnIndexOrThrow(COL_VALUE))
                }
                saldoCursor.close()

                val nextSaldo = if (transaction.type == "Pemasukan") {
                    currentSaldo - transaction.amount
                } else {
                    currentSaldo + transaction.amount
                }

                val configValues = ContentValues().apply {
                    put(COL_VALUE, nextSaldo)
                }
                db.update(TABLE_CONFIG, configValues, "$COL_KEY = ?", arrayOf(KEY_SALDO))

                val deleteResult = db.delete(TABLE_TRANSACTIONS, "$COL_ID = ?", arrayOf(id.toString()))
                if (deleteResult > 0) {
                    db.setTransactionSuccessful()
                    return true
                }
            }
            return false
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun updateTransaction(newTransaction: Transaction): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            var oldTransaction: Transaction? = null
            val cursor = db.query(TABLE_TRANSACTIONS, null, "$COL_ID = ?", arrayOf(newTransaction.id.toString()), null, null, null)
            if (cursor.moveToFirst()) {
                oldTransaction = Transaction(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE))
                )
            }
            cursor.close()

            if (oldTransaction != null) {
                var currentSaldo = 0
                val saldoCursor = db.query(TABLE_CONFIG, arrayOf(COL_VALUE), "$COL_KEY = ?", arrayOf(KEY_SALDO), null, null, null)
                if (saldoCursor.moveToFirst()) {
                    currentSaldo = saldoCursor.getInt(saldoCursor.getColumnIndexOrThrow(COL_VALUE))
                }
                saldoCursor.close()

                var adjustedSaldo = if (oldTransaction.type == "Pemasukan") {
                    currentSaldo - oldTransaction.amount
                } else {
                    currentSaldo + oldTransaction.amount
                }

                adjustedSaldo = if (newTransaction.type == "Pemasukan") {
                    adjustedSaldo + newTransaction.amount
                } else {
                    adjustedSaldo - newTransaction.amount
                }

                val configValues = ContentValues().apply {
                    put(COL_VALUE, adjustedSaldo)
                }
                db.update(TABLE_CONFIG, configValues, "$COL_KEY = ?", arrayOf(KEY_SALDO))

                val values = ContentValues().apply {
                    put(COL_TITLE, newTransaction.title)
                    put(COL_CATEGORY, newTransaction.category)
                    put(COL_AMOUNT, newTransaction.amount)
                    put(COL_DATE, newTransaction.date)
                    put(COL_LOCATION, newTransaction.location)
                    put(COL_TYPE, newTransaction.type)
                }
                val updateResult = db.update(TABLE_TRANSACTIONS, values, "$COL_ID = ?", arrayOf(newTransaction.id.toString()))
                if (updateResult > 0) {
                    db.setTransactionSuccessful()
                    return true
                }
            }
            return false
        } finally {
            db.endTransaction()
            db.close()
        }
    }
}