package codetail.graphics.drawables;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.support.v4.util.LruCache;

public class DrawableHelper {

    final static boolean COMPAT = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    final static ColorFilterLruCache COLOR_FILTER_CACHE = new ColorFilterLruCache(6);

    /**
     * Parses a {@link android.graphics.PorterDuff.Mode} from a tintMode
     * attribute's enum value.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
        switch (value) {
            case 3: return PorterDuff.Mode.SRC_OVER;
            case 5: return PorterDuff.Mode.SRC_IN;
            case 9: return PorterDuff.Mode.SRC_ATOP;
            case 14: return PorterDuff.Mode.MULTIPLY;
            case 15: return PorterDuff.Mode.SCREEN;
            case 16: return COMPAT ? defaultMode : PorterDuff.Mode.ADD;
            default: return defaultMode;
        }
    }

    public static void setTint(Drawable drawable, int color){
        if(Build.VERSION.SDK_INT >= 21){
            drawable.setTint(color);
        }else{
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    /**
     * Ensures the tint filter is consistent with the current tint color and
     * mode.
     */
    private static PorterDuffColorFilter updateTintFilter(ColorStateList tint,
                                                          PorterDuff.Mode tintMode,
                                                          int[] drawableState) {

        if (tint == null || tintMode == null) {
            return null;
        }

        final int color = tint.getColorForState(drawableState, Color.TRANSPARENT);
        PorterDuffColorFilter filter = COLOR_FILTER_CACHE.get(color, tintMode);

        if(filter == null) {
            filter = new PorterDuffColorFilter(color, tintMode);
            COLOR_FILTER_CACHE.put(color, tintMode, filter);
        }

        return filter;
    }

    static void updateTintFilter(Drawable who,
                                 ColorStateList tint,
                                 PorterDuff.Mode tintMode,
                                 int[] drawableState){

        PorterDuffColorFilter filter = updateTintFilter(tint, tintMode, drawableState);
        who.setColorFilter(filter);
    }

    public static class DrawableCompat extends InsetDrawable{

        Drawable mWrapped;

        ColorStateList mTintList;
        PorterDuff.Mode mTintMode = PorterDuff.Mode.SRC_ATOP;

        public static DrawableCompat wrap(Drawable drawable){
            return new DrawableCompat(drawable);
        }

        private DrawableCompat(Drawable drawable) {
            super(drawable, 0);
            mWrapped = drawable;
        }

        @Override
        public boolean setState(int[] stateSet) {
            if(COMPAT){
                updateTintFilter(this, mTintList, mTintMode, stateSet);
            }
            return super.setState(stateSet);
        }

        @Override
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void setTintList(ColorStateList tint) {
            if(COMPAT){
                mTintList = tint;
                updateTintFilter(this, mTintList, mTintMode, getState());
                return;
            }

            super.setTintList(tint);
        }

        @Override
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void setTintMode(PorterDuff.Mode tintMode) {
            if(COMPAT){
                mTintMode = tintMode;
                updateTintFilter(this, mTintList, mTintMode, getState());
                return;
            }

            super.setTintMode(tintMode);
        }

        @Override
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void setTint(int tint) {
            if(COMPAT){
                setTintList(ColorStateList.valueOf(tint));
                return;
            }

            super.setTint(tint);
        }

        @Override
        public Drawable getDrawable() {
            return mWrapped;
        }

        @Override
        public DrawableCompat mutate() {
            return (DrawableCompat) super.mutate();
        }
    }


    private static class ColorFilterLruCache extends LruCache<Integer, PorterDuffColorFilter> {

        public ColorFilterLruCache(int maxSize) {
            super(maxSize);
        }

        PorterDuffColorFilter get(int color, PorterDuff.Mode mode) {
            return get(generateCacheKey(color, mode));
        }

        PorterDuffColorFilter put(int color, PorterDuff.Mode mode, PorterDuffColorFilter filter) {
            return put(generateCacheKey(color, mode), filter);
        }

        private static int generateCacheKey(int color, PorterDuff.Mode mode) {
            int hashCode = 1;
            hashCode = 31 * hashCode + color;
            hashCode = 31 * hashCode + mode.hashCode();
            return hashCode;
        }
    }
}
