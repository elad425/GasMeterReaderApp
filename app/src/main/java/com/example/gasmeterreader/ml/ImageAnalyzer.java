package com.example.gasmeterreader.ml;

import static com.example.gasmeterreader.utils.BitmapUtils.convertToGrayscale;
import static com.example.gasmeterreader.utils.BitmapUtils.cropBitmap;
import static com.example.gasmeterreader.utils.BitmapUtils.mapToOriginalImage;
import static com.example.gasmeterreader.utils.BitmapUtils.placeOnGrayCanvas;
import static com.example.gasmeterreader.utils.StringsUtils.fixData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class ImageAnalyzer {
    private String data = "";
    private final Detector boxDetector;
    private final Detector digitsDetectorData;
    private Bitmap originalBitmap;
    private Bitmap geryBackImage;

    public ImageAnalyzer(Context context){
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
        data = "";
    }

    public void detect(Bitmap bitmap){
        originalBitmap = convertToGrayscale(bitmap);
        geryBackImage = placeOnGrayCanvas(originalBitmap);
        boxDetector.detect(geryBackImage);
    }

    public void cropOriginalBitmap(List<BoundingBox> boundingBoxes){
        RectF bestDataBox = new RectF();
        float maxCondData = 0f;
        for (BoundingBox b : boundingBoxes){
            if(b.getClsName().equals("data")) {
                if (b.getCnf() > maxCondData){
                    maxCondData = b.getCnf();
                    bestDataBox = new RectF(b.getX1() * geryBackImage.getWidth(),
                            b.getY1() * geryBackImage.getHeight(),
                            b.getX2() * geryBackImage.getWidth(),
                            b.getY2()* geryBackImage.getHeight());
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
            if (type.equals("data")) {
                data = fixData(classNames.toString());
            }
        }
    }

    public String getData() {
        return data;
    }

    public void close(){
        boxDetector.close();
        digitsDetectorData.close();
    }
}
