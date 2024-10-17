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
import androidx.room.Room
import com.google.android.material.bottomnavigation.BottomNavigationView


class ResultActivity : ComponentActivity() {

    private lateinit var bpmResultDao: BPMResultDao;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java,"heartwise-db").allowMainThreadQueries().build();

        bpmResultDao = db.bpmDao();

        enableEdgeToEdge()
        setContent {
            MainView(
                modifier = Modifier.padding(top = 20.dp),
                bpmResult = bpmResultDao.getAll()
            )
        }
    }
}

@Composable
fun ButtonNavigation(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = {context ->
            val bottomNavigationView = BottomNavigationView(context)
            val frameLayout = FrameLayout(context).apply {
                bottomNavigationView.inflateMenu(R.menu.buttom_navigation_menu)
                bottomNavigationView.setBackgroundColor(ContextCompat.getColor(context, R.color.pink_200))
                bottomNavigationView.itemTextColor = ContextCompat.getColorStateList(context, R.color.white)
                bottomNavigationView.itemIconTintList = ContextCompat.getColorStateList(context, R.color.white)

                bottomNavigationView.setSelectedItemId(R.id.nav_result);

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
fun BPMCard(bpmResult: BPMResult) {
    // Define custom FontFamily
    val poppinsSemiBold = FontFamily(Font(R.font.poppins_semibold, FontWeight.SemiBold))
    val poppinsRegular = FontFamily(Font(R.font.poppins_regular))

    // Card composable to represent the Drawable shape
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp), // Equivalent to layout_marginTop in XML
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.shadow_grey)),
        shape = RoundedCornerShape(55.dp) // Adjusted to 25dp to maintain consistency
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // Padding inside the card for better spacing
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title Text
            Text(
                text = "Heart Rate History",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp), // Padding bottom for spacing between title and content
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 16.sp,
                fontFamily = poppinsSemiBold,
                color = colorResource(id = R.color.titleTextColor)
            )

            // Timestamp Text
            Text(
                text = "Date: ${bpmResult.timestamp}",
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 12.sp,
                fontFamily = poppinsSemiBold,
                color = Color.White
            )

            // BPM Result Text
            Text(
                text = "${bpmResult.result} BPM",
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 12.sp,
                fontFamily = poppinsSemiBold,
                color = colorResource(id = R.color.white)
            )
        }
    }
}

@Composable
fun MainView(modifier: Modifier = Modifier, bpmResult: List<BPMResult>) {
    // Define the custom FontFamily
    val poppinsSemiBold = FontFamily(
        Font(R.font.poppins_semibold, FontWeight.SemiBold)
    )

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
                fontFamily = poppinsSemiBold // Apply the custom font here
            )
            Column(
                modifier = Modifier.padding(
                    vertical = 15.dp
                )
            ) {
                bpmResult.forEach { item ->
                    BPMCard(item)
                }
            }
        }
        ButtonNavigation(modifier = Modifier.fillMaxWidth())
    }
}
