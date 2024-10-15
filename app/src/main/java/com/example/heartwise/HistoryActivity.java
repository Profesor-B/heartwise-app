package com.example.heartwise;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private EditText etActivityDescription, etHeartRate;
    private Button btnAddActivity;
    private RecyclerView rvActivityLog;
    private ActivityAdapter activityAdapter;
    private AppDatabase db;
    private TextView tvDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize views
        etActivityDescription = findViewById(R.id.etActivityDescription);
        etHeartRate = findViewById(R.id.etHeartRate);
        btnAddActivity = findViewById(R.id.btnAddActivity);
        rvActivityLog = findViewById(R.id.rvActivityLog);
        tvDate = findViewById(R.id.tvDate);

        // Set current date
        String currentDate = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
        tvDate.setText("Activity Date: " + currentDate);

        // Initialize database
        db = AppDatabase.getDatabase(this);

        // Set up RecyclerView
        rvActivityLog.setLayoutManager(new LinearLayoutManager(this));
        activityAdapter = new ActivityAdapter();
        rvActivityLog.setAdapter(activityAdapter);

        // Load activities
        loadActivities();

        // Button to add activity
        btnAddActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String activityDescription = etActivityDescription.getText().toString();
                String heartRateStr = etHeartRate.getText().toString();

                if (!TextUtils.isEmpty(activityDescription) && !TextUtils.isEmpty(heartRateStr)) {
                    int heartRate = Integer.parseInt(heartRateStr);
                    ActivityEntity activity = new ActivityEntity(currentDate, activityDescription, heartRate);

                    // Insert activity into the database
                    db.activityDao().insertActivity(activity);

                    // Clear inputs
                    etActivityDescription.setText("");
                    etHeartRate.setText("");

                    // Reload activity list
                    loadActivities();
                }
            }
        });

        //Initialize Navigation buttons
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set Home as the selected default item
        bottomNavigationView.setSelectedItemId(R.id.nav_activity_history);

        // Set the listener for navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                // Navigate to the Home screen
                startActivity(new Intent(getApplicationContext(), HomeActivityMain.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_result) {
                // Navigate to the Result screen
                startActivity(new Intent(getApplicationContext(), ResultActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_measure) {
                // Navigate to the Measure Heart Rate screen
                startActivity(new Intent(getApplicationContext(), CameraMonitor.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_activity_history) {
                // Navigate to the Activity History screen
                startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                // Navigate to the Settings screen
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false; // Return false if none of the conditions matched
        });
    }
    // Method to load and display activities from the database
    private void loadActivities() {
        List<ActivityEntity> activities = db.activityDao().getAllActivities();
        activityAdapter.setActivities(activities);
    }
}