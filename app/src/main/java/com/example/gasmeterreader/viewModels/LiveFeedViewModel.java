package com.example.gasmeterreader.viewModels;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gasmeterreader.R;
import com.example.gasmeterreader.data.BuildingRepository;
import com.example.gasmeterreader.entities.Building;
import com.example.gasmeterreader.entities.Read;
import com.example.gasmeterreader.ml.ImageAnalyzer;
import com.example.gasmeterreader.utils.ResultUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiveFeedViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isDetected = new MutableLiveData<>(false);
    private final MutableLiveData<String> dataResultText = new MutableLiveData<>("");
    private final MutableLiveData<Integer> detectionStatusIcon = new MutableLiveData<>(R.drawable.ic_redx);
    private final MutableLiveData<Boolean> isFlashOn = new MutableLiveData<>(false);
    private final MutableLiveData<List<Read>> reads = new MutableLiveData<>();
    private final MutableLiveData<Integer> listPlace = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> errorCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isPaused = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);

    private final HashMap<String, Integer> detectionCounterData = new HashMap<>();
    private final HashMap<String, Integer> errorCounterData = new HashMap<>();
    private final int detectionThreshold = 3;
    private Building building;

    private final ImageAnalyzer imageAnalyzer;
    private final BuildingRepository buildingRepository;

    public LiveFeedViewModel(@NonNull Application application) {
        super(application);
        this.imageAnalyzer = new ImageAnalyzer(application);
        this.buildingRepository = new BuildingRepository(application);
    }

    public LiveData<Boolean> getIsDetected() { return isDetected; }
    public LiveData<String> getDataResultText() { return dataResultText; }
    public LiveData<Integer> getDetectionStatusIcon() { return detectionStatusIcon; }
    public LiveData<Boolean> getIsFlashOn() { return isFlashOn; }
    public LiveData<Integer> getListPlace() { return listPlace; }
    public LiveData<Integer> getErrorCount() { return errorCount; }
    public LiveData<List<Read>> getReadList() { return reads; }
    public LiveData<Boolean> getIsPaused() { return isPaused;}
    public LiveData<Boolean> getIsLoading() { return isLoading;}
    public Building getBuilding() { return building; }

    public void setData(int center, int place){
        building = buildingRepository.getBuildingByCenter(center);
        reads.postValue(building.getReadList());
        if (place == -1){
            place = 0;
        }
        listPlace.postValue(place);
        initializeAnalyzer();
    }

    public void initializeAnalyzer() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            imageAnalyzer.initializeDetectors();
            isPaused.postValue(false);
            isLoading.postValue(false);
        });
    }

    public void processImage(Bitmap rotatedBitmap) {
        if (Boolean.FALSE.equals(isDetected.getValue()) && getListPlace().getValue() != null) {
            imageAnalyzer.detect(rotatedBitmap);
            imageAnalyzer.setRead(Objects.requireNonNull(reads.getValue()).get(getListPlace().getValue()));
            updateResultTexts(imageAnalyzer.getData());
            rotatedBitmap.recycle();
        }
    }

    private void updateResultTexts(final Pair<String, Integer> dataResult) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (dataResult != null && !dataResult.first.isEmpty()) {
                if (dataResult.second == 1) {
                    ResultUtils.addString(dataResult.first, detectionCounterData);
                } else if (!dataResult.first.equals("None")) {
                    ResultUtils.addString(dataResult.first, errorCounterData);
                }
            }

            if (ResultUtils.getMaxCount(detectionCounterData) >= detectionThreshold){
                isDetected.setValue(Boolean.TRUE);
            }
            updateDetectionStatus();
            errorCount.setValue(imageAnalyzer.getErrorCount());
        });
    }

    private void updateDetectionStatus() {
        new Handler(Looper.getMainLooper()).post(() -> {
            detectionStatusIcon.setValue(
                    Boolean.TRUE.equals(isDetected.getValue()) ?
                            R.drawable.ic_greenv : R.drawable.ic_redx
            );
            dataResultText.setValue(String.format("%s",
                    ResultUtils.getMostFrequentString(detectionCounterData)));

        });
    }

    public String getHigherError(){
        if (ResultUtils.getMaxCount(errorCounterData) >= detectionThreshold) {
            return ResultUtils.getMostFrequentString(errorCounterData);
        } else return "";
    }

    public void setReadManual(String read){
        if(listPlace.getValue() != null) {
            List<Read> temp = reads.getValue();
            assert temp != null;
            temp.get(listPlace.getValue()).setCurrent_read(Double.parseDouble(
                    Objects.requireNonNull(read)));
            temp.get(listPlace.getValue()).wasRead();
            reads.setValue(temp);
            building.setReadList(temp);
            building.checkCompleted();
            buildingRepository.updateBuilding(building);
            nextRead();
        }
    }

    public void nextRead() {
        detectionCounterData.clear();
        errorCounterData.clear();
        isDetected.setValue(Boolean.FALSE);
        imageAnalyzer.deleteDataDetect();
        resetError();
        incrementListPlace();
        if(getListPlace().getValue() != null) {
            imageAnalyzer.setRead(Objects.requireNonNull(reads.getValue()).get(getListPlace().getValue()));
        }
    }

    public void resetDetection(){
        detectionCounterData.clear();
        errorCounterData.clear();
        isDetected.setValue(Boolean.FALSE);
        imageAnalyzer.deleteDataDetect();
        resetError();
    }

    public void resetError(){
        errorCount.setValue(0);
        imageAnalyzer.resetError();
    }

    public void toggleFlash() {
        Boolean currentFlashState = isFlashOn.getValue();
        isFlashOn.setValue(currentFlashState == null || !currentFlashState);
    }

    public void setPaused(boolean paused) {
        isPaused.setValue(paused);
    }

    public void incrementListPlace() {
        if (!building.isComplete() && listPlace.getValue() != null) {
            int size = Objects.requireNonNull(reads.getValue()).size();
            int currentIndex = listPlace.getValue();
            for (int i = 0; i < size; i++) {
                currentIndex += 1;
                if (currentIndex >= size) {
                    currentIndex = 0;
                }
                if (Objects.requireNonNull(reads.getValue()).
                        get(currentIndex).getCurrent_read() == 0) {
                    break;
                }
            }
            listPlace.setValue(currentIndex);
        }
    }

    public void enterRead(){
        if(Objects.equals(dataResultText.getValue(), "מחפש..")){
            Toast.makeText(getApplication(), "לא נמצאה קריאה", Toast.LENGTH_SHORT).show();
            return;
        }
        if(listPlace.getValue() != null) {
            List<Read> temp = reads.getValue();
            assert temp != null;
            temp.get(listPlace.getValue()).setCurrent_read(Double.parseDouble(
                    Objects.requireNonNull(dataResultText.getValue())));
            temp.get(listPlace.getValue()).wasRead();
            reads.setValue(temp);
            building.setReadList(temp);
            building.checkCompleted();
            buildingRepository.updateBuilding(building);
            nextRead();
        }
    }

    public void setListPlace(int position) {
        if (position >= 0 && position < Objects.requireNonNull(reads.getValue()).size()) {
            listPlace.setValue(position);
            detectionCounterData.clear();
            errorCounterData.clear();
            resetError();
            isDetected.setValue(Boolean.FALSE);
            imageAnalyzer.deleteDataDetect();
            imageAnalyzer.setRead(Objects.requireNonNull(reads.getValue()).get(position));
        }
    }

}