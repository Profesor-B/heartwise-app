package com.example.heartwise;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private EditText etActivityDescription, etHeartRate;
    private Button btnAddActivity;
    private RecyclerView rvActivityLog;
    private ActivityAdapter activityAdapter;
    private ActivityDao activityDao;
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
        String currentDate = new SimpleDateFormat("MMMM dd, yyyy").format(new Date());
        tvDate.setText(currentDate);

        // Initialize database
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "history-db")
                .allowMainThreadQueries()
                .build();
        activityDao = db.activityDao();
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
                    // Create new activity entity with data
                    ActivityEntity activity = new ActivityEntity(0, currentDate, activityDescription, heartRateStr);
                    // Insert activity into the database
                    activityDao.insertActivity(activity);
                    // Clear inputs
                    etActivityDescription.setText("");
                    etHeartRate.setText("");

                    // Display toast message
                    Toast.makeText(HistoryActivity.this, "New activity added", Toast.LENGTH_SHORT).show();

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
        List<ActivityEntity> activities = activityDao.getAllActivities();
        activityAdapter.setActivities(activities);
    }
}