package codetail.text;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Map;

import codetail.components.R;


public class RobotoTextView extends TextView{

    static Map<String, Typeface> sCachedFonts;

    static {
        sCachedFonts = new ArrayMap<>();
    }

    AssetManager mAssetsManager;

    public RobotoTextView(Context context) {
        this(context, null);
    }

    public RobotoTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RobotoTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mAssetsManager = context.getAssets();

        if(isInEditMode()){
            return;
        }

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RobotoTextView);
        String fontName = a.getString(R.styleable.RobotoTextView_fontFamily);
        if(!TextUtils.isEmpty(fontName)){
            setFontFamily(fontName);
        }else {
            setFontFamily("Roboto-Regular");
        }
        a.recycle();
    }

    public void setFontFamily(String name){
        Typeface font = Typeface.createFromAsset(mAssetsManager, "fonts/" + name + ".ttf");
        setTypeface(font);
    }

}
