package com.example.heartwise;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;


public class SettingsActivity extends AppCompatActivity {
    private TextView tvThemeValue;
    private TextView tvLanguageValue;

    @Override
            protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);

            // Initialize views
            tvThemeValue = findViewById(R.id.tv_theme_value);
            tvLanguageValue = findViewById(R.id.tv_language_value);
            LinearLayout layoutTheme = findViewById(R.id.layout_theme);
            LinearLayout layoutLanguage = findViewById(R.id.layout_language);

            // Set up click listeners
            layoutTheme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showThemeSelectionDialog();
                }
            });

            layoutLanguage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLanguageSelectionDialog();
                }
            });
        // Initialize Bottom Navigation View
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
    }

        private void showThemeSelectionDialog() {
            final String[] themes = {"System Default", "Light", "Dark"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose Theme")
                    .setSingleChoiceItems(themes, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tvThemeValue.setText(themes[which]);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }

        private void showLanguageSelectionDialog() {
            final String[] languages = {"English", "Spanish", "Mandarin Chinese", "Hindi", "Arabic", "Bengali", "Portuguese", "Russian", "Japanese", "German", "French", "Urdu"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose Language")
                    .setSingleChoiceItems(languages, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tvLanguageValue.setText(languages[which]);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
    }
}
