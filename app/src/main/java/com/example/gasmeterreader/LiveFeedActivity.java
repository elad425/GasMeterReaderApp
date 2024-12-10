package com.example.gasmeterreader;

import android.os.Bundle;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gasmeterreader.ml.LiveStreamDetection;

public class LiveFeedActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ImageButton flashButton;
    private TextView idResultText;
    private TextView dataResultText;
    private ImageView detectionStatusIcon;
    private LiveStreamDetection cameraLiveFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_feed);

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));

        previewView = findViewById(R.id.viewFinder);
        flashButton = findViewById(R.id.flashButton);
        idResultText = findViewById(R.id.idResultText);
        dataResultText = findViewById(R.id.dataResultText);
        detectionStatusIcon = findViewById(R.id.detectionStatusIcon);

        startCamera();
    }

    private void startCamera() {
        this.cameraLiveFeed = new LiveStreamDetection(this, previewView,
                flashButton, idResultText, dataResultText, detectionStatusIcon);
        cameraLiveFeed.startCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.cameraLiveFeed.close();
    }
}