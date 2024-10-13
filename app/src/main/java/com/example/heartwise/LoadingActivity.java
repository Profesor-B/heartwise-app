package com.example.heartwise;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {

    private ProgressBar loadingProgressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        loadingProgressBar = findViewById(R.id.loading_progress);

        // Simulate progress over 3 seconds
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 1;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadingProgressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        Thread.sleep(30); // Sleep for 30ms to simulate slow loading
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Once loading is complete, navigate to the MaintenanceActivity
                Intent intent = new Intent(LoadingActivity.this, HomeActivityMain.class);
                startActivity(intent);
                finish();
            }
        }).start();
    }
}
