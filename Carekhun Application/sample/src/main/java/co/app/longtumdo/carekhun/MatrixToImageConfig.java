package co.app.longtumdo.carekhun;

/**
 * Created by Suttipong.k on 3/30/2017.
 */

import android.graphics.Bitmap;

public class MatrixToImageConfig {

    public static final int BLACK = 0xFF000000;
    public static final int WHITE = 0xFFFFFFFF;

    private final int onColor;
    private final int offColor;

    public MatrixToImageConfig() {
        this(BLACK, WHITE);
    }

    public MatrixToImageConfig(int onColor, int offColor) {
        this.onColor = onColor;
        this.offColor = offColor;
    }

    public int getPixelOnColor() {
        return onColor;
    }

    public int getPixelOffColor() {
        return offColor;
    }

    Bitmap.Config getBufferedImageColorModel() {
        return onColor == BLACK && offColor == WHITE ? Bitmap.Config.ALPHA_8 : Bitmap.Config.ARGB_8888;
    }
}
