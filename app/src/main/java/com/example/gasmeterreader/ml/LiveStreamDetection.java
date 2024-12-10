package com.example.gasmeterreader.ml;

import static com.example.gasmeterreader.utils.StringsUtils.addString;
import static com.example.gasmeterreader.utils.StringsUtils.getMaxCount;
import static com.example.gasmeterreader.utils.StringsUtils.getMostFrequentString;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.gasmeterreader.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiveStreamDetection {
    private final Context context;
    private final PreviewView previewView;
    private final ExecutorService cameraExecutor;
    private Camera camera;
    private boolean isFlashOn = false;
    private final ImageButton flashButton;
    private final TextView idResultText;
    private final TextView dataResultText;
    private final ImageAnalyzer imageAnalyzer;
    private final ImageView detectionStatusIcon;
    private final HashMap<String, Integer> detectionCounterID;
    private final HashMap<String, Integer> detectionCounterData;
    private final int detectionThreshold;
    private Boolean isDetected;

    public LiveStreamDetection(Context context, PreviewView previewView,
                               ImageButton flashButton,
                               TextView idResultText,
                               TextView dataResultText,
                               ImageView detectionStatusIcon) {
        this.context = context;
        this.previewView = previewView;
        this.flashButton = flashButton;
        this.idResultText = idResultText;
        this.dataResultText = dataResultText;
        this.detectionCounterID = new HashMap<>();
        this.detectionCounterData = new HashMap<>();
        this.detectionStatusIcon = detectionStatusIcon;
        this.cameraExecutor = Executors.newSingleThreadExecutor();
        this.imageAnalyzer = new ImageAnalyzer(this.context);
        this.detectionThreshold = 2;
        this.isDetected = false;
        this.dataResultText.setText(R.string.no_detection);
        this.idResultText.setText(R.string.no_detection);
        setupFlashButton();
    }

    private void updateResultTexts(final String idResult, final String dataResult) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!idResult.isEmpty()) {
                addString(idResult,detectionCounterID);
            }
            if (!dataResult.isEmpty()) {
                addString(dataResult,detectionCounterData);
            }
            this.isDetected = getMaxCount(detectionCounterData) >= detectionThreshold;
            updateDetectionStatus();
        });
    }

    private void updateDetectionStatus() {
        new Handler(Looper.getMainLooper()).post(() -> {
            detectionStatusIcon.setImageResource(
                    isDetected ? R.drawable.ic_green_check : R.drawable.ic_red_x);
            dataResultText.setText(String.format("Data: %s", getMostFrequentString(detectionCounterData)));
            idResultText.setText(String.format("ID: %s", getMostFrequentString(detectionCounterID)));
            if (isDetected) {
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else { vibrator.vibrate(200);}
                }
            }
        });
    }

    private void setupFlashButton() {
        flashButton.setOnClickListener(v -> toggleFlash());
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
                ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException ignored) {}
        }, ContextCompat.getMainExecutor(context));
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
                updateResultTexts(imageAnalyzer.getId(),imageAnalyzer.getData());
                rotatedBitmap.recycle();
            }
        });

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview,
                    imageAnalysis);
            flashButton.setVisibility(camera.getCameraInfo().hasFlashUnit() ?
                    View.VISIBLE : View.GONE);
        } catch (Exception ignored) {}
    }

    public void close(){
        imageAnalyzer.close();
    }

}