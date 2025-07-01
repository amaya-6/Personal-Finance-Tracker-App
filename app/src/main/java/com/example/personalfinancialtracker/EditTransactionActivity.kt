package com.example.personalfinancialtracker

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class EditTransactionActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var radioType: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button

    private lateinit var transaction: Transaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        etTitle = findViewById(R.id.etTitle)
        etAmount = findViewById(R.id.etAmount)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        radioType = findViewById(R.id.radioType)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)

        // Setup spinner
        val categories = arrayOf("Food", "Transport", "Bills", "Shopping", "Others")
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        // Get transaction
        transaction = intent.getSerializableExtra("transaction", Transaction::class.java)!!

        // Fill form
        etTitle.setText(transaction.title)
        etAmount.setText(transaction.amount.toString())
        spinnerCategory.setSelection(getSpinnerIndex(spinnerCategory, transaction.category))
        if (transaction.type == "Income") radioType.check(R.id.radioIncome)
        else radioType.check(R.id.radioExpense)

        // Show delete button
        btnDelete.visibility = View.VISIBLE

        // Save logic
        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = spinnerCategory.selectedItem.toString()
            val type = if (radioType.checkedRadioButtonId == R.id.radioIncome) "Income" else "Expense"

            if (title.isBlank() || amount == null) {
                Toast.makeText(this, "Please enter valid data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updated = Transaction(transaction.id, title, amount, category, transaction.date, type)
            val list = SharedPrefHelper.getTransactions(this).toMutableList()
            val index = list.indexOfFirst { it.id == transaction.id }

            if (index != -1) {
                list[index] = updated
                SharedPrefHelper.saveTransactions(this, list)
                Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show()
            }
            finish()
        }

        // Delete logic
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete") { _, _ ->
                    val list = SharedPrefHelper.getTransactions(this).toMutableList()
                    val index = list.indexOfFirst { it.id == transaction.id }
                    if (index != -1) {
                        list.removeAt(index)
                        SharedPrefHelper.saveTransactions(this, list)
                        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun getSpinnerIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(value, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }
}
