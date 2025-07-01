package com.example.personalfinancialtracker

import android.content.Context
import android.widget.Toast
import com.google.gson.Gson
import java.io.*

object BackupHelper {
    private const val FILE_NAME = "transactions_backup.json"

    fun backupTransactions(context: Context) {
        val transactions = SharedPrefHelper.getTransactions(context)
        val json = Gson().toJson(transactions)

        try {
            val file = File(context.filesDir, FILE_NAME)
            val writer = FileWriter(file)
            writer.write(json)
            writer.close()
            Toast.makeText(context, "Backup successful!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Backup failed!", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun restoreTransactions(context: Context) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) {
                Toast.makeText(context, "No backup file found", Toast.LENGTH_SHORT).show()
                return
            }

            val json = file.readText()
            val type = object : com.google.gson.reflect.TypeToken<List<Transaction>>() {}.type
            val transactions: List<Transaction> = Gson().fromJson(json, type)

            // Replace existing data
            val jsonToStore = Gson().toJson(transactions)
            context.getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("transactions", jsonToStore)
                .apply()

            Toast.makeText(context, "Restore successful!", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            Toast.makeText(context, "Restore failed!", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}
