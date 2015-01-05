package codetail.utils;

import android.graphics.Color;


public class ColorUtils {

    private static boolean isLight(int color) {
        return Math.sqrt(
                Color.red(color) * Color.red(color) * .241 +
                        Color.green(color) * Color.green(color) * .691 +
                        Color.blue(color) * Color.blue(color) * .068) > 130;
    }

    public static int getBaseColor(int color) {
        if (isLight(color)) {
            return Color.BLACK;
        }
        return Color.WHITE;
    }
}
