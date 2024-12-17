package com.example.gasmeterreader.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.gasmeterreader.R;
import com.example.gasmeterreader.viewModels.LiveFeedViewModel;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class LiveFeedActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ImageButton flashButton;
    private TextView idResultText;
    private TextView dataResultText;
    private ImageView detectionStatusIcon;

    private Camera camera;
    private LiveFeedViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_feed);

        // Set status bar color
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LiveFeedViewModel.class);

        // Initialize UI elements
        initializeViews();

        // Setup observers
        setupObservers();

        // Start camera
        startCamera();
    }

    private void initializeViews() {
        previewView = findViewById(R.id.viewFinder);
        flashButton = findViewById(R.id.flashButton);
        idResultText = findViewById(R.id.idResultText);
        dataResultText = findViewById(R.id.dataResultText);
        detectionStatusIcon = findViewById(R.id.detectionStatusIcon);
        Button resetResult = findViewById(R.id.reset);

        // Setup click listeners
        flashButton.setOnClickListener(v -> viewModel.toggleFlash());
        resetResult.setOnClickListener(view -> viewModel.resetResult());
    }

    private void setupObservers() {
        // Observe detection icon
        viewModel.getDetectionStatusIcon().observe(this,
                iconResource -> detectionStatusIcon.setImageResource(iconResource));

        // Observe ID result text
        viewModel.getIdResultText().observe(this,
                text -> idResultText.setText(text));

        // Observe data result text
        viewModel.getDataResultText().observe(this,
                text -> dataResultText.setText(text));

        // Observe detection status
        viewModel.getIsDetected().observe(this, isDetected -> {
            if (Boolean.TRUE.equals(isDetected)) {
                triggerVibration();
            }
        });

        // Observe flash state
        viewModel.getIsFlashOn().observe(this, isFlashOn -> {
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                camera.getCameraControl().enableTorch(isFlashOn);
                flashButton.setImageResource(
                        isFlashOn ? R.drawable.ic_flash_off : R.drawable.ic_flash_on
                );
            }
        });
    }

    private void triggerVibration() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200,
                        VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(200);
            }
        }
    }

    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException ignored) {
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build();

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageProxy -> {
            Bitmap bitmapBuffer = Bitmap.createBitmap(
                    imageProxy.getWidth(),
                    imageProxy.getHeight(),
                    Bitmap.Config.ARGB_8888
            );

            bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());
            imageProxy.close();

            Matrix matrix = new Matrix();
            matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

            Bitmap rotatedBitmap = Bitmap.createBitmap(
                    bitmapBuffer, 0, 0,
                    bitmapBuffer.getWidth(),
                    bitmapBuffer.getHeight(),
                    matrix, true);

            viewModel.processImage(rotatedBitmap);
        });

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            // Show/hide flash button based on flash unit availability
            flashButton.setVisibility(
                    camera.getCameraInfo().hasFlashUnit() ? View.VISIBLE : View.GONE
            );
        } catch (Exception ignored) {
        }
    }
}