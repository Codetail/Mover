package codetail.graphics.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;

import codetail.utils.ResourceUtils;

public class MenuDrawable extends Drawable {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean reverseAngle = false;
    private long lastFrameTime;
    private boolean animationInProgress;
    private float finalRotation;
    private float currentRotation;
    private int currentAnimationTime;
    private DecelerateInterpolator interpolator = new DecelerateInterpolator();
    
    Context context;

    public MenuDrawable(Context context, int color) {
        super();
        this.context = context;
        paint.setColor(color);
        paint.setStrokeWidth(ResourceUtils.dp2px(context, 2));
    }

    public boolean isHamburgerState(){
        return !reverseAngle;
    }

    public boolean isBackState(){
        return reverseAngle;
    }

    public void setRotation(float rotation, boolean animated) {
        lastFrameTime = 0;
        if (currentRotation == 1) {
            reverseAngle = true;
        } else if (currentRotation == 0) {
            reverseAngle = false;
        }
        lastFrameTime = 0;
        if (animated) {
            if (currentRotation < rotation) {
                currentAnimationTime = (int) (currentRotation * 300);
            } else {
                currentAnimationTime = (int) ((1.0f - currentRotation) * 300);
            }
            lastFrameTime = System.currentTimeMillis();
            finalRotation = rotation;
        } else {
            finalRotation = currentRotation = rotation;
        }
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (currentRotation != finalRotation) {
            if (lastFrameTime != 0) {
                long dt = System.currentTimeMillis() - lastFrameTime;

                currentAnimationTime += dt;
                if (currentAnimationTime >= 300) {
                    currentRotation = finalRotation;
                } else {
                    if (currentRotation < finalRotation) {
                        currentRotation = interpolator.getInterpolation(currentAnimationTime / 300.0f) * finalRotation;
                    } else {
                        currentRotation = 1.0f - interpolator.getInterpolation(currentAnimationTime / 300.0f);
                    }
                }
            }
            lastFrameTime = System.currentTimeMillis();
            invalidateSelf();
        }

        canvas.save();
        canvas.translate(getIntrinsicWidth() / 2, getIntrinsicHeight() / 2);
        canvas.rotate(currentRotation * (reverseAngle ? -180 : 180));
        canvas.drawLine(-ResourceUtils.dp2px(context, 9), 0, ResourceUtils.dp2px(context, 9) - ResourceUtils.dp2px(context, 1) * currentRotation, 0, paint);
        float endYDiff = ResourceUtils.dp2px(context, 5) * (1 - Math.abs(currentRotation)) - ResourceUtils.dp2px(context, 0.5f) * Math.abs(currentRotation);
        float endXDiff = ResourceUtils.dp2px(context, 9) - ResourceUtils.dp2px(context, 0.5f) *  Math.abs(currentRotation);
        float startYDiff = ResourceUtils.dp2px(context, 5) + ResourceUtils.dp2px(context, 3.5f) * Math.abs(currentRotation);
        float startXDiff = -ResourceUtils.dp2px(context, 9) + ResourceUtils.dp2px(context, 8.5f) * Math.abs(currentRotation);
        canvas.drawLine(startXDiff, -startYDiff, endXDiff, -endYDiff, paint);
        canvas.drawLine(startXDiff, startYDiff, endXDiff, endYDiff, paint);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public int getIntrinsicWidth() {
        return ResourceUtils.dp2px(context, 24);
    }

    @Override
    public int getIntrinsicHeight() {
        return ResourceUtils.dp2px(context, 24);
    }
}