package codetail.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import codetail.animation.RevealAnimator;


public class RevealFrameLayout extends FrameLayoutCompat implements RevealAnimator {

    Path mRevealPath;

    boolean mClipOutlines;

    float mCenterX;
    float mCenterY;
    float mRadius;

    View mTarget;

    public RevealFrameLayout(Context context) {
        this(context, null);
    }

    public RevealFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RevealFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if(Build.VERSION.SDK_INT > 14 && Build.VERSION.SDK_INT < 16){
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        mRevealPath = new Path();
    }

    /**
     * Animation target
     *
     * @hide
     */
    @Override
    public void setTarget(View view){
        mTarget = view;
    }

    /**
     * Epicenter of animation circle reveal
     *
     * @hide
     */
    @Override
    public void setCenter(float centerX, float centerY){
        mCenterX = centerX;
        mCenterY = centerY;
    }

    /**
     * Flag that animation is enabled
     *
     * @hide
     */
    @Override
    public void setClipOutlines(boolean clip){
        mClipOutlines = clip;
    }

    /**
     * Circle radius size
     *
     * @hide
     */
    @Override
    public void setRevealRadius(float radius){
        mRadius = radius;
        invalidate();
    }

    /**
     * Circle radius size
     *
     * @hide
     */
    @Override
    public float getRevealRadius(){
        return mRadius;
    }


    @Override
    protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
        if(!mClipOutlines && child != mTarget)
            return super.drawChild(canvas, child, drawingTime);

        final int state = canvas.save();

        mRevealPath.reset();
        mRevealPath.addCircle(mCenterX, mCenterY, mRadius, Path.Direction.CW);

        canvas.clipPath(mRevealPath);

        boolean isInvalided = super.drawChild(canvas, child, drawingTime);

        canvas.restoreToCount(state);

        return isInvalided;
    }

}
