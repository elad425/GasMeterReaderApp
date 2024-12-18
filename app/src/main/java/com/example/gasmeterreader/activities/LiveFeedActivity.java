package com.example.gasmeterreader.activities;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class LiveFeedActivity extends AppCompatActivity {
    private PreviewView previewView;
    private MaterialButton flashButton;
    private MaterialButton resetButton;
    private TextView dataResultText;
    private ImageView detectionStatusIcon;
    private TextView serialText;
    private TextView apartmentText;

    private Camera camera;
    private LiveFeedViewModel viewModel;

    // Animation durations
    private static final long ANIMATION_DURATION = 300;
    private static final float SCALE_FACTOR = 1.2f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_feed);

        // Set status bar color
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LiveFeedViewModel.class);
        viewModel.setBuilding(getIntent().getIntExtra("building_center", -1));

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
        resetButton = findViewById(R.id.reset);
        dataResultText = findViewById(R.id.dataResultText);
        detectionStatusIcon = findViewById(R.id.detectionStatusIcon);
        serialText = findViewById(R.id.serial);
        apartmentText = findViewById(R.id.apartment);

        // Setup click listeners with animations
        flashButton.setOnClickListener(v -> {
            animateButton(flashButton);
            viewModel.toggleFlash();
        });

        resetButton.setOnClickListener(view -> {
            animateButton(resetButton);
            viewModel.resetResult();
        });
    }

    private void animateButton(MaterialButton button) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.9f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.9f);
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(button, "scaleX", 0.9f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.9f, 1f);

        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);
        scaleUpX.setDuration(100);
        scaleUpY.setDuration(100);

        scaleDownX.start();
        scaleDownY.start();

        scaleDownX.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                scaleUpX.start();
                scaleUpY.start();
            }
        });
    }

    private void setupObservers() {
        // Observe detection icon with animation
        viewModel.getDetectionStatusIcon().observe(this, iconResource ->
                detectionStatusIcon.setImageResource(iconResource));

        viewModel.getListPlace().observe(this, this::nextRead);

        // Observe data result text
        viewModel.getDataResultText().observe(this, text -> {
            if(!text.contentEquals(dataResultText.getText())) {
                dataResultText.setText(text);
                animateText(dataResultText);
            }
        });

        // Observe detection status
        viewModel.getIsDetected().observe(this, isDetected -> {
            if (Boolean.TRUE.equals(isDetected)) {
                animateDetection();
                triggerVibration();
                viewModel.enterRead();
            }
        });

        // Observe flash state
        viewModel.getIsFlashOn().observe(this, isFlashOn -> {
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                camera.getCameraControl().enableTorch(isFlashOn);

                // Rotate animation for flash button
                ObjectAnimator rotation = ObjectAnimator.ofFloat(
                        flashButton,
                        "rotation",
                        isFlashOn ? 0f : 180f,
                        isFlashOn ? 180f : 360f
                );
                rotation.setDuration(ANIMATION_DURATION);
                rotation.setInterpolator(new AccelerateDecelerateInterpolator());
                rotation.start();

                flashButton.setIconResource(
                        isFlashOn ? R.drawable.ic_flash_off : R.drawable.ic_flash_on
                );
            }
        });
    }

    private void animateDetection() {
        // Pulse animation for detection icon
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(detectionStatusIcon, "scaleX", 1f, SCALE_FACTOR, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(detectionStatusIcon, "scaleY", 1f, SCALE_FACTOR, 1f);

        scaleX.setDuration(ANIMATION_DURATION);
        scaleY.setDuration(ANIMATION_DURATION);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();
    }

    private void animateText(TextView textView) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
        fadeIn.setDuration(ANIMATION_DURATION);
        fadeIn.start();
    }

    private void nextRead(int place) {
        String serialNumber = String.valueOf(Objects.requireNonNull(
                viewModel.getReadList().getValue()).get(place).getMeter_id());
        String apartmentNumber = "דירה " + Objects.requireNonNull(
                viewModel.getReadList().getValue()).get(place).getApartment();

        // Animate text changes
        animateText(serialText);
        animateText(apartmentText);

        serialText.setText(serialNumber);
        apartmentText.setText(apartmentNumber);
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
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

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