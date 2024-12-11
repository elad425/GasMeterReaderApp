package com.example.gasmeterreader;

import static com.example.gasmeterreader.utils.BitmapUtils.rotateImageIfRequired;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gasmeterreader.ml.ImageAnalyzer;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private static final int CAMERA_CAPTURE_REQUEST_CODE = 101;
    private static final int GALLERY_PICK_REQUEST_CODE = 102;
    private Uri photoUri;
    private TextView idResult;
    private TextView dataResult;
    private ImageAnalyzer imageAnalyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));

        this.imageAnalyzer = new ImageAnalyzer(this);

        this.idResult = findViewById(R.id.idResultText);
        this.dataResult = findViewById(R.id.dataResultText);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this,
                                "Camera permission is required to use this feature",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        Button liveFeedButton = findViewById(R.id.liveFeedButton);
        liveFeedButton.setOnClickListener(v -> checkAndRequestCameraPermission(true));

        Button captureButton = findViewById(R.id.camera);
        captureButton.setOnClickListener(v -> checkAndRequestCameraPermission(false));

        Button galleryButton = findViewById(R.id.gallery);
        galleryButton.setOnClickListener(v -> openGallery());

    }

    private void checkAndRequestCameraPermission(Boolean isLive) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if (isLive) {
                startLiveFeedActivity();
            } else {
                openCamera();
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startLiveFeedActivity() {
        Intent intent = new Intent(this, LiveFeedActivity.class);
        startActivity(intent);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = new File(getExternalFilesDir(null), "captured_image.jpg");
        photoUri = FileProvider.getUriForFile(this,
                "com.example.gasmeterreader.fileprovider", photoFile);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(cameraIntent, CAMERA_CAPTURE_REQUEST_CODE);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_PICK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap imageBitmap = null;
            if (requestCode == CAMERA_CAPTURE_REQUEST_CODE) {
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    imageBitmap = rotateImageIfRequired(this,imageBitmap, photoUri);
                } catch (IOException ignored) {}
            } else if (requestCode == GALLERY_PICK_REQUEST_CODE) {
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    try {
                        imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                                selectedImageUri);
                        imageBitmap = rotateImageIfRequired(this,imageBitmap, selectedImageUri);
                    } catch (IOException ignored) {}
                }
            }

            if (imageBitmap != null) {
                try {
                    this.imageAnalyzer.detect(imageBitmap);
                } finally {
                    File photoFile = new File(getExternalFilesDir(null),
                            "captured_image.jpg");
                    if (photoFile.exists()) {
                        photoFile.delete();
                    }
                }
                update();
            }
        }
    }

    public void update(){
        this.dataResult.setText(String.format("data: %s", imageAnalyzer.getData()));
        this.idResult.setText(String.format("serial: %s", imageAnalyzer.getId()));
    }

}
