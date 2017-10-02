package co.app.longtumdo.carekhun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.zxing.common.BitMatrix;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Suttipong.k on 3/30/2017.
 */

public class QRCodeUtil {

    private static final MatrixToImageConfig DEFAULT_CONFIG = new MatrixToImageConfig();

    private QRCodeUtil() {
    }

    public static Bitmap toBitmap(BitMatrix matrix) {
        return toBitmap(matrix, DEFAULT_CONFIG);
    }

    public static Bitmap toBitmap(BitMatrix matrix, MatrixToImageConfig config) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(image);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Paint paint = new Paint();
                if (matrix.get(x, y)) {
                    paint.setColor(Color.BLACK);
                } else {
                    paint.setColor(Color.WHITE);
                }
                canvas.drawPoint(x, y, paint);
            }
        }

        canvas.drawBitmap(image, 0, 0, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        return image;
    }

    public static void writeToFile(BitMatrix matrix, String format, String file) throws IOException {
        writeToFile(matrix, format, file, DEFAULT_CONFIG);
    }

    public static void writeToFile(BitMatrix matrix, String format, String file, MatrixToImageConfig config) throws IOException {
        Bitmap image = toBitmap(matrix, config);
        if (!write(image, format, file)) {
            throw new IOException("Could not write an image of format " + format + " to " + file);
        }
    }

    public static void writeToStream(BitMatrix matrix, String format, OutputStream stream) throws IOException {
        writeToStream(matrix, format, stream, DEFAULT_CONFIG);
    }

    public static void writeToStream(BitMatrix matrix, String format, OutputStream stream, MatrixToImageConfig config) throws IOException {
        Bitmap image = toBitmap(matrix, config);
        if (!write(image, format, stream)) {
            throw new IOException("Could not write an image of format " + format);
        }
    }

    public static boolean write(Bitmap image, String type, OutputStream stream) {
        return image.compress(Bitmap.CompressFormat.JPEG, 80, stream);
    }

    public static boolean write(Bitmap image, String type, String file) throws IOException {
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            return write(image, type, stream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                stream.flush();
                stream.close();
            }
        }
        return false;
    }

}
