package com.example.heartwise

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultActivity : ComponentActivity() {

    private lateinit var bpmResultDao: BPMResultDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "bpm-result-db")
            .allowMainThreadQueries() // You can use coroutines to remove this for production apps
            .build()

        bpmResultDao = db.bpmDao()

        enableEdgeToEdge()
        setContent {
            MainView(
                modifier = Modifier.padding(top = 20.dp),
                bpmResult = bpmResultDao.getAll()
            )
        }
    }

    private fun saveBPMResult(bpm: Int) {
        // Calculate systolic and diastolic based on the BPM
        val (systolic, diastolic) = calculateBloodPressure(bpm)

        // Create a new BPMResult object with current values
        val bpmResult = BPMResult(
            uid = 0, // Room will auto-generate this
            result = bpm.toString(),
            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            systolic = systolic,
            diastolic = diastolic
        )

        // Insert the data into the database using a coroutine
        lifecycleScope.launch {
            bpmResultDao.insertData(bpmResult)
        }
    }

    // Function to calculate systolic and diastolic based on BPM
    private fun calculateBloodPressure(bpm: Int): Pair<Int, Int> {
        val systolic = when {
            bpm < 100 -> 120
            bpm in 100..120 -> 130
            else -> 140
        }

        val diastolic = when {
            bpm < 100 -> 80
            bpm in 100..120 -> 85
            else -> 90
        }

        return Pair(systolic, diastolic)
    }
}

@Composable
fun ButtonNavigation(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val bottomNavigationView = BottomNavigationView(context)
            val frameLayout = FrameLayout(context).apply {
                bottomNavigationView.inflateMenu(R.menu.buttom_navigation_menu)
                bottomNavigationView.setBackgroundColor(ContextCompat.getColor(context, R.color.pink_200))
                bottomNavigationView.itemTextColor = ContextCompat.getColorStateList(context, R.color.white)
                bottomNavigationView.itemIconTintList = ContextCompat.getColorStateList(context, R.color.white)

                bottomNavigationView.setSelectedItemId(R.id.nav_result)

                bottomNavigationView.setOnNavigationItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.nav_home -> {
                            startActivity(context, Intent(context, HomeActivityMain::class.java), null)
                            if (context is Activity) {
                                context.overridePendingTransition(0, 0)
                            }
                            true
                        }
                        R.id.nav_result -> {
                            startActivity(context, Intent(context, ResultActivity::class.java), null)
                            if (context is Activity) {
                                context.overridePendingTransition(0, 0)
                            }
                            true
                        }
                        R.id.nav_measure -> {
                            startActivity(context, Intent(context, CameraMonitor::class.java), null)
                            if (context is Activity) {
                                context.overridePendingTransition(0, 0)
                            }
                            true
                        }
                        R.id.nav_activity_history -> {
                            startActivity(context, Intent(context, HistoryActivity::class.java), null)
                            if (context is Activity) {
                                context.overridePendingTransition(0, 0)
                            }
                            true
                        }
                        R.id.nav_settings -> {
                            startActivity(context, Intent(context, SettingsActivity::class.java), null)
                            if (context is Activity) {
                                context.overridePendingTransition(0, 0)
                            }
                            true
                        }
                        else -> false
                    }
                }

                addView(bottomNavigationView)
            }

            frameLayout
        }
    )
}

@Composable
fun getBloodPressureCategory(bpmResult: String): String {

    // convert to double
    val result = bpmResult.toDouble();

    return when {
        result < 120 && result > 70  -> "Normal"
        result > 120 && result < 130 -> "Elevated"
        result > 130 && result < 140 -> "High Blood Pressure"
        result > 140 && result < 180 -> "Hypertension"
        else -> "Unknown"
    }
}

@Composable
fun BPMCard(bpmResult: BPMResult) {
    val poppinsSemiBold = FontFamily(Font(R.font.poppins_semibold, FontWeight.SemiBold))
    val poppinsRegular = FontFamily(Font(R.font.poppins_regular))

    val bloodPressureCategory = getBloodPressureCategory(bpmResult.result)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        elevation = CardDefaults.cardElevation(45.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.shadow_grey)),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Heart Rate History",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontFamily = poppinsSemiBold,
                color = colorResource(id = R.color.titleTextColor)
            )

            Text(
                text = "Date: ${bpmResult.timestamp}",
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontFamily = poppinsSemiBold,
                color = Color.White
            )

            Text(
                text = "${bpmResult.result} BPM",
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontFamily = poppinsSemiBold,
                color = colorResource(id = R.color.white)
            )
            Text(
                text = "Category: $bloodPressureCategory",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontFamily = poppinsSemiBold,
                color = colorResource(id = R.color.white)
            )
        }
    }
}

@Composable
fun MainView(modifier: Modifier = Modifier, bpmResult: List<BPMResult>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.pink_200))
            .padding(all = 15.dp)
            .padding(top = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(R.drawable.heartwisetext),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(colorResource(id = R.color.pink_200)),
            )
            Spacer(modifier = Modifier.height(3.dp))
            Image(
                painter = painterResource(id = R.drawable.heartwiselogo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Blood Pressure History",
                modifier = Modifier
                    .fillMaxWidth(),
                color = colorResource(id = R.color.titleTextColor),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily(Font(R.font.poppins_semibold, FontWeight.SemiBold))
            )

            // Wrapping the BPMCard list in LazyColumn for scrollability
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp)
            ) {
                items(bpmResult) { item ->
                    BPMCard(item)
                }
            }
        }
        ButtonNavigation(modifier = Modifier.fillMaxWidth())
    }
}