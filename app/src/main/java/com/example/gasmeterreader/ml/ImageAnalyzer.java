package com.example.gasmeterreader.ml;

import static com.example.gasmeterreader.utils.BitmapUtils.addPaddingToBitmap;
import static com.example.gasmeterreader.utils.BitmapUtils.cropBitmap;
import static com.example.gasmeterreader.utils.BitmapUtils.saveBitmapToGallery;
import static com.example.gasmeterreader.utils.BitmapUtils.toGrayscale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class ImageAnalyzer {
    private final Context context;
    private String data;
    private String id;
    private final Detector boxDetector;
    private final Detector digitsDetectorData;
    private final Detector digitsDetectorId;
    private Bitmap originalBitmap;
    private Bitmap cropId;
    private Bitmap cropData;

    public ImageAnalyzer(Context context){
        this.context = context;
        this.data = "";
        this.id = "";

        Detector.DetectorListener boxListener = new Detector.DetectorListener() {
            @Override
            public void onEmptyDetect() {
            }

            @Override
            public void onDetect(@NonNull List<BoundingBox> boundingBoxes, long inferenceTime) {
                cropOriginalBitmap(boundingBoxes);
            }
        };

        Detector.DetectorListener dataDigitsListener = new Detector.DetectorListener() {
            @Override
            public void onEmptyDetect() {
            }

            @Override
            public void onDetect(@NonNull List<BoundingBox> boundingBoxes, long inferenceTime) {
                createStringFromDetection(boundingBoxes, "data");
            }
        };

        Detector.DetectorListener idDigitsListener = new Detector.DetectorListener() {
            @Override
            public void onEmptyDetect() {
            }

            @Override
            public void onDetect(@NonNull List<BoundingBox> boundingBoxes, long inferenceTime) {
                createStringFromDetection(boundingBoxes, "id");
            }
        };

        this.boxDetector = new Detector(context,"boxDetection.tflite",
                Arrays.asList("id", "data"), boxListener);
        this.digitsDetectorData = new Detector(context, "digitsDetectionData.tflite",
                Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "."), dataDigitsListener);
        this.digitsDetectorId = new Detector(context, "digitsDetectionId.tflite",
                Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), idDigitsListener);

    }

    public void detect(Bitmap bitmap){
        this.originalBitmap = bitmap;
        this.boxDetector.detect(this.originalBitmap);
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
                    bestIdBox = new RectF(b.getX1() * this.originalBitmap.getWidth(),
                            b.getY1() * this.originalBitmap.getHeight(),
                            b.getX2() * this.originalBitmap.getWidth(),
                            b.getY2()* this.originalBitmap.getHeight());
                }
            }
            if(b.getClsName().equals("data")) {
                if (b.getCnf() > maxCondData){
                    maxCondData = b.getCnf();
                    bestDataBox = new RectF(b.getX1() * this.originalBitmap.getWidth(),
                            b.getY1() * this.originalBitmap.getHeight(),
                            b.getX2() * this.originalBitmap.getWidth(),
                            b.getY2()* this.originalBitmap.getHeight());
                }
            }
        }
        if (maxCondId != 0f) {
            this.cropId = addPaddingToBitmap(toGrayscale(cropBitmap(originalBitmap, bestIdBox,false)));
            saveBitmapToGallery(cropId,this.context);
            this.digitsDetectorId.detect(cropId);
        }
        if (maxCondData != 0f) {
            this.cropData = addPaddingToBitmap(toGrayscale(cropBitmap(originalBitmap, bestDataBox,false)));
            saveBitmapToGallery(cropData,this.context);
            this.digitsDetectorData.detect(cropData);
        }
    }

    public void createStringFromDetection(List<BoundingBox> boundingBoxes, String type) {
        boundingBoxes.sort((b1, b2) -> Float.compare(b1.getX1(), b2.getX1()));
        StringBuilder classNames = new StringBuilder();
        Bitmap cropBitmap = type.equals("data") ? cropData : cropId;

        for (BoundingBox box : boundingBoxes) {
            classNames.append(box.getClsName());

            RectF digitRect = new RectF(
                    box.getX1() * cropBitmap.getWidth(),
                    box.getY1() * cropBitmap.getHeight(),
                    box.getX2() * cropBitmap.getWidth(),
                    box.getY2() * cropBitmap.getHeight()
            );

            Bitmap digitCrop = cropBitmap(cropBitmap, digitRect, true);
            if (type.equals("data")) {
                this.data = classNames.toString();
            } else {
                this.id = classNames.toString();
            }
            saveBitmapToGallery(digitCrop, this.context);
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
