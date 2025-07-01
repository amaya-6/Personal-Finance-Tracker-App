package com.example.personalfinancialtracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView

class ChartsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charts)

        supportActionBar?.title = "Charts"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_charts

        // Bottom nav click handling
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_records -> {
                    startActivity(Intent(this, MainActivity::class.java))
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
                R.id.nav_charts -> true // stay here
                else -> false
            }
        }
        loadPieChart()
        loadBarChart()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadPieChart() {
        val transactions = SharedPrefHelper.getTransactions(this)
            .filter { it.type == "Expense" }

        val categoryTotal = mutableMapOf<String, Double>()
        for (txn in transactions) {
            categoryTotal[txn.category] = categoryTotal.getOrDefault(txn.category, 0.0) + txn.amount
        }

        val entries = categoryTotal.map { PieEntry(it.value.toFloat(), it.key) }

        val dataSet = PieDataSet(entries, "Expenses")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Categories"
        pieChart.setUsePercentValues(true)
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun loadBarChart() {
        val transactions = SharedPrefHelper.getTransactions(this)
            .filter { it.type == "Expense" }

        val monthTotal = mutableMapOf<String, Double>()
        for (txn in transactions) {
            val month = txn.date.takeLast(4) // last 4 characters = year OR use month extraction logic
            monthTotal[month] = monthTotal.getOrDefault(month, 0.0) + txn.amount
        }

        val entries = monthTotal.entries.mapIndexed { index, it ->
            BarEntry(index.toFloat(), it.value.toFloat())
        }

        val labels = monthTotal.keys.toList()
        val dataSet = BarDataSet(entries, "Monthly Expenses")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        barChart.data = barData
        barChart.setFitBars(true)
        barChart.description.isEnabled = false
        barChart.animateY(1000)

        // Set labels on x-axis
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.labelRotationAngle = -45f

        barChart.invalidate()
    }
}
