package com.example.gasmeterreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.example.gasmeterreader.ml.ImageAnalyzer;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.gasmeterreader.utils.StringsUtils.addString;
import static com.example.gasmeterreader.utils.StringsUtils.getMaxCount;
import static com.example.gasmeterreader.utils.StringsUtils.getMostFrequentString;

public class LiveFeedActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ImageButton flashButton;
    private TextView idResultText;
    private TextView dataResultText;
    private ImageView detectionStatusIcon;

    private Camera camera;
    private boolean isFlashOn = false;
    private final ExecutorService cameraExecutor;
    private ImageAnalyzer imageAnalyzer;
    private final HashMap<String, Integer> detectionCounterID;
    private final HashMap<String, Integer> detectionCounterData;
    private final int detectionThreshold;
    private Boolean isDetected;

    public LiveFeedActivity() {
        this.cameraExecutor = Executors.newSingleThreadExecutor();
        this.detectionCounterID = new HashMap<>();
        this.detectionCounterData = new HashMap<>();
        this.detectionThreshold = 3;
        this.isDetected = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_feed);

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));

        previewView = findViewById(R.id.viewFinder);
        flashButton = findViewById(R.id.flashButton);
        idResultText = findViewById(R.id.idResultText);
        dataResultText = findViewById(R.id.dataResultText);
        detectionStatusIcon = findViewById(R.id.detectionStatusIcon);
        Button resetResult = findViewById(R.id.reset);

        this.imageAnalyzer = new ImageAnalyzer(this);
        this.dataResultText.setText(R.string.data_not_detected);
        this.idResultText.setText(R.string.serial_not_detected);

        flashButton.setOnClickListener(v -> toggleFlash());
        resetResult.setOnClickListener(view -> resetResult());
        startCamera();
    }

    private void updateResultTexts(final String idResult, final String dataResult) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!idResult.isEmpty()) {
                addString(idResult, detectionCounterID);
            }
            if (!dataResult.isEmpty()) {
                addString(dataResult, detectionCounterData);
            }
            this.isDetected = getMaxCount(detectionCounterData) >= detectionThreshold &&
                    getMaxCount(detectionCounterID) >= detectionThreshold;
            updateDetectionStatus();
        });
    }

    private void updateDetectionStatus() {
        new Handler(Looper.getMainLooper()).post(() -> {
            detectionStatusIcon.setImageResource(
                    isDetected ? R.drawable.ic_green_check : R.drawable.ic_red_x);
            dataResultText.setText(String.format("Data: %s",
                    getMostFrequentString(detectionCounterData)));
            idResultText.setText(String.format("Serial: %s",
                    getMostFrequentString(detectionCounterID)));
            if (isDetected) {
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
        });
    }

    private void resetResult() {
        this.detectionCounterID.clear();
        this.detectionCounterData.clear();
        this.isDetected = false;
        this.imageAnalyzer.deleteDataDetect();
        this.imageAnalyzer.deleteIdDetect();
    }

    private void toggleFlash() {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            isFlashOn = !isFlashOn;
            camera.getCameraControl().enableTorch(isFlashOn);
            flashButton.setImageResource(isFlashOn ?
                    R.drawable.ic_flash_off :
                    R.drawable.ic_flash_on);
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
        Preview preview = new Preview.Builder()
                .build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build();

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
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

            if (!isDetected) {
                imageAnalyzer.detect(rotatedBitmap);
                updateResultTexts(imageAnalyzer.getId(), imageAnalyzer.getData());
                rotatedBitmap.recycle();
            }
        });

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview,
                    imageAnalysis);
            flashButton.setVisibility(camera.getCameraInfo().hasFlashUnit() ?
                    View.VISIBLE : View.GONE);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}