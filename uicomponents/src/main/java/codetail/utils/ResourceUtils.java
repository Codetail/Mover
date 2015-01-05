package codetail.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class ResourceUtils {

    static float sDensity;
    static Resources sResources;

    /**
     *
     * @param context The application context to initialize utilities
     */
    public static void init(Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        sDensity = metrics.density;
        sResources = resources;
    }

    public static void release(){
        sResources = null;
    }

    /**
     * Convert dimens to pixel size
     *
     * @param dp dimen size
     * @return pixel size
     */
    public static int dp(float dp){
        return (int) (sDensity * dp);
    }

    /**
     * Convert to pixel size of given dimen in current density
     *
     * @param context Context to get display metrics
     * @param dp dimen size
     * @return pixel size of given dimen in current density
     */
    public static int dp2px(Context context, float dp) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return (int) px;
    }

    public static Resources getResources(){
        return sResources;
    }

    public static String getString(int resId, Object... args){
        return sResources.getString(resId, args);
    }

    public static String[] getStringArray(@ArrayRes int id){
        return sResources.getStringArray(id);
    }

    public static CharSequence[] getTextArray(@ArrayRes int id){
        return sResources.getTextArray(id);
    }

    /**
     * The logical density of the display.  This is a scaling factor for the
     * Density Independent Pixel unit, where one DIP is one pixel on an
     * approximately 160 dpi screen (for example a 240x320, 1.5"x2" screen),
     * providing the baseline of the system's display. Thus on a 160dpi screen
     * this density value will be 1; on a 120 dpi screen it would be .75; etc.
     *
     * <p>This value does not exactly follow the real screen size (as given by
     * {@link android.util.DisplayMetrics#xdpi} and {@link android.util.DisplayMetrics#ydpi}, but rather is used to scale the size of
     * the overall UI in steps based on gross changes in the display dpi.  For
     * example, a 240x320 screen will have a density of 1 even if its width is
     * 1.8", 1.3", etc. However, if the screen resolution is increased to
     * 320x480 but the screen size remained 1.5"x2" then the density would be
     * increased (probably to 1.5).
     */
    public static float getDensity(){
        return sDensity;
    }

    /**
     * @param id Color resource id
     *
     * @return found {@link android.content.res.ColorStateList}
     */
    public static ColorStateList getColorList(@ColorRes int id){
        return sResources.getColorStateList(id);
    }

    /**
     * @param id Color resource id
     *
     * @return found color in hex
     */
    public static int getColor(@ColorRes int id){
        return sResources.getColor(id);
    }

    /**
     * @param id Drawable resource id
     *
     * @return found {@link android.graphics.drawable.Drawable}
     */
    public static Drawable getDrawable(@DrawableRes int id){
        return sResources.getDrawable(id);
    }

    /**
     * @param id Drawable resource id
     *
     * @return found {@link android.graphics.drawable.Drawable}
     */
    public static Drawable getDrawable(@DrawableRes int id, Resources.Theme theme){
        return ResourcesCompat.getDrawable(sResources, id, theme);
    }

    /**
     * @param id Dimen item id
     *
     * @return dimension pixel size of resource
     */
    public static int getPixelSize(@DimenRes int id){
        return sResources.getDimensionPixelOffset(id);
    }

    public static class SupportResources extends Resources{

        public SupportResources(Resources resources) {
            super(resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration());
        }

    }
}
