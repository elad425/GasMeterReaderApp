package com.example.gasmeterreader.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.graphics.Canvas;
import android.graphics.Color;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BitmapUtils {

    public static Bitmap cropBitmap(Bitmap original, RectF boundingBox,Boolean isDigit) {
        int left = Math.max(0, Math.round(boundingBox.left));
        int top = Math.max(0, Math.round(boundingBox.top));
        int width = Math.min(original.getWidth() - left, Math.round(boundingBox.width()));
        int height = Math.min(original.getHeight() - top, Math.round(boundingBox.height()));

        if (width <= 0 || height <= 0 || left >= original.getWidth() || top >= original.getHeight()) {
            return null;
        }

        try {
            Bitmap bitmap = Bitmap.createBitmap(original, left, top, width, height);
            if (isDigit) {
                return Bitmap.createScaledBitmap(bitmap, 320, 320, true);
            } else {
                return Bitmap.createScaledBitmap(bitmap, 1000, 500, true);

            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void saveBitmapToGallery(Bitmap bitmap, Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "check" + "_" + timeStamp + "_" + ".jpg";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GasMeterDebug");

        try {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (uri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                    assert outputStream != null;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static Bitmap addPaddingToBitmap(Bitmap originalBitmap) {
        // Desired dimensions
        int newWidth = 1000;
        int newHeight = 3000;

        // Create a new bitmap with the desired size
        Bitmap paddedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        // Create a canvas to draw on the new bitmap
        Canvas canvas = new Canvas(paddedBitmap);

        canvas.drawColor(Color.GRAY);

        // Calculate the top position to center the original bitmap vertically
        int height = (newHeight - originalBitmap.getHeight()) / 2;
        int width = (newWidth - originalBitmap.getWidth()) / 2;

        // Draw the original bitmap onto the canvas
        canvas.drawBitmap(originalBitmap, width, height, null);

        // Return the new padded bitmap
        return paddedBitmap;
    }

    public static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        assert input != null;
        ExifInterface ei = new ExifInterface(input);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }


    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    public static Bitmap toGrayscale(Bitmap originalBitmap) {
        if(originalBitmap == null){
            return null;
        }
        Bitmap grayscaleBitmap = Bitmap.createBitmap(
                originalBitmap.getWidth(),
                originalBitmap.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        // Iterate through each pixel and convert it to grayscale
        for (int x = 0; x < originalBitmap.getWidth(); x++) {
            for (int y = 0; y < originalBitmap.getHeight(); y++) {
                // Get the color of the current pixel
                int pixelColor = originalBitmap.getPixel(x, y);

                // Extract the RGB components
                int red = Color.red(pixelColor);
                int green = Color.green(pixelColor);
                int blue = Color.blue(pixelColor);

                // Calculate the grayscale value using the luminance formula
                int gray = (int) (0.3 * red + 0.59 * green + 0.11 * blue);

                // Set the new grayscale color to the pixel
                int newColor = Color.rgb(gray, gray, gray);
                grayscaleBitmap.setPixel(x, y, newColor);
            }
        }

        return grayscaleBitmap;
    }


}





