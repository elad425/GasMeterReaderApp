package com.example.gasmeterreader.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gasmeterreader.R;
import com.example.gasmeterreader.adapters.ReadingAdapter;
import com.example.gasmeterreader.entities.Read;
import com.example.gasmeterreader.viewModels.ReadingViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Objects;

public class ReadingActivity extends AppCompatActivity {
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ReadingViewModel viewModel;
    private TextView ownerText, apartmentText, statusText, serialText, lastReadText;
    private TextInputEditText currentReadInput;
    private boolean isUpdatingInput = false;
    private int buildingNumber;
    private ReadingAdapter readingAdapter;
    private ImageButton nextButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        initializeViews();
        setupViewModel();
        setupRecyclerView();
        setupCameraPermissionLauncher();
        setupLiveFeedButton();
        setupCurrentReadInput();
        setupNavigationButtons();

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
    }

    private void initializeViews() {
        ownerText = findViewById(R.id.owner_text);
        apartmentText = findViewById(R.id.apartment_text);
        statusText = findViewById(R.id.status_text);
        serialText = findViewById(R.id.serial_text);
        lastReadText = findViewById(R.id.last_call_text);
        currentReadInput = findViewById(R.id.current_call_input);
        nextButton = findViewById(R.id.next);
        backButton = findViewById(R.id.back);
    }

    private void setupNavigationButtons() {
        nextButton.setOnClickListener(v -> viewModel.moveToNextRead());
        backButton.setOnClickListener(v -> viewModel.moveToPreviousRead());
    }

    private void setupCurrentReadInput() {
        currentReadInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdatingInput) {
                    viewModel.updateCurrentReadInput(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Add keyboard action listener to move to next read
        currentReadInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_NEXT) {
                // Get the current list of reads
                List<Read> currentReads = viewModel.getReads().getValue();
                Read currentSelectedRead = viewModel.getSelectedRead().getValue();

                if (currentReads != null && currentSelectedRead != null) {
                    int currentIndex = currentReads.indexOf(currentSelectedRead);

                    // If there's a next read, select it
                    if (currentIndex < currentReads.size() - 1) {
                        Read nextRead = currentReads.get(currentIndex + 1);
                        viewModel.setSelectedRead(nextRead);
                    } else {
                        Toast.makeText(this, "סיום קריאה", Toast.LENGTH_SHORT).show();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(currentReadInput.getWindowToken(), 0);
                    }
                    return true;
                }
            }
            return false;
        });

        // Observe changes to the current read input
        viewModel.getCurrentReadInput().observe(this, input -> {
            if (input != null && !input.equals(Objects.requireNonNull(currentReadInput.getText()).toString())) {
                isUpdatingInput = true;
                currentReadInput.setText(input);
                currentReadInput.setSelection(input.length());
                isUpdatingInput = false;
            }
        });

        // Update input field when selected read changes
        viewModel.getSelectedRead().observe(this, read -> {
            if (read != null) {
                String currentValue = read.getCurrent_read() != 0 ?
                        String.valueOf(read.getCurrent_read()) : "";
                if (!currentValue.equals(Objects.requireNonNull(currentReadInput.getText()).toString())) {
                    isUpdatingInput = true;
                    currentReadInput.setText(currentValue);
                    currentReadInput.setSelection(currentValue.length());
                    isUpdatingInput = false;
                }
            } else {
                currentReadInput.setText("");
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ReadingViewModel.class);
        buildingNumber = getIntent().getIntExtra("building_center", -1);
        viewModel.loadReadsForBuilding(buildingNumber);

        viewModel.getSelectedRead().observe(this, read -> {
            if (read != null) {
                ownerText.setText(read.getUser_name());
                apartmentText.setText(String.valueOf(read.getApartment()));
                statusText.setText(read.getUser_status());
                serialText.setText(String.valueOf(read.getMeter_id()));
                lastReadText.setText(String.valueOf(read.getLast_read()));
            }
        });

        viewModel.getCameraPermissionGranted().observe(this, granted -> {
            if (granted) {
                startLiveFeedActivity();
            }
        });
    }

    private void setupRecyclerView() {
        RecyclerView lstReadings = findViewById(R.id.read_lst);
        readingAdapter = new ReadingAdapter(null, this, viewModel);
        lstReadings.setAdapter(readingAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        lstReadings.setLayoutManager(layoutManager);

        // Observe reads
        viewModel.getReads().observe(this, reads -> {
            readingAdapter.updateReadings(reads);
            // If no read is currently selected and there are reads, select the first unread
            if (viewModel.getSelectedRead().getValue() == null && !reads.isEmpty()) {
                for (Read read : reads) {
                    if (!read.isRead()) {
                        viewModel.setSelectedRead(read);
                        break;
                    }
                }
                // If all reads are read, select the first one
                if (viewModel.getSelectedRead().getValue() == null) {
                    viewModel.setSelectedRead(reads.get(0));
                }
            }
        });

        // Observe selected read to update RecyclerView and scroll position
        viewModel.getSelectedRead().observe(this, selectedRead -> {
            if (selectedRead != null && readingAdapter != null) {
                readingAdapter.notifyDataSetChanged();

                // Find the position of the selected read
                List<Read> currentReads = viewModel.getReads().getValue();
                if (currentReads != null) {
                    int position = currentReads.indexOf(selectedRead);
                    if (position != -1) {
                        // Calculate the scroll position to show the selected read at the bottom
                        int totalItemCount = layoutManager.getItemCount();
                        int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();

                        // If the selected read is not in the visible area, scroll to show it at the bottom
                        if (position < lastVisibleItemPosition - 1 || position > lastVisibleItemPosition) {
                            lstReadings.scrollToPosition(Math.min(position, totalItemCount - 1));
                        }
                    }
                }
            }
        });
    }

    private void setupCameraPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        viewModel.setCameraPermissionGranted(true);
                    } else {
                        Toast.makeText(this,
                                "Camera permission is required to use this feature",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupLiveFeedButton() {
        FloatingActionButton liveFeed = findViewById(R.id.live_feed);
        liveFeed.setOnClickListener(v -> checkAndRequestCameraPermission());
    }

    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            viewModel.setCameraPermissionGranted(true);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startLiveFeedActivity() {
        Intent intent = new Intent(this, LiveFeedActivity.class);
        intent.putExtra("building_center", viewModel.getBuilding().getCenter());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadReadsForBuilding(buildingNumber);
    }
}