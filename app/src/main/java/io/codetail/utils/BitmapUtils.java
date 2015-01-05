package io.codetail.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

public class BitmapUtils {

    public static Transformation sCircleTransformation = new Transformation() {
        @Override
        public Bitmap transform(Bitmap source) {
            return makeCircleBitmap(source);
        }

        @Override
        public String key() {
            return "makeCircle()";
        }
    };


    public static Bitmap makeCircleBitmap(Bitmap original){
        final int width = original.getWidth();
        final int height = original.getHeight();
        final float radius = Math.min(width, height) / 2;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(original, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(radius, radius, radius, paint);

        original.recycle();
        return bitmap;
    }
}
