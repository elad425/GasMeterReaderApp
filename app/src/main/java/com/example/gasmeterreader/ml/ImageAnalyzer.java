package com.example.gasmeterreader.ml;

import static com.example.gasmeterreader.utils.BitmapUtils.convertToGrayscale;
import static com.example.gasmeterreader.utils.BitmapUtils.cropBitmap;
import static com.example.gasmeterreader.utils.BitmapUtils.mapToOriginalImage;
import static com.example.gasmeterreader.utils.BitmapUtils.placeOnGrayCanvas;
import static com.example.gasmeterreader.utils.StringsUtils.fixID;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class ImageAnalyzer {
    private String data = "";
    private String id = "";
    private final Detector boxDetector;
    private final Detector digitsDetectorData;
    private final Detector digitsDetectorId;
    private Bitmap originalBitmap;
    private Bitmap geryBackImage;
    private Context context;

    public ImageAnalyzer(Context context){
        this.context = context;
        Detector.DetectorListener boxListener = new Detector.DetectorListener() {
            @Override
            public void onEmptyDetect() {
                deleteDataDetect();
                deleteIdDetect();
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

        Detector.DetectorListener idDigitsListener = new Detector.DetectorListener() {
            @Override
            public void onEmptyDetect() {
                deleteIdDetect();
            }

            @Override
            public void onDetect(@NonNull List<BoundingBox> boundingBoxes, long inferenceTime) {
                createStringFromDetection(boundingBoxes, "id");
            }
        };

        this.boxDetector = new Detector(context,"boxDetection.tflite",
                Arrays.asList("data", "id"), boxListener);
        this.digitsDetectorData = new Detector(context, "digitsDetectionData.tflite",
                Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "."), dataDigitsListener);
        this.digitsDetectorId = new Detector(context, "digitsDetectionId.tflite",
                Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "line"), idDigitsListener);

    }

    public void deleteIdDetect(){
        this.id = "";
    }

    public void deleteDataDetect(){
        this.data = "";
    }

    public void detect(Bitmap bitmap){
        this.originalBitmap = convertToGrayscale(bitmap);
        this.geryBackImage = placeOnGrayCanvas(this.originalBitmap);
        this.boxDetector.detect(this.geryBackImage);
    }

    public void cropOriginalBitmap(List<BoundingBox> boundingBoxes){
        RectF bestIdBox = new RectF();
        float maxCondId = 0f;
        RectF bestDataBox = new RectF();
        float maxCondData = 0f;
        for (BoundingBox b : boundingBoxes){
            if(b.getClsName().equals("id")) {
                if (b.getCnf() > maxCondId){
                    maxCondId = b.getCnf();
                    bestIdBox = new RectF(b.getX1() * this.geryBackImage.getWidth(),
                            b.getY1() * this.geryBackImage.getHeight(),
                            b.getX2() * this.geryBackImage.getWidth(),
                            b.getY2()* this.geryBackImage.getHeight());
                }
            }
            if(b.getClsName().equals("data")) {
                if (b.getCnf() > maxCondData){
                    maxCondData = b.getCnf();
                    bestDataBox = new RectF(b.getX1() * this.geryBackImage.getWidth(),
                            b.getY1() * this.geryBackImage.getHeight(),
                            b.getX2() * this.geryBackImage.getWidth(),
                            b.getY2()* this.geryBackImage.getHeight());
                }
            }
        }
        if (maxCondId != 0f) {
            Bitmap resultId = cropBitmap(originalBitmap,
                    mapToOriginalImage(bestIdBox, originalBitmap.getWidth(), originalBitmap.getHeight()));
            this.digitsDetectorId.detect(resultId);
        }
        if (maxCondData != 0f) {
            Bitmap resultData = cropBitmap(originalBitmap,
                    mapToOriginalImage(bestDataBox, originalBitmap.getWidth(), originalBitmap.getHeight()));
            this.digitsDetectorData.detect(resultData);
        }
    }

    public void createStringFromDetection(List<BoundingBox> boundingBoxes, String type) {
        boundingBoxes.sort((b1, b2) -> Float.compare(b1.getX1(), b2.getX1()));
        StringBuilder classNames = new StringBuilder();
        for (BoundingBox box : boundingBoxes) {
            classNames.append(box.getClsName());
            if (type.equals("data")) {
                this.data = classNames.toString();
            } else {
                this.id = fixID(classNames.toString());
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public void close(){
        boxDetector.close();
        digitsDetectorData.close();
        digitsDetectorId.close();
    }
}
