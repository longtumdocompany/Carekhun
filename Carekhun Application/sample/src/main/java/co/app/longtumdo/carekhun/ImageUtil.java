package co.app.longtumdo.carekhun;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;

/**
 * Created by TOPPEE on 9/11/2017.
 */

public class ImageUtil {
    public static Bitmap resizeBmp(Bitmap orgBmp, int newWidth, int newHeight) {

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                orgBmp, newWidth, newHeight, false);
        return resizedBitmap;
    }

    public static String encodeByteTobase64(byte[] bytes) {
        String imageEncoded = Base64.encodeToString(bytes, Base64.DEFAULT);
        return imageEncoded;
    }

    public static String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 90, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static Bitmap convertToBitmapImage(String base64Str) throws IllegalArgumentException
    {
        byte[] decodedBytes = Base64.decode(
                base64Str.substring(base64Str.indexOf(",")  + 1),
                Base64.DEFAULT
        );

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static String convertToString(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    public static String encodeURL(String url) {
        String query =  null;
        try {
            query = URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.d("ImageUtil", e.getMessage());
        }
        return query;
    }

    public static int calculatedInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image.
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the reqeuested height and width.
            while ((halfHeight / inSampleSize ) > reqHeight
                    && (halfWidth / inSampleSize ) > reqWidth ) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodedSampledBitmapFromData(byte[] data, int offset, int reqLength, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, offset, reqLength, options);

        // Calculate inSampleSize.
        options.inSampleSize = calculatedInSampleSize(options, reqWidth, reqHeight);
        //Log.i("decodedSampledBitmapFromResource", "options.inSampleSize : " + options.inSampleSize);
        // Decode bitmap width inSampleSize set.
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, offset, reqLength, options);


    }

    public static byte[] bitmapToByteArray(Bitmap bm) {
        int bytes = bm.getWidth() * bm.getHeight() * 4;
        ByteBuffer buffer = ByteBuffer.allocate(bytes);

        bm.copyPixelsToBuffer(buffer);
        return buffer.array();
    }


    public static Bitmap openJpegFile(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        return bitmap;
    }

    public static byte[] jpegToByteArray(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayBitmapStream);
        byte[] imageBytes = byteArrayBitmapStream.toByteArray();

        return imageBytes;
    }

    public static String saveBitmapToFile(Bitmap bitmap, String filename) {
        File pictureFile = new File(filename);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(pictureFile);
            fos.write(byteArray);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("saveFileFromImageView", "IOException - " + e.getMessage());
            return "";
        }

        Log.i("saveBitmapToFile", "filename = " + filename);
        return filename;
    }

    public static String saveBitmapDpiToFile(Bitmap bitmap, String filename, int dpi) {
        File pictureFile = new File(filename);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        //setDpi(byteArray, dpi);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(pictureFile);
            fos.write(byteArray);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("SaveImageWithDPI", "IOException - " + e.getMessage());
            return "";
        }
        Log.i("SaveImageWithDPI", "filename = " + filename);
        return filename;
    }

    private static void setDpi(byte[] imageData, int dpi) {         //300 dpi
        imageData[13] = 1;
        imageData[14] = (byte)(dpi >> 8);
        imageData[15] = (byte)(dpi & 0xff);
        imageData[16] = (byte)(dpi >> 8);
        imageData[17] = (byte)(dpi & 0xff);
    }

    public Bitmap drawMultilineTextToBitmap(Context gContext, int gResId, String gText) {
        Resources resources = gContext.getResources();                      // prepare canvas
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);
        Bitmap.Config bitmapConfig = bitmap.getConfig();
        if(bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);                       // resource bitmaps are imutable,
        Canvas canvas = new Canvas(bitmap);
        TextPaint paint=new TextPaint(Paint.ANTI_ALIAS_FLAG);           // new antialiased Paint
        paint.setColor(Color.rgb(61, 61, 61));                          // text color - #3D3D3D
        paint.setTextSize((int) (14 * scale));                          // text size in pixels
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);                  // text shadow
        int textWidth = canvas.getWidth() - (int) (16 * scale);         // set text width to canvas width minus 16dp padding

        // init StaticLayout for text
        StaticLayout textLayout = new StaticLayout(gText, paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        int textHeight = textLayout.getHeight();                        // get height of multiline text

        float x = (bitmap.getWidth() - textWidth)/2;                    // get position of text's top left corner
        float y = (bitmap.getHeight() - textHeight)/2;

        canvas.save();
        canvas.translate(x, y);
        textLayout.draw(canvas);
        canvas.restore();

        return bitmap;
    }
}
