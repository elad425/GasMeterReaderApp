package com.example.gasmeterreader.viewModels;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gasmeterreader.R;
import com.example.gasmeterreader.ml.ImageAnalyzer;
import com.example.gasmeterreader.utils.StringsUtils;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiveFeedViewModel extends AndroidViewModel {
    // Detection states
    private final MutableLiveData<Boolean> isDetected = new MutableLiveData<>(false);
    private final MutableLiveData<String> idResultText = new MutableLiveData<>("");
    private final MutableLiveData<String> dataResultText = new MutableLiveData<>("");
    private final MutableLiveData<Integer> detectionStatusIcon = new MutableLiveData<>(R.drawable.ic_red_x);
    private final MutableLiveData<Boolean> isFlashOn = new MutableLiveData<>(false);

    // Detection helpers
    private final HashMap<String, Integer> detectionCounterID = new HashMap<>();
    private final HashMap<String, Integer> detectionCounterData = new HashMap<>();
    private final int detectionThreshold = 3;

    // Image analysis
    private final ImageAnalyzer imageAnalyzer;
    private final ExecutorService cameraExecutor;

    public LiveFeedViewModel(@NonNull Application application) {
        super(application);
        this.imageAnalyzer = new ImageAnalyzer(application);
        this.cameraExecutor = Executors.newSingleThreadExecutor();

        // Initialize default texts
        dataResultText.setValue(application.getString(R.string.data_not_detected));
        idResultText.setValue(application.getString(R.string.serial_not_detected));
    }

    // Getters for LiveData
    public LiveData<Boolean> getIsDetected() { return isDetected; }
    public LiveData<String> getIdResultText() { return idResultText; }
    public LiveData<String> getDataResultText() { return dataResultText; }
    public LiveData<Integer> getDetectionStatusIcon() { return detectionStatusIcon; }
    public LiveData<Boolean> getIsFlashOn() { return isFlashOn; }

    public void processImage(Bitmap rotatedBitmap) {
        if (Boolean.FALSE.equals(isDetected.getValue())) {
            imageAnalyzer.detect(rotatedBitmap);
            updateResultTexts(imageAnalyzer.getId(), imageAnalyzer.getData());
            rotatedBitmap.recycle();
        }
    }

    private void updateResultTexts(final String idResult, final String dataResult) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!idResult.isEmpty()) {
                StringsUtils.addString(idResult, detectionCounterID);
            }
            if (!dataResult.isEmpty()) {
                StringsUtils.addString(dataResult, detectionCounterData);
            }

            boolean newDetectionStatus =
                    StringsUtils.getMaxCount(detectionCounterData) >= detectionThreshold &&
                            StringsUtils.getMaxCount(detectionCounterID) >= detectionThreshold;

            isDetected.setValue(newDetectionStatus);
            updateDetectionStatus();
        });
    }

    private void updateDetectionStatus() {
        new Handler(Looper.getMainLooper()).post(() -> {
            // Update detection icon
            detectionStatusIcon.setValue(
                    Boolean.TRUE.equals(isDetected.getValue()) ?
                            R.drawable.ic_green_check : R.drawable.ic_red_x
            );

            // Update result texts
            dataResultText.setValue(String.format("קריאה: %s",
                    StringsUtils.getMostFrequentString(detectionCounterData)));

            idResultText.setValue(String.format("סיריאלי: %s",
                    StringsUtils.getMostFrequentString(detectionCounterID)));
        });
    }

    public void resetResult() {
        detectionCounterID.clear();
        detectionCounterData.clear();
        isDetected.setValue(false);
        imageAnalyzer.deleteDataDetect();
        imageAnalyzer.deleteIdDetect();
    }

    public void toggleFlash() {
        Boolean currentFlashState = isFlashOn.getValue();
        isFlashOn.setValue(currentFlashState == null || !currentFlashState);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cameraExecutor.shutdown();
    }
}