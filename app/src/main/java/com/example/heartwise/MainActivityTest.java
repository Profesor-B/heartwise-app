package com.example.heartwise; // Replace with your actual package name

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.view.ViewGroup.LayoutParams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivityTest extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String TAG = "MainActivityTest";

    private SurfaceView preview;
    private Camera camera;
    private boolean isMeasuring = false;
    private TextView resultText;
    private Button startButton;

    private static final int SAMPLE_SIZE = 256; // Number of samples to collect (power of 2)
    private static final double SAMPLING_RATE = 30.0; // Sampling rate in Hz (frames per second)
    private List<Integer> redAvgList = new ArrayList<>();
    private static final int CAMERA_REQUEST_CODE = 100;

    private boolean isSurfaceCreated = false;

    // TextToSpeech instance
    private TextToSpeech tts;
    private boolean isTtsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);

        Log.d(TAG, "onCreate: Initializing TTS and UI elements");

        // Initialize TextToSpeech
        tts = new TextToSpeech(this, this);

        // Initialize UI elements
        startButton = findViewById(R.id.start_button);
        resultText = findViewById(R.id.result_text);

        // Set up camera preview
        preview = new SurfaceView(this);
        FrameLayout.LayoutParams previewLayoutParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        preview.setLayoutParams(previewLayoutParams);

        SurfaceHolder holder = preview.getHolder();
        holder.addCallback(surfaceCallback);

        // Add the preview to the layout
        FrameLayout previewLayout = findViewById(R.id.preview_layout);
        previewLayout.addView(preview);

        // Start measurement on button click
        startButton.setOnClickListener(v -> {
            if (!isMeasuring) {
                startMeasurement();
            } else {
                stopMeasurement();
            }
        });

        checkCameraPermission();
    }

    @SuppressWarnings("deprecation")
    private void startMeasurement() {
        Log.d(TAG, "startMeasurement: Starting measurement");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted; request it
            Log.w(TAG, "startMeasurement: Camera permission not granted, requesting permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            return; // Exit the method to prevent proceeding without permission
        }

        try {
            if (camera == null) {
                Log.d(TAG, "startMeasurement: Opening camera");
                camera = Camera.open();

                // Adjust camera display orientation
                setCameraDisplayOrientation();

                camera.setPreviewCallback(previewCallback);

                Camera.Parameters params = camera.getParameters();

                // Set the optimal preview size
                Camera.Size optimalSize = getOptimalPreviewSize(params.getSupportedPreviewSizes(), preview.getWidth(), preview.getHeight());
                if (optimalSize != null) {
                    params.setPreviewSize(optimalSize.width, optimalSize.height);
                    Log.d(TAG, "startMeasurement: Set preview size to " + optimalSize.width + "x" + optimalSize.height);
                }

                List<String> supportedFlashModes = params.getSupportedFlashModes();
                if (supportedFlashModes != null && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    Log.d(TAG, "startMeasurement: Torch mode enabled");
                } else {
                    String message = "Torch mode not supported on this device.";
                    Log.w(TAG, "startMeasurement: " + message);
                    showMessage(message);
                }
                camera.setParameters(params);
            }

            if (isSurfaceCreated) {
                Log.d(TAG, "startMeasurement: Surface is created, starting camera preview");
                camera.setPreviewDisplay(preview.getHolder());
                camera.startPreview();
            } else {
                String message = "Surface not ready. Please try again.";
                Log.w(TAG, "startMeasurement: " + message);
                showMessage(message);
                return;
            }

            isMeasuring = true;
            startButton.setText("Stop Measurement");
            resultText.setText("Measuring...");

        } catch (Exception e) {
            Log.e(TAG, "startMeasurement: Failed to access the camera.", e);
            String message = "Failed to access the camera.";
            showMessage(message);
        }
    }

    private void setCameraDisplayOrientation() {
        if (camera == null) {
            Log.w(TAG, "setCameraDisplayOrientation: Camera is null");
            return;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // Compensate the mirror
        } else {  // Back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
        Log.d(TAG, "setCameraDisplayOrientation: Set camera orientation to " + result + " degrees");
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w; // Because camera preview sizes are expressed as width x height

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find size matching aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one matching the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        Log.d(TAG, "getOptimalPreviewSize: Optimal preview size is " + optimalSize.width + "x" + optimalSize.height);
        return optimalSize;
    }

    private void stopMeasurement() {
        Log.d(TAG, "stopMeasurement: Stopping measurement");
        isMeasuring = false;
        if (camera != null) {
            try {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.release();
                camera = null;
                Log.d(TAG, "stopMeasurement: Camera released");
            } catch (Exception e) {
                Log.e(TAG, "stopMeasurement: Error releasing camera", e);
            }
        }
        startButton.setText("Start Measurement");
        String message = "Measurement stopped.";
        showMessage(message);
    }

    // SurfaceHolder Callback
    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "surfaceCreated: Surface created");
            isSurfaceCreated = true;
            if (isMeasuring && camera != null) {
                try {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                    Log.d(TAG, "surfaceCreated: Camera preview started");
                } catch (IOException e) {
                    Log.e(TAG, "surfaceCreated: Error starting camera preview", e);
                    String message = "Error starting camera preview.";
                    showMessage(message);
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "surfaceChanged: Surface changed");
            // Handle surface changes
            if (holder.getSurface() == null || camera == null) {
                Log.w(TAG, "surfaceChanged: Holder or camera is null");
                return;
            }

            // Stop preview before making changes
            try {
                camera.stopPreview();
                Log.d(TAG, "surfaceChanged: Camera preview stopped");
            } catch (Exception e) {
                // Ignore: tried to stop a non-existent preview
                Log.w(TAG, "surfaceChanged: Tried to stop a non-existent preview", e);
            }

            // Start preview with new settings
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
                Log.d(TAG, "surfaceChanged: Camera preview restarted");
            } catch (Exception e) {
                Log.e(TAG, "surfaceChanged: Error restarting camera preview", e);
                String message = "Error restarting camera preview.";
                showMessage(message);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed: Surface destroyed");
            isSurfaceCreated = false;
            if (camera != null) {
                camera.stopPreview();
                Log.d(TAG, "surfaceDestroyed: Camera preview stopped");
            }
        }
    };

    // Camera Preview Callback
    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (!isMeasuring) return;

            try {
                Camera.Size size = camera.getParameters().getPreviewSize();
                int width = size.width;
                int height = size.height;

                // Convert YUV to red average
                int redAvg = decodeYUV420SPtoRedAvg(data, width, height);

                // Add redAvg to a data buffer
                redAvgList.add(redAvg);

                // Process the buffer to estimate heart rate and blood pressure
                if (redAvgList.size() == SAMPLE_SIZE) {
                    processSignal(new ArrayList<>(redAvgList)); // Pass a copy of the list
                }
            } catch (Exception e) {
                Log.e(TAG, "onPreviewFrame: Error processing frame", e);
            }
        }
    };

    private void processSignal(List<Integer> redAvgList) {
        Log.d(TAG, "processSignal: Processing signal");
        new Thread(() -> {
            double heartRate = calculateHeartRate(redAvgList);
            if (heartRate == -1) {
                String message = "Unable to detect heart rate. Please try again.";
                Log.w(TAG, "processSignal: " + message);
                runOnUiThread(() -> {
                    resultText.setText(message);
                    showMessage(message);
                });
            } else {
                String bloodPressure = estimateBloodPressure(heartRate);
                String result = String.format("Heart Rate: %.1f BPM\nEstimated Blood Pressure: %s mmHg", heartRate, bloodPressure);
                Log.d(TAG, "processSignal: " + result);
                runOnUiThread(() -> {
                    resultText.setText(result);
                    showMessage(result);
                });
            }
            redAvgList.clear();
            stopMeasurement();
        }).start();
    }

    private int decodeYUV420SPtoRedAvg(byte[] yuv420sp, int width, int height) {
        // This method can be improved to accurately calculate the red channel average
        // For simplicity, we're calculating the Y (luma) average here
        int frameSize = width * height;
        int sum = 0;
        for (int i = 0; i < frameSize; i++) {
            int y = yuv420sp[i] & 0xff;
            sum += y;
        }
        int avg = sum / frameSize;
        Log.d(TAG, "decodeYUV420SPtoRedAvg: Y average = " + avg);
        return avg;
    }

    private double calculateHeartRate(List<Integer> redAvgList) {
        Log.d(TAG, "calculateHeartRate: Calculating heart rate");
        int n = redAvgList.size();

        // Ensure n is a power of 2
        int m = (int) (Math.log(n) / Math.log(2));
        if (Math.pow(2, m) != n) {
            n = (int) Math.pow(2, m);
            redAvgList = redAvgList.subList(0, n);
            Log.w(TAG, "calculateHeartRate: Adjusted sample size to next lower power of 2: " + n);
        }

        double[] real = new double[n];
        double[] imag = new double[n];
        for (int i = 0; i < n; i++) {
            real[i] = redAvgList.get(i);
            imag[i] = 0;
        }

        // Remove DC component
        double mean = 0;
        for (double val : real) {
            mean += val;
        }
        mean /= n;
        for (int i = 0; i < n; i++) {
            real[i] -= mean;
        }

        // Perform FFT
        try {
            FFT.fft(real, imag);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "calculateHeartRate: FFT error", e);
            return -1;
        }

        // Compute magnitudes
        double[] magnitudes = new double[n / 2];
        for (int i = 1; i < n / 2; i++) { // Start from 1 to skip DC component
            magnitudes[i] = Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
        }

        // Find peak frequency
        int peakIndex = 1;
        double maxMagnitude = magnitudes[1];
        for (int i = 2; i < n / 2; i++) {
            if (magnitudes[i] > maxMagnitude) {
                maxMagnitude = magnitudes[i];
                peakIndex = i;
            }
        }

        // Frequency calculation
        double frequencyResolution = SAMPLING_RATE / n;
        double heartRateFrequency = peakIndex * frequencyResolution;

        // Convert to BPM
        double heartRate = heartRateFrequency * 60.0;

        Log.d(TAG, "calculateHeartRate: Peak frequency index = " + peakIndex + ", Frequency = " + heartRateFrequency + " Hz, Heart Rate = " + heartRate + " BPM");

        // Validate heart rate
        if (heartRate < 40 || heartRate > 180) {
            Log.w(TAG, "calculateHeartRate: Detected heart rate out of valid range: " + heartRate);
            return -1;
        }

        return heartRate;
    }

    private String estimateBloodPressure(double heartRate) {
        // Placeholder formula for demonstration
        double systolic = 120 + (heartRate - 70) * 0.5;
        double diastolic = 80 + (heartRate - 70) * 0.3;
        String bloodPressure = Math.round(systolic) + "/" + Math.round(diastolic);
        Log.d(TAG, "estimateBloodPressure: Estimated BP = " + bloodPressure);
        return bloodPressure;
    }

    private void checkCameraPermission() {
        Log.d(TAG, "checkCameraPermission: Checking camera permission");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted; request it
            Log.w(TAG, "checkCameraPermission: Camera permission not granted, requesting permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    // Handle the permission request response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Handling permission result");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                String message = "Camera permission granted.";
                Log.d(TAG, "onRequestPermissionsResult: " + message);
                showMessage(message);
            } else {
                // Permission denied
                String message = "Camera permission is required.";
                Log.w(TAG, "onRequestPermissionsResult: " + message);
                showMessage(message);
                startButton.setEnabled(false);
            }
        }
    }

    // TextToSpeech.OnInitListener implementation
    @Override
    public void onInit(int status) {
        Log.d(TAG, "onInit: TTS initialization started");
        if (status == TextToSpeech.SUCCESS) {
            // Set language to default locale
            int result = tts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "onInit: Language not supported for TTS.");
                Toast.makeText(this, "Language not supported for TTS.", Toast.LENGTH_LONG).show();
            } else {
                isTtsInitialized = true;
                Log.d(TAG, "onInit: TTS initialized successfully");
            }
        } else {
            Log.e(TAG, "onInit: TTS Initialization failed.");
            Toast.makeText(this, "TTS Initialization failed.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Helper method to show messages using both Toast and TTS.
     *
     * @param message The message to display and speak.
     */
    private void showMessage(String message) {
        // Show Toast
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.d(TAG, "showMessage: " + message);

        // Speak the message if TTS is initialized
        speakOut(message);
    }

    /**
     * Speaks out the given text using TextToSpeech.
     *
     * @param text The text to speak.
     */
    private void speakOut(String text) {
        if (tts != null && isTtsInitialized) {
            // For Android API >= 21, you can specify queue mode and utterance ID
            int speakStatus = tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
            if (speakStatus == TextToSpeech.ERROR) {
                Log.e(TAG, "speakOut: TTS error while speaking");
            } else {
                Log.d(TAG, "speakOut: Speaking - " + text);
            }
        } else {
            Log.w(TAG, "speakOut: TTS not initialized");
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Destroying activity and shutting down TTS");
        // Shutdown TTS to free up resources
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            Log.d(TAG, "onDestroy: TTS shutdown");
        }
        super.onDestroy();
    }
}
