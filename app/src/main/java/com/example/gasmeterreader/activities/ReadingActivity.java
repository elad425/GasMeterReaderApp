package com.example.gasmeterreader.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.gasmeterreader.viewModels.ReadingViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class ReadingActivity extends AppCompatActivity {
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ReadingViewModel viewModel;
    private TextView ownerText, apartmentText, statusText, serialText, lastReadText;
    private TextInputEditText currentReadInput;
    private boolean isUpdatingInput = false;

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

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
    }

    private void initializeViews() {
        ownerText = findViewById(R.id.owner_text);
        apartmentText = findViewById(R.id.apartment_text);
        statusText = findViewById(R.id.status_text);
        serialText = findViewById(R.id.serial_text);
        lastReadText = findViewById(R.id.last_call_text);
        currentReadInput = findViewById(R.id.current_call_input);
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

        // Observe changes to the current read input
        viewModel.getCurrentReadInput().observe(this, input -> {
            if (input != null && !input.equals(currentReadInput.getText().toString())) {
                isUpdatingInput = true;
                currentReadInput.setText(input);
                currentReadInput.setSelection(input.length());
                isUpdatingInput = false;
            }
        });

        // Update input field when selected read changes
        viewModel.getSelectedRead().observe(this, read -> {
            if (read != null) {
                String currentValue = String.valueOf(read.getCurrent_read());
                if (!currentValue.equals(currentReadInput.getText().toString())) {
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
        viewModel.loadReadsForBuilding(getIntent().getIntExtra("building_center", -1));

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
        viewModel.getReads().observe(this, reads -> {
            ReadingAdapter readingAdapter = new ReadingAdapter(reads, this, viewModel);
            lstReadings.setAdapter(readingAdapter);
            lstReadings.setLayoutManager(new LinearLayoutManager(this));
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
        startActivity(intent);
    }
}