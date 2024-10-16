package com.example.heartwise;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.ContactsContract;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    private TextView tvThemeValue;
    private TextView tvLanguageValue;

    private TextView tvEmergencyContactsValue;
    private LinearLayout layoutEmergencyContacts;

    private static final int SMS_PERMISSION_CODE = 101;
    private static final int BP_THRESHOLD = 140; // Example threshold for high BP
    private List<String> emergencyContacts; // Stores emergency contact numbers
    private EditText etBloodPressure;

    private static final int CONTACT_PICKER_REQUEST = 1001;
    private static final String PREFS_NAME = "EmergencyContactsPrefs";
    private static final String PREFS_KEY_CONTACTS = "EmergencyContacts";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize App appearance
        tvThemeValue = findViewById(R.id.tv_theme_value);
        tvLanguageValue = findViewById(R.id.tv_language_value);
        LinearLayout layoutTheme = findViewById(R.id.layout_theme);
        LinearLayout layoutLanguage = findViewById(R.id.layout_language);

        //Initialize Emergency Contacts
        tvEmergencyContactsValue = findViewById(R.id.tv_emergency_contacts_value);
        layoutEmergencyContacts = findViewById(R.id.layout_emergency_contacts);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load contacts from SharedPreferences
        emergencyContacts = new ArrayList<>(getSavedContacts());

        // Update the contact count in the UI
        updateContactCount();

        etBloodPressure = findViewById(R.id.et_blood_pressure);
        Button btnCheckPressure = findViewById(R.id.btn_check_pressure);
        Button btnAddContacts = findViewById(R.id.btn_add_contacts);

        // Initialize emergency contacts (this could also be stored in a database)
        // Load saved emergency contacts
        emergencyContacts = new ArrayList<>(getSavedContacts());

        // Set onClick listener to add emergency contacts
        layoutEmergencyContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEmergencyContactsDialog();
            }
        });

        // Add contact button handler - opens dialog to add contacts
        btnAddContacts.setOnClickListener(v -> {
            addEmergencyContacts();
        });

        // Check blood pressure button handler
        btnCheckPressure.setOnClickListener(v -> {
            String bpText = etBloodPressure.getText().toString();
            if (!bpText.isEmpty()) {
                int bloodPressure = Integer.parseInt(bpText);
                if (bloodPressure > BP_THRESHOLD) {
                    // Send emergency message if blood pressure is above the threshold
                    sendEmergencyMessage(bloodPressure);
                } else {
                    Toast.makeText(SettingsActivity.this, "Blood pressure is normal", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SettingsActivity.this, "Enter valid blood pressure", Toast.LENGTH_SHORT).show();
            }
        });

        // Check for SMS permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }

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

        layoutEmergencyContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEmergencyContactsDialog();
            }
        });
    }
    // Method to display a dialog for adding emergency contacts
    private void showEmergencyContactsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Emergency Contact")
                .setItems(new String[]{"Add from Contacts", "Add Manually"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Add from contacts
                            openContactPicker();
                        } else {
                            // Add manually
                            showManualContactInputDialog();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    // Method to open contact picker
    private void openContactPicker() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_REQUEST);
    }

    // Handle contact picker result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONTACT_PICKER_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri contactUri = data.getData();
                String contactNumber = getContactNumber(contactUri);
                if (contactNumber != null) {
                    emergencyContacts.add(contactNumber);
                    saveContactsToSharedPreferences();
                    updateContactCount();
                    Toast.makeText(this, "Contact added: " + contactNumber, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to retrieve contact number", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Method to retrieve the contact number from the selected contact
    @SuppressLint("Range")
    private String getContactNumber(Uri contactUri) {
        String phoneNumber = null;
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phonesCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contactId}, null);

            if (phonesCursor != null && phonesCursor.moveToFirst()) {
                phoneNumber = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phonesCursor.close();
            }
            cursor.close();
        }
        return phoneNumber;
    }

    // Method to display a dialog for manually entering a contact number
    private void showManualContactInputDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter contact number");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Emergency Contact")
                .setView(input)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String contact = input.getText().toString();
                        if (!contact.isEmpty() && contact.matches("\\d+")) { // Ensure the contact is valid (digits only)
                            emergencyContacts.add(contact);
                            saveContactsToSharedPreferences();
                            updateContactCount();
                        } else {
                            Toast.makeText(SettingsActivity.this, "Invalid contact. Please enter a valid number.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }


    // Method to save contacts to SharedPreferences
    private void saveContactsToSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> contactSet = new HashSet<>(emergencyContacts);
        editor.putStringSet(PREFS_KEY_CONTACTS, contactSet);
        editor.apply();
    }

    // Method to retrieve contacts from SharedPreferences
    private Set<String> getSavedContacts() {
        return sharedPreferences.getStringSet(PREFS_KEY_CONTACTS, new HashSet<>());
    }

    // Method to update the contact count in the TextView
    private void updateContactCount() {
        tvEmergencyContactsValue.setText(emergencyContacts.size() + " Contacts");
    }

    private void addEmergencyContacts() {
        // Open dialog to add contacts (manually or from contact picker)
        showEmergencyContactsDialog();
    }

    // Send an emergency SMS message
    private void sendEmergencyMessage(int bloodPressure) {
        String message = "Emergency! Blood pressure is too high: " + bloodPressure;

        // Check if there are any emergency contacts saved
        if (emergencyContacts.isEmpty()) {
            Toast.makeText(this, "No emergency contacts available.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Loop through each contact and send the SMS
        SmsManager smsManager = SmsManager.getDefault();
        for (String contact : emergencyContacts) {
            smsManager.sendTextMessage(contact, null, message, null, null);
        }

        // Display a confirmation once all messages are sent
        Toast.makeText(this, "Emergency message sent to all contacts!", Toast.LENGTH_SHORT).show();
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
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
