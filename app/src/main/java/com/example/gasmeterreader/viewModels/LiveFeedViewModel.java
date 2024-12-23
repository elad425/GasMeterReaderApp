package com.example.gasmeterreader.viewModels;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gasmeterreader.R;
import com.example.gasmeterreader.room.BuildingRepository;
import com.example.gasmeterreader.entities.Building;
import com.example.gasmeterreader.entities.Read;
import com.example.gasmeterreader.ml.ImageAnalyzer;
import com.example.gasmeterreader.utils.StringsUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class LiveFeedViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isDetected = new MutableLiveData<>(false);
    private final MutableLiveData<String> dataResultText = new MutableLiveData<>("");
    private final MutableLiveData<Integer> detectionStatusIcon = new MutableLiveData<>(R.drawable.ic_redx);
    private final MutableLiveData<Boolean> isFlashOn = new MutableLiveData<>(false);
    private final MutableLiveData<List<Read>> reads = new MutableLiveData<>();
    private final MutableLiveData<Integer> listPlace = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> errorCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isPaused = new MutableLiveData<>(false);

    private final HashMap<String, Integer> detectionCounterData = new HashMap<>();
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
    public Building getBuilding() { return building; }

    public void processImage(Bitmap rotatedBitmap) {
        if (Boolean.FALSE.equals(isDetected.getValue()) && getListPlace().getValue() != null) {
            imageAnalyzer.detect(rotatedBitmap);
            imageAnalyzer.setRead(Objects.requireNonNull(reads.getValue()).get(getListPlace().getValue()));
            updateResultTexts(imageAnalyzer.getData());
            rotatedBitmap.recycle();
        }
    }

    private void updateResultTexts(final String dataResult) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!dataResult.isEmpty()) {
                StringsUtils.addString(dataResult, detectionCounterData);
            }

            if (StringsUtils.getMaxCount(detectionCounterData) >= detectionThreshold){
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
                    StringsUtils.getMostFrequentString(detectionCounterData)));

        });
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
            resetError();
            incrementListPlace();
        }
    }

    public void nextRead() {
        detectionCounterData.clear();
        isDetected.setValue(false);
        imageAnalyzer.deleteDataDetect();
        incrementListPlace();
        if(getListPlace().getValue() != null) {
            imageAnalyzer.setRead(Objects.requireNonNull(reads.getValue()).get(getListPlace().getValue()));
        }
    }

    public void resetDetection(){
        detectionCounterData.clear();
        isDetected.setValue(false);
        imageAnalyzer.deleteDataDetect();
        errorCount.setValue(0);
        imageAnalyzer.resetError();
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

    public void setBuilding(int center){
        building = buildingRepository.getBuildingByCenter(center);
        reads.setValue(building.getReadList());
        if(listPlace.getValue() != null) {
            while (Objects.requireNonNull(reads.getValue()).get(listPlace.getValue()).getCurrent_read() != 0) {
                incrementListPlace();
            }
        }
    }

    public void incrementListPlace(){
        if (listPlace.getValue() != null && listPlace.getValue() < Objects.requireNonNull(reads.getValue()).size() - 1) {
            listPlace.setValue(listPlace.getValue() + 1);
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
            resetError();
            incrementListPlace();
        }
    }

    public void setListPlace(int position) {
        if (position >= 0 && position < Objects.requireNonNull(reads.getValue()).size()) {
            listPlace.setValue(position);
            detectionCounterData.clear();
            isDetected.setValue(false);
            imageAnalyzer.deleteDataDetect();
            imageAnalyzer.setRead(Objects.requireNonNull(reads.getValue()).get(position));
        }
    }

}