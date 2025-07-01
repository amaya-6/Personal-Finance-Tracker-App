package com.example.personalfinancialtracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var radioType: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button

    private var isEditMode = false
    private var editTransaction: Transaction? = null
    private var editPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        etTitle = findViewById(R.id.etTitle)
        etAmount = findViewById(R.id.etAmount)
        etDate = findViewById(R.id.etDate)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        radioType = findViewById(R.id.radioType)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)

        // Setup category spinner
        val categories = arrayOf("Food", "Transport", "Bills", "Shopping", "Others")
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        // Date picker
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, year, month, day ->
                val selectedDate = "$day/${month + 1}/$year"
                etDate.setText(selectedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        // Check if edit mode
        val txn = intent.getSerializableExtra("transaction", Transaction::class.java)

        editPosition = intent.getIntExtra("position", -1)

        if (txn != null) {
            isEditMode = true
            editTransaction = txn

            // Pre-fill data
            etTitle.setText(txn.title)
            etAmount.setText(txn.amount.toString())
            etDate.setText(txn.date)
            spinnerCategory.setSelection(getSpinnerIndex(spinnerCategory, txn.category))
            if (txn.type == "Income") {
                radioType.check(R.id.radioIncome)
            } else {
                radioType.check(R.id.radioExpense)
            }

            btnDelete.visibility = View.VISIBLE
        } else {
            btnDelete.visibility = View.GONE
        }

        // Save logic
        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = spinnerCategory.selectedItem.toString()
            val type = if (radioType.checkedRadioButtonId == R.id.radioIncome) "Income" else "Expense"
            val date = etDate.text.toString()

            if (title.isBlank() || amount == null || date.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = Transaction(
                id = editTransaction?.id ?: Random().nextInt(10000),
                title = title,
                amount = amount,
                category = category,
                date = date,
                type = type
            )

            val list = SharedPrefHelper.getTransactions(this).toMutableList()
            if (isEditMode) {
                list[editPosition] = transaction
                Toast.makeText(this, "Transaction Updated", Toast.LENGTH_SHORT).show()
            } else {
                list.add(transaction)
                Toast.makeText(this, "Transaction Added", Toast.LENGTH_SHORT).show()
            }
            SharedPrefHelper.saveTransactions(this, list)
            finish()
        }

        // Delete logic with confirmation
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete") { _, _ ->
                    val list = SharedPrefHelper.getTransactions(this).toMutableList()
                    list.removeAt(editPosition)
                    SharedPrefHelper.saveTransactions(this, list)
                    Toast.makeText(this, "Transaction Deleted", Toast.LENGTH_SHORT).show()
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
