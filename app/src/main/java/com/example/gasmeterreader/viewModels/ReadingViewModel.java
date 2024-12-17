package com.example.gasmeterreader.viewModels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.gasmeterreader.database.BuildingRepository;
import com.example.gasmeterreader.entities.Building;
import com.example.gasmeterreader.entities.Read;
import static com.example.gasmeterreader.utils.EntityUtils.sortReadsByOrder;
import java.util.List;

public class ReadingViewModel extends AndroidViewModel {
    private final BuildingRepository buildingRepository;
    private final MutableLiveData<List<Read>> reads = new MutableLiveData<>();
    private final MutableLiveData<Read> selectedRead = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cameraPermissionGranted = new MutableLiveData<>(false);
    private final MutableLiveData<String> currentReadInput = new MutableLiveData<>("");
    private Building building;

    public ReadingViewModel(Application application) {
        super(application);
        buildingRepository = new BuildingRepository(application);
    }

    public void loadReadsForBuilding(int buildingCenter) {
        if (buildingCenter != -1) {
            building = buildingRepository.getBuildingByCenter(buildingCenter);
            List<Read> readList = building.getReadList();
            reads.setValue(sortReadsByOrder(readList));
        }
    }

    public void setSelectedRead(Read read) {
        selectedRead.setValue(read);
        if (read != null  && read.getCurrent_read() != 0) {
            currentReadInput.setValue(String.valueOf(read.getCurrent_read()));
        } else {
            currentReadInput.setValue("");
        }
    }

    public void updateCurrentReadInput(String input) {
        currentReadInput.setValue(input);
        Read read = selectedRead.getValue();
        if (read != null) {
            try {
                if (input.isEmpty()) {
                    read.setCurrent_read(0);
                } else {
                    double value = Double.parseDouble(input);
                    read.setCurrent_read(value);
                }

                List<Read> currentReads = reads.getValue();
                if (currentReads != null) {
                    int index = currentReads.indexOf(read);
                    if (index != -1) {
                        currentReads.set(index, read);
                        reads.setValue(currentReads);
                        building.setReadList(reads.getValue());
                        buildingRepository.updateBuilding(building);
                    }
                }
            } catch (NumberFormatException e) {
                // Invalid number format - keep the previous value
            }
        }
    }

    public void setCameraPermissionGranted(boolean granted) {
        cameraPermissionGranted.setValue(granted);
    }

    public LiveData<List<Read>> getReads() {
        return reads;
    }

    public LiveData<Read> getSelectedRead() {
        return selectedRead;
    }

    public LiveData<Boolean> getCameraPermissionGranted() {
        return cameraPermissionGranted;
    }

    public LiveData<String> getCurrentReadInput() {
        return currentReadInput;
    }

    public Building getBuilding() {
        return building;
    }
}