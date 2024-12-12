package com.example.gasmeterreader;

import android.os.Bundle;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gasmeterreader.ml.LiveStreamDetection;

import org.checkerframework.common.subtyping.qual.Bottom;

public class LiveFeedActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ImageButton flashButton;
    private TextView idResultText;
    private TextView dataResultText;
    private ImageView detectionStatusIcon;
    private Button resetResult;

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
        resetResult = findViewById(R.id.reset);
        startCamera();
    }

    private void startCamera() {
        LiveStreamDetection cameraLiveFeed = new LiveStreamDetection(this, previewView,
                flashButton, idResultText, dataResultText, detectionStatusIcon,resetResult);
        cameraLiveFeed.startCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}