package com.example.gasmeterreader.ml;

import static com.example.gasmeterreader.ml.FloatStringProcessor.fixData;
import static com.example.gasmeterreader.utils.BitmapUtils.convertToGrayscale;
import static com.example.gasmeterreader.utils.BitmapUtils.cropBitmap;
import static com.example.gasmeterreader.utils.BitmapUtils.mapToOriginalImage;
import static com.example.gasmeterreader.utils.BitmapUtils.placeOnGrayCanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.example.gasmeterreader.entities.Read;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ImageAnalyzer {
    private Pair<String, Integer> data;
    private Detector boxDetector;
    private Detector digitsDetectorData;
    private Bitmap originalBitmap;
    private Bitmap greyBackImage;
    private Read read;
    private int errorCount = 0;
    private final Context context;

    public ImageAnalyzer(Context context){
        this.context = context;
    }

    public void initializeDetectors() {
        Detector.DetectorListener boxListener = new Detector.DetectorListener() {
            @Override
            public void onEmptyDetect() {
                deleteDataDetect();
            }

            @Override
            public void onDetect(@NonNull List<BoundingBox> boundingBoxes, long inferenceTime) {
                cropOriginalBitmap(boundingBoxes);
            }
        };

        Detector.DetectorListener dataDigitsListener = new Detector.DetectorListener() {
            @Override
            public void onEmptyDetect() {
                deleteDataDetect();
            }

            @Override
            public void onDetect(@NonNull List<BoundingBox> boundingBoxes, long inferenceTime) {
                createStringFromDetection(boundingBoxes, "data");
            }
        };

        boxDetector = new Detector(context,"boxDetection.tflite",
                Arrays.asList("data", "id"), boxListener);
        digitsDetectorData = new Detector(context, "digitsDetectionData.tflite",
                Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "dot"), dataDigitsListener);
    }

    public void deleteDataDetect(){
        data = new Pair<>("None",0);
    }

    public void detect(Bitmap bitmap) {
        if (boxDetector == null || digitsDetectorData == null) return;
        originalBitmap = convertToGrayscale(bitmap);
        greyBackImage = placeOnGrayCanvas(originalBitmap);
        boxDetector.detect(greyBackImage);
    }

    public void setRead(Read read){
        this.read = read;
    }

    public void cropOriginalBitmap(List<BoundingBox> boundingBoxes){
        RectF bestDataBox = new RectF();
        float maxCondData = 0f;
        for (BoundingBox b : boundingBoxes){
            if(b.getClsName().equals("data")) {
                if (b.getCnf() > maxCondData){
                    maxCondData = b.getCnf();
                    bestDataBox = new RectF(b.getX1() * greyBackImage.getWidth(),
                            b.getY1() * greyBackImage.getHeight(),
                            b.getX2() * greyBackImage.getWidth(),
                            b.getY2()* greyBackImage.getHeight());
                }
            }
        }
        if (maxCondData != 0f) {
            Bitmap resultData = cropBitmap(originalBitmap,
                    mapToOriginalImage(bestDataBox, originalBitmap.getWidth(), originalBitmap.getHeight()));
            digitsDetectorData.detect(resultData);
        }
    }

    public void createStringFromDetection(List<BoundingBox> boundingBoxes, String type) {
        boundingBoxes.sort((b1, b2) -> Float.compare(b1.getX1(), b2.getX1()));
        StringBuilder classNames = new StringBuilder();
        for (BoundingBox box : boundingBoxes) {
            classNames.append(box.getClsName());
            if (type.equals("data") && read != null) {
                data = fixData(classNames.toString(),String.valueOf(read.getLast_read()));
                if (Objects.equals(data.second, 0)) {
                    errorCount+=1;
                }
            }
        }
    }

    public Pair<String, Integer> getData() {
        return data;
    }

    public int getErrorCount(){
        return errorCount;
    }

    public void resetError(){
        errorCount = 0;
    }

}
