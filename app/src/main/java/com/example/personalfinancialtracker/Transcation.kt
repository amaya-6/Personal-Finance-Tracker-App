package com.example.personalfinancialtracker

import java.io.Serializable

data class Transaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val type: String // "Income" or "Expense"
): Serializable
