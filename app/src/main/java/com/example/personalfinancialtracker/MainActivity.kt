package com.example.personalfinancialtracker

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var rvTransactions: RecyclerView
    private lateinit var tvBudgetInfo: TextView
    private lateinit var btnAddTransaction: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvTransactions = findViewById(R.id.rvTransactions)
        tvBudgetInfo = findViewById(R.id.tvBudgetInfo)
        btnAddTransaction = findViewById(R.id.btnAddTransaction)

        btnAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }


        val btnBackup = findViewById<Button>(R.id.btnBackup)
        val btnRestore = findViewById<Button>(R.id.btnRestore)

        btnBackup.setOnClickListener {
            BackupHelper.backupTransactions(this)
        }

        btnRestore.setOnClickListener {
            BackupHelper.restoreTransactions(this)
            recreate() // Refresh the activity to show restored data
        }
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        fun scheduleDailyReminder() {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 14) // 2 PM
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            // Set the alarm to repeat daily
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
        scheduleDailyReminder()
        val switchReminder = findViewById<SwitchCompat>(R.id.switchReminder)


        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                scheduleDailyReminder()
                Toast.makeText(this, "Reminder On", Toast.LENGTH_SHORT).show()
            } else {
                cancelReminder()
                Toast.makeText(this, "Reminder Off", Toast.LENGTH_SHORT).show()
            }
        }
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)


// Handle Bottom Navigation Clicks
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_records -> {
                    // Stay here
                    true
                }
                R.id.nav_charts -> {
                    startActivity(Intent(this, ChartsActivity::class.java))
                    true
                }
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }



    }
      private  fun cancelReminder() {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
        }





    override fun onResume() {
        super.onResume()

        val transactions = SharedPrefHelper.getTransactions(this)

        rvTransactions.layoutManager = LinearLayoutManager(this)


        val totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
        val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }

        tvBudgetInfo.text = String.format(Locale.getDefault(), "Income: Rs. %.2f | Expense: Rs. %.2f", totalIncome, totalExpense)


        val budgetLimit = SharedPrefHelper.getBudget(this)
        if (budgetLimit > 0 && totalExpense >= budgetLimit * 0.8) {
            NotificationHelper.showBudgetNotification(this, totalExpense, budgetLimit)
        }

        val expenseTransactions = transactions.filter { it.type == "Expense" }

        val categoryTotals = mutableMapOf<String, Double>()
        for (txn in expenseTransactions) {
            categoryTotals[txn.category] = categoryTotals.getOrDefault(txn.category, 0.0) + txn.amount
        }
        rvTransactions.adapter = TransactionAdapter(transactions) { txn, _ ->
            val intent = Intent(this, EditTransactionActivity::class.java)
            intent.putExtra("transaction", txn)
            startActivity(intent)
        }







    }


}
