package com.example.gasmeterreader.viewModels;

import android.app.Application;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.gasmeterreader.room.BuildingRepository;
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
            selectFirstUnreadRead();
        }
    }

    private void selectFirstUnreadRead() {
        List<Read> readList = reads.getValue();
        if (readList != null && !readList.isEmpty()) {
            setSelectedRead(readList.get(0));

            for (Read read : readList) {
                if (!read.isRead() || read.getCurrent_read() == 0) {
                    setSelectedRead(read);
                    break;
                }
            }
        }
    }

    public void setSelectedRead(Read read) {
        selectedRead.setValue(read);
        if (read != null && read.getCurrent_read() != 0) {
            currentReadInput.setValue(String.valueOf(read.getCurrent_read()));
        } else {
            currentReadInput.setValue("");
        }
    }

    public void moveToNextRead() {
        List<Read> readList = reads.getValue();
        Read currentRead = selectedRead.getValue();

        if (readList != null && currentRead != null) {
            int currentIndex = readList.indexOf(currentRead);
            if (currentIndex < readList.size() - 1) {
                setSelectedRead(readList.get(currentIndex + 1));
            }
        }
    }

    public void moveToPreviousRead() {
        List<Read> readList = reads.getValue();
        Read currentRead = selectedRead.getValue();

        if (readList != null && currentRead != null) {
            int currentIndex = readList.indexOf(currentRead);
            if (currentIndex > 0) {
                setSelectedRead(readList.get(currentIndex - 1));
            }
        }
    }

    public void resetRead(Read read){
        if(read != null) {
            read.setCurrent_read(0);
            read.unRead();
            updateRead(read);
        }
    }

    private void updateRead(Read read){
        if(read != null && reads.getValue() != null) {
            List<Read> currentReads = reads.getValue();
            int index = currentReads.indexOf(read);
            if (index != -1) {
                currentReads.set(index, read);
                reads.setValue(currentReads);
                building.setReadList(reads.getValue());
                building.checkCompleted();
                buildingRepository.updateBuilding(building);
            }
        }
    }

    public void updateCurrentReadInput(String input) {
        currentReadInput.setValue(input);
        Read read = selectedRead.getValue();
        if (read != null) {
            try {
                double value = input.isEmpty() ? 0 : Double.parseDouble(input.trim());
                if (value >= 0 && value <= 99999) {
                    read.setCurrent_read(value);
                    read.wasRead();
                    updateRead(read);
                } else {
                    Toast.makeText(getApplication(), "Invalid meter reading", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException ignored) {
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