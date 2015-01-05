package codetail.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.TypedValue;

public class ThemeUtils {

    public static Drawable getDrawableWithTint(@DrawableRes int drawable, int color){
        Resources r = ResourceUtils.sResources;
        Drawable d = r.getDrawable(drawable);
        d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return d;
    }

    /**
     * @param context Current Activity or Application context
     * @param attr attribute
     *
     * @return theme color
     */
    public static int getThemeColor(Context context, int attr){
        int resourceId = getThemeResourceId(context.getTheme(), attr);
        return context.getResources().getColor(resourceId);
    }

    /**
     * @param context Current Activity or Application context
     * @param attr attribute
     *
     * @return theme color
     */
    public static ColorStateList getThemeColorStateList(Context context, int attr){
        int resourceId = getThemeResourceId(context.getTheme(), attr);
        return context.getResources().getColorStateList(resourceId);
    }

    /**
     * @param theme Current theme
     * @param attr attribute
     *
     * @return resourceId
     */
    public static int getThemeResourceId(Resources.Theme theme, int attr){
        TypedValue value = new TypedValue();
        theme.resolveAttribute(attr, value, true);
        return value.resourceId;
    }

}
