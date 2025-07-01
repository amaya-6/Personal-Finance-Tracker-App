package com.example.personalfinancialtracker

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefHelper {
    private const val PREF_NAME = "finance_prefs"
    private const val KEY_TRANSACTIONS = "transactions"



    fun getTransactions(context: Context): List<Transaction> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TRANSACTIONS, null)
        return if (json != null) {
            val type = object : TypeToken<List<Transaction>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }
    private const val KEY_BUDGET = "monthly_budget"

    fun saveBudget(context: Context, budget: Double) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putFloat(KEY_BUDGET, budget.toFloat()).apply()
    }

    fun getBudget(context: Context): Double {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_BUDGET, 0f).toDouble()
    }
    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val json = Gson().toJson(transactions)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TRANSACTIONS, json)
            .apply()
    }


}
