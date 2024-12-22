package com.example.gasmeterreader.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Objects;

public class ReadingActivity extends AppCompatActivity {
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ReadingViewModel viewModel;
    private TextView ownerText, apartmentText, statusText, serialText, lastReadText, readCounterText;
    private TextInputEditText currentReadInput;
    private boolean isUpdatingInput = false;
    private int buildingNumber;
    private ReadingAdapter readingAdapter;
    private ImageButton nextButton, backButton;
    private TextInputLayout searchCard;
    private ImageButton searchButton;
    private TextInputEditText searchInput;
    private boolean isSearchVisible = false;
    private boolean isSearchIconClose = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialWindow();
        initializeViews();
        setupViewModel();
        setupRecyclerView();
        setupCameraPermissionLauncher();
        setupLiveFeedButton();
        setupCurrentReadInput();
        setupNavigationButtons();
        setupSearch();
    }

    private void initialWindow(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES);
        View decorView = getWindow().getDecorView();
        int flags = decorView.getSystemUiVisibility();

        if (isDarkMode) {flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;}
        else {flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;}
        decorView.setSystemUiVisibility(flags);
        setContentView(R.layout.activity_reading);

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
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
        readCounterText = findViewById(R.id.read_counter);
        searchButton = findViewById(R.id.search_button);
        searchCard = findViewById(R.id.search_input_layout);
        searchInput = findViewById(R.id.search_input);
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

        currentReadInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_NEXT) {
                List<Read> currentReads = viewModel.getReads().getValue();
                Read currentSelectedRead = viewModel.getSelectedRead().getValue();

                if (currentReads != null && currentSelectedRead != null) {
                    int currentIndex = currentReads.indexOf(currentSelectedRead);

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

        viewModel.getCurrentReadInput().observe(this, input -> {
            if (input != null && !input.equals(Objects.requireNonNull(currentReadInput.getText()).toString())) {
                isUpdatingInput = true;
                currentReadInput.setText(input);
                currentReadInput.setSelection(input.length());
                isUpdatingInput = false;
            }
        });

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
            if(viewModel.getBuilding().isComplete()){
                Toast.makeText(this,
                        "כל הקריאות הושלמו",
                        Toast.LENGTH_SHORT).show();
            } else if (granted) {
                startLiveFeedActivity();
            }
        });
    }

    private void setupSearch() {
        searchButton.setOnClickListener(v -> {
            toggleSearchIcon();
            toggleSearch();
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                readingAdapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void toggleSearchIcon() {
        float startDegrees = isSearchIconClose ? 180f : 0f;
        float endDegrees = isSearchIconClose ? 360f : 180f;

        RotateAnimation rotateAnimation = new RotateAnimation(
                startDegrees, endDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateAnimation.setDuration(200);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        searchButton.startAnimation(rotateAnimation);
        searchButton.setImageResource(isSearchIconClose ?
                R.drawable.ic_search : R.drawable.ic_close);
        isSearchIconClose = !isSearchIconClose;
    }

    private void toggleSearch() {
        if (isSearchVisible) {
            searchCard.animate()
                    .alpha(0f)
                    .translationY(-searchCard.getHeight())
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        searchCard.setVisibility(View.GONE);
                        readCounterText.setVisibility(View.VISIBLE);
                        searchInput.setText("");
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                    })
                    .setDuration(200)
                    .start();
        } else {
            searchCard.setVisibility(View.VISIBLE);
            readCounterText.setVisibility(View.GONE);
            searchCard.setAlpha(0f);
            searchCard.setTranslationY(-searchCard.getHeight());
            searchCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        searchInput.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
                    })
                    .setDuration(200)
                    .start();
        }
        isSearchVisible = !isSearchVisible;
    }

    private void setupRecyclerView() {
        RecyclerView lstReadings = findViewById(R.id.read_lst);
        readingAdapter = new ReadingAdapter(null, viewModel, this);
        lstReadings.setAdapter(readingAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        lstReadings.setLayoutManager(layoutManager);

        viewModel.getReads().observe(this, reads -> {
            readingAdapter.updateReadings(reads);
            if (viewModel.getSelectedRead().getValue() == null && !reads.isEmpty()) {
                for (Read read : reads) {
                    if (!read.isRead()) {
                        viewModel.setSelectedRead(read);
                        break;
                    }
                }
                if (viewModel.getSelectedRead().getValue() == null) {
                    viewModel.setSelectedRead(reads.get(0));
                }
            }
        });

        viewModel.getSelectedRead().observe(this, selectedRead -> {
            if (selectedRead != null && readingAdapter != null) {
                readingAdapter.notifyDataSetChanged();
                List<Read> currentReads = viewModel.getReads().getValue();

                if (currentReads != null) {
                    int position = currentReads.indexOf(selectedRead);
                    readCounterText.setText(String.format("(%d/%d)", position + 1, currentReads.size()));
                    int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = layoutManager.getItemCount();
                    if (position < lastVisibleItemPosition - 1 || position > lastVisibleItemPosition) {
                        lstReadings.scrollToPosition(Math.min(position, totalItemCount - 1));
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
                                "דרוש הרשאות מצלמה",
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