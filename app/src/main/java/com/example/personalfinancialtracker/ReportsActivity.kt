package com.example.personalfinancialtracker

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class ReportsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)
        val tvCurrentBudget = findViewById<TextView>(R.id.tvCurrentBudget)
        val tvBudgetUsage = findViewById<TextView>(R.id.tvBudgetUsage)
        val tvRemainingBudget = findViewById<TextView>(R.id.tvRemainingBudget)
        val progressBudget = findViewById<ProgressBar>(R.id.progressBudget)

        fun updateBudgetUI() {
            val budget = SharedPrefHelper.getBudget(this)
            val transactions = SharedPrefHelper.getTransactions(this)
            val totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

            val remaining = (budget - totalExpense).coerceAtLeast(0.0)
            val usedPercent = if (budget > 0) (totalExpense / budget * 100).toInt() else 0

            tvCurrentBudget.text = "Current Budget: Rs. %.2f".format(budget)
            tvBudgetUsage.text = "Used: Rs. %.2f / Rs. %.2f".format(totalExpense, budget)
            tvRemainingBudget.text = "Remaining: Rs. %.2f".format(remaining)

            // Animate progress bar
            ObjectAnimator.ofInt(progressBudget, "progress", 0, usedPercent).apply {
                duration = 1000
                interpolator = DecelerateInterpolator()
                start()
            }

            // Show overspent warning if over budget
            if (totalExpense > budget) {
                Toast.makeText(this, "⚠️ Overspent! You exceeded your monthly budget.", Toast.LENGTH_LONG).show()
                tvRemainingBudget.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            } else {
                tvRemainingBudget.setTextColor(resources.getColor(android.R.color.black))
            }
        }





        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val etBudget = findViewById<EditText>(R.id.etBudget)
        val btnSetBudget = findViewById<Button>(R.id.btnSetBudget)


        bottomNav.selectedItemId = R.id.nav_reports
        btnSetBudget.setOnClickListener {
            val budget = etBudget.text.toString().toDoubleOrNull()
            if (budget != null) {
                SharedPrefHelper.saveBudget(this, budget)
                Toast.makeText(this, "Budget Saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            }
        }
        updateBudgetUI()
        val btnExport = findViewById<Button>(R.id.btnExport)

        btnExport.setOnClickListener {
            BackupHelper.backupTransactions(this)
        }


        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_records -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_charts -> {
                    startActivity(Intent(this, ChartsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_reports -> true // Stay here
                else -> false
            }
        }


    }
}
