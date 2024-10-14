package com.example.heartwise;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    private Switch alertNotificationSwitch;
    private RelativeLayout emergencyContactsLayout;
    private RelativeLayout privacyLayout;
    private RelativeLayout deleteHistoryLayout;
    private RelativeLayout helpSectionLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set Home as the selected default item
        bottomNavigationView.setSelectedItemId(R.id.nav_settings);

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

        // Initialize UI elements
        alertNotificationSwitch = findViewById(R.id.alert_notification_switch);
        emergencyContactsLayout = findViewById(R.id.emergency_contacts_layout);
        privacyLayout = findViewById(R.id.privacy_layout);
        deleteHistoryLayout = findViewById(R.id.delete_history_layout);
        helpSectionLayout = findViewById(R.id.help_section_layout);

        // Set listeners for UI elements
            alertNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(SettingsActivity.this, "Alert Notification ON", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingsActivity.this, "Alert Notification OFF", Toast.LENGTH_SHORT).show();
            }
        });

            emergencyContactsLayout.setOnClickListener(v -> {
            // Handle emergency contacts click
            Toast.makeText(SettingsActivity.this, "Emergency Contacts Clicked", Toast.LENGTH_SHORT).show();
        });

            privacyLayout.setOnClickListener(v -> {
            // Handle privacy click
            Toast.makeText(SettingsActivity.this, "Privacy Clicked", Toast.LENGTH_SHORT).show();
        });

            deleteHistoryLayout.setOnClickListener(v -> {
            // Handle delete history click
            Toast.makeText(SettingsActivity.this, "Delete History Clicked", Toast.LENGTH_SHORT).show();
        });

            helpSectionLayout.setOnClickListener(v -> {
            // Handle help section click
            Toast.makeText(SettingsActivity.this, "Help Section Clicked", Toast.LENGTH_SHORT).show();
        });
    }

}