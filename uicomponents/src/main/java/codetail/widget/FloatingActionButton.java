package codetail.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import codetail.components.R;
import codetail.graphics.drawables.RippleDrawable;
import codetail.graphics.drawables.TouchTracker;
import codetail.utils.ViewUtils;

import static codetail.utils.ViewUtils.isVisible;

public class FloatingActionButton extends View{

    RippleDrawable mRippleDrawable;
    Drawable mActionIcon;
    final int mActionSize;

    final int mInnerCircleOffset;
    final Circle mCircle;
    final Path mCirclePath;
    final Paint mCirclePaint;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public FloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true);
        setClickable(true);

        if(Build.VERSION.SDK_INT > 14 && Build.VERSION.SDK_INT < 16){
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        Resources r = context.getResources();

        mInnerCircleOffset = r.getDimensionPixelSize(R.dimen.fab_inner_circle_offset);
        mActionSize = r.getDimensionPixelSize(R.dimen.fab_action_icon_size);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.FILL);

        // Resolving attribute styles
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton);

        mCirclePaint.setColor(array.getColor(R.styleable.FloatingActionButton_actionColor, Color.WHITE));
        mActionIcon = array.getDrawable(R.styleable.FloatingActionButton_actionIcon);

        if(mActionIcon != null){
            mActionIcon.setBounds(0, 0, mActionSize, mActionSize);
        }

        array.recycle();

        mCircle = new Circle();
        mCirclePath = new Path();

        ViewUtils.setBackground(this, r.getDrawable(R.drawable.floatingactionbutton_shadow_layer));

        mRippleDrawable = new RippleDrawable(r.getColorStateList(R.color.overlay_color), null);
        mRippleDrawable.setVisible(true, false);
        mRippleDrawable.setCallback(this);
        mRippleDrawable.setState(getDrawableState());

        setOnTouchListener(new TouchTracker());
    }

    public void setActionIcon(Drawable drawable){
        mActionIcon = drawable;
        mActionIcon.setBounds(0, 0, mActionSize, mActionSize);
        invalidate();
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mRippleDrawable || who == mActionIcon || super.verifyDrawable(who);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        mRippleDrawable.setState(getDrawableState());

        if(mActionIcon != null && mActionIcon.isStateful()){
            mActionIcon.setState(getDrawableState());
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();

        mRippleDrawable.jumpToCurrentState();

        if(mActionIcon != null){
            mActionIcon.jumpToCurrentState();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        final int size = Math.min(right - left, bottom - top) - (mInnerCircleOffset * 2);

        mCircle.radius = size / 2;
        mCircle.x = mCircle.y = mInnerCircleOffset + mCircle.radius;

        mRippleDrawable.setBounds(mInnerCircleOffset, mInnerCircleOffset,
                mInnerCircleOffset + size, mInnerCircleOffset + size);

        mCirclePath.reset();
        mCirclePath.addCircle(mCircle.x, mCircle.y, mCircle.radius, Path.Direction.CW);
    }

    public void setColor(int color){
        mCirclePaint.setColor(color);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int state = canvas.save();

        canvas.clipPath(mCirclePath);
        canvas.drawPath(mCirclePath, mCirclePaint);

        mRippleDrawable.draw(canvas);

        if(mActionIcon != null){
            final int beforeActionState = canvas.save();

            canvas.translate( (mCircle.x - (mActionSize / 2)), (mCircle.y - (mActionSize / 2)));
            mActionIcon.draw(canvas);

            canvas.restoreToCount(beforeActionState);
        }

        canvas.restoreToCount(state);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mRippleDrawable.setVisible(isVisible(this), false);

        if(mActionIcon != null){
            mActionIcon.setVisible(isVisible(this), false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mRippleDrawable.setVisible(false, false);

        if(mActionIcon != null){
            mActionIcon.setVisible(false, false);
        }
    }

    private static class Circle{
        float x, y, radius;
    }
}
