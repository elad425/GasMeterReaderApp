package com.example.gasmeterreader.utils;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Canvas;
import android.graphics.Color;


public class BitmapUtils {
    private static final int IMAGE_SIZE = 640;

    public static Bitmap cropBitmap(Bitmap original, RectF boundingBox) {
        int left = Math.max(0, Math.round(boundingBox.left));
        int top = Math.max(0, Math.round(boundingBox.top));
        int width = Math.min(original.getWidth() - left, Math.round(boundingBox.width()));
        int height = Math.min(original.getHeight() - top, Math.round(boundingBox.height()));

        return placeOnGrayCanvas(Bitmap.createBitmap(original, left, top, width, height));
    }

    public static Bitmap placeOnGrayCanvas(Bitmap originalBitmap) {

        int canvasWidth = IMAGE_SIZE;
        int canvasHeight = IMAGE_SIZE;

        Bitmap canvasBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(canvasBitmap);
        canvas.drawColor(Color.GRAY);

        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();
        float scale = Math.min((float) canvasWidth / originalWidth,
                (float) canvasHeight / originalHeight);
        int newWidth = Math.round(originalWidth * scale);
        int newHeight = Math.round(originalHeight * scale);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight,
                true);

        int left = (canvasWidth - newWidth) / 2;
        int top = (canvasHeight - newHeight) / 2;

        canvas.drawBitmap(resizedBitmap, left, top, null);
        return canvasBitmap;
    }

    public static Bitmap convertToGrayscale(Bitmap original) {
        Bitmap grayscale = Bitmap.createBitmap(original.getWidth(), original.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(grayscale);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(original, 0, 0, paint);
        return grayscale;
    }

    public static RectF mapToOriginalImage(RectF rectF, int originalWidth, int originalHeight) {
        float scale = Math.min((float) IMAGE_SIZE / originalWidth,
                (float) IMAGE_SIZE / originalHeight);

        float newWidth = originalWidth * scale;
        float newHeight = originalHeight * scale;
        float leftPadding = (IMAGE_SIZE - newWidth) / 2.0f;
        float topPadding = (IMAGE_SIZE - newHeight) / 2.0f;
        float x1 = (rectF.left - leftPadding) / scale;
        float y1 = (rectF.top - topPadding) / scale;
        float x2 = (rectF.right - leftPadding) / scale;
        float y2 = (rectF.bottom - topPadding) / scale;

        return new RectF(x1, y1, x2, y2);
    }

}





