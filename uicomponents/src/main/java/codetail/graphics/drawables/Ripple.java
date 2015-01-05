package codetail.graphics.drawables;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Draws a Material ripple.
 */
class Ripple {
    private static final LinearInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final LogInterpolator DECEL_INTERPOLATOR = new LogInterpolator();

    private static final float GLOBAL_SPEED = 1.0f;
    private static final float WAVE_TOUCH_DOWN_ACCELERATION = 1024.0f * GLOBAL_SPEED;
    private static final float WAVE_TOUCH_UP_ACCELERATION = 3400.0f * GLOBAL_SPEED;
    private static final float WAVE_OPACITY_DECAY_VELOCITY = 3.0f / GLOBAL_SPEED;

    private static final long RIPPLE_ENTER_DELAY = 80;

    private final RippleDrawable mOwner;

    /** Bounds used for computing max radius. */
    private final Rect mBounds;

    /** Full-opacity color for drawing this ripple. */
    private int mColorOpaque;
    private int mColorAlpha;

    /** Maximum ripple radius. */
    private float mOuterRadius;

    /** Screen density used to adjust pixel-based velocities. */
    private float mDensity;

    private float mStartingX;
    private float mStartingY;
    private float mClampedStartingX;
    private float mClampedStartingY;

    // Software animators.
    private ObjectAnimator mAnimRadius;
    private ObjectAnimator mAnimOpacity;
    private ObjectAnimator mAnimX;
    private ObjectAnimator mAnimY;

    // Temporary paint used for creating canvas properties.
    private Paint mTempPaint;

    // Software rendering properties.
    private float mOpacity = 1;
    private float mOuterX;
    private float mOuterY;

    // Values used to tween between the start and end positions.
    private float mTweenRadius = 0;
    private float mTweenX = 0;
    private float mTweenY = 0;

    /** Whether we have an explicit maximum radius. */
    private boolean mHasMaxRadius;

    /** Whether we were canceled externally and should avoid self-removal. */
    private boolean mCanceled;

    /**
     * Creates a new ripple.
     */
    public Ripple(RippleDrawable owner, Rect bounds, float startingX, float startingY) {
        mOwner = owner;
        mBounds = bounds;

        mStartingX = startingX;
        mStartingY = startingY;
    }

    public void setup(int maxRadius, int color, float density) {
        mColorOpaque = color | 0xFF000000;
        mColorAlpha = Color.alpha(color);

        if (maxRadius != -1) {
            mHasMaxRadius = true;
            mOuterRadius = maxRadius;
        } else {
            final float halfWidth = mBounds.width() / 2.0f;
            final float halfHeight = mBounds.height() / 2.0f;
            mOuterRadius = (float) Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight);
        }

        mOuterX = 0;
        mOuterY = 0;
        mDensity = density;

        clampStartingPosition();
    }

    public void setAlpha(int alpha){
        mColorAlpha = alpha;
    }

    public int getAlpha(){
        return mColorAlpha;
    }

    private void clampStartingPosition() {
        final float cX = mBounds.exactCenterX();
        final float cY = mBounds.exactCenterY();
        final float dX = mStartingX - cX;
        final float dY = mStartingY - cY;
        final float r = mOuterRadius;
        if (dX * dX + dY * dY > r * r) {
            // Point is outside the circle, clamp to the circumference.
            final double angle = Math.atan2(dY, dX);
            mClampedStartingX = cX + (float) (Math.cos(angle) * r);
            mClampedStartingY = cY + (float) (Math.sin(angle) * r);
        } else {
            mClampedStartingX = mStartingX;
            mClampedStartingY = mStartingY;
        }
    }

    public void onHotspotBoundsChanged() {
        if (!mHasMaxRadius) {
            final float halfWidth = mBounds.width() / 2.0f;
            final float halfHeight = mBounds.height() / 2.0f;
            mOuterRadius = (float) Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight);

            clampStartingPosition();
        }
    }

    public void setOpacity(float a) {
        mOpacity = a;
        invalidateSelf();
    }

    public float getOpacity() {
        return mOpacity;
    }

    @SuppressWarnings("unused")
    public void setRadiusGravity(float r) {
        mTweenRadius = r;
        invalidateSelf();
    }

    @SuppressWarnings("unused")
    public float getRadiusGravity() {
        return mTweenRadius;
    }

    @SuppressWarnings("unused")
    public void setXGravity(float x) {
        mTweenX = x;
        invalidateSelf();
    }

    @SuppressWarnings("unused")
    public float getXGravity() {
        return mTweenX;
    }

    @SuppressWarnings("unused")
    public void setYGravity(float y) {
        mTweenY = y;
        invalidateSelf();
    }

    @SuppressWarnings("unused")
    public float getYGravity() {
        return mTweenY;
    }

    /**
     * Draws the ripple centered at (0,0) using the specified paint.
     */
    public boolean draw(Canvas c, Paint p) {
        final boolean hasContent;
        hasContent = drawSoftware(c, p);

        return hasContent;
    }

    private boolean drawSoftware(Canvas c, Paint p) {
        boolean hasContent = false;

        p.setColor(mColorOpaque);
        final int alpha = (int) (mColorAlpha * mOpacity + 0.5f);
        final float radius = lerp(0, mOuterRadius, mTweenRadius);
        if (alpha > 0 && radius > 0) {
            final float x = lerp(
                    mClampedStartingX - mBounds.exactCenterX(), mOuterX, mTweenX);
            final float y = lerp(
                    mClampedStartingY - mBounds.exactCenterY(), mOuterY, mTweenY);
            p.setAlpha(alpha);
            p.setStyle(Style.FILL);
            c.drawCircle(x, y, radius, p);
            hasContent = true;
        }

        return hasContent;
    }

    public static float lerp(float start, float stop, float amount) {
        return start + (stop - start) * amount;
    }

    /**
     * Returns the maximum bounds of the ripple relative to the ripple center.
     */
    public void getBounds(Rect bounds) {
        final int outerX = (int) mOuterX;
        final int outerY = (int) mOuterY;
        final int r = (int) mOuterRadius + 1;
        bounds.set(outerX - r, outerY - r, outerX + r, outerY + r);
    }

    /**
     * Specifies the starting position relative to the drawable bounds. No-op if
     * the ripple has already entered.
     */
    public void move(float x, float y) {
        mStartingX = x;
        mStartingY = y;

        clampStartingPosition();
    }

    /**
     * Starts the enter animation.
     */
    public void enter() {
        cancel();

        final int radiusDuration = (int)
                (1000 * Math.sqrt(mOuterRadius / WAVE_TOUCH_DOWN_ACCELERATION * mDensity) + 0.5);

        final ObjectAnimator radius = ObjectAnimator.ofFloat(this, "radiusGravity", 1);
        radius.setAutoCancel(true);
        radius.setDuration(radiusDuration);
        radius.setInterpolator(LINEAR_INTERPOLATOR);
        radius.setStartDelay(RIPPLE_ENTER_DELAY);

        final ObjectAnimator cX = ObjectAnimator.ofFloat(this, "xGravity", 1);
        cX.setAutoCancel(true);
        cX.setDuration(radiusDuration);
        cX.setInterpolator(LINEAR_INTERPOLATOR);
        cX.setStartDelay(RIPPLE_ENTER_DELAY);

        final ObjectAnimator cY = ObjectAnimator.ofFloat(this, "yGravity", 1);
        cY.setAutoCancel(true);
        cY.setDuration(radiusDuration);
        cY.setInterpolator(LINEAR_INTERPOLATOR);
        cY.setStartDelay(RIPPLE_ENTER_DELAY);

        mAnimRadius = radius;
        mAnimX = cX;
        mAnimY = cY;

        // Enter animations always run on the UI thread, since it's unlikely
        // that anything interesting is happening until the user lifts their
        // finger.
        radius.start();
        cX.start();
        cY.start();
    }

    /**
     * Starts the exit animation.
     */
    public void exit() {
        cancel();

        final float radius = lerp(0, mOuterRadius, mTweenRadius);
        final float remaining;
        if (mAnimRadius != null && mAnimRadius.isRunning()) {
            remaining = mOuterRadius - radius;
        } else {
            remaining = mOuterRadius;
        }

        final int radiusDuration = (int) (1000 * Math.sqrt(remaining / (WAVE_TOUCH_UP_ACCELERATION
                + WAVE_TOUCH_DOWN_ACCELERATION) * mDensity) + 0.5);
        final int opacityDuration = (int) (1000 * mOpacity / WAVE_OPACITY_DECAY_VELOCITY + 0.5f);

        exitSoftware(radiusDuration, opacityDuration);
    }



    /**
     * Jump all animations to their end state. The caller is responsible for
     * removing the ripple from the list of animating ripples.
     */
    public void jump() {
        mCanceled = true;
        endSoftwareAnimations();
        mCanceled = false;
    }

    private void endSoftwareAnimations() {
        if (mAnimRadius != null) {
            mAnimRadius.end();
            mAnimRadius = null;
        }

        if (mAnimOpacity != null) {
            mAnimOpacity.end();
            mAnimOpacity = null;
        }

        if (mAnimX != null) {
            mAnimX.end();
            mAnimX = null;
        }

        if (mAnimY != null) {
            mAnimY.end();
            mAnimY = null;
        }
    }

    private Paint getTempPaint() {
        if (mTempPaint == null) {
            mTempPaint = new Paint();
        }
        return mTempPaint;
    }

    private void exitSoftware(int radiusDuration, int opacityDuration) {
        final ObjectAnimator radiusAnim = ObjectAnimator.ofFloat(this, "radiusGravity", 1);
        radiusAnim.setAutoCancel(true);
        radiusAnim.setDuration(radiusDuration);
        radiusAnim.setInterpolator(DECEL_INTERPOLATOR);

        final ObjectAnimator xAnim = ObjectAnimator.ofFloat(this, "xGravity", 1);
        xAnim.setAutoCancel(true);
        xAnim.setDuration(radiusDuration);
        xAnim.setInterpolator(DECEL_INTERPOLATOR);

        final ObjectAnimator yAnim = ObjectAnimator.ofFloat(this, "yGravity", 1);
        yAnim.setAutoCancel(true);
        yAnim.setDuration(radiusDuration);
        yAnim.setInterpolator(DECEL_INTERPOLATOR);

        final ObjectAnimator opacityAnim = ObjectAnimator.ofFloat(this, "opacity", 0);
        opacityAnim.setAutoCancel(true);
        opacityAnim.setDuration(opacityDuration);
        opacityAnim.setInterpolator(LINEAR_INTERPOLATOR);
        opacityAnim.addListener(mAnimationListener);

        mAnimRadius = radiusAnim;
        mAnimOpacity = opacityAnim;
        mAnimX = xAnim;
        mAnimY = yAnim;

        radiusAnim.start();
        opacityAnim.start();
        xAnim.start();
        yAnim.start();
    }

    /**
     * Cancels all animations. The caller is responsible for removing
     * the ripple from the list of animating ripples.
     */
    public void cancel() {
        mCanceled = true;
        cancelSoftwareAnimations();
        mCanceled = false;
    }

    private void cancelSoftwareAnimations() {
        if (mAnimRadius != null) {
            mAnimRadius.cancel();
            mAnimRadius = null;
        }

        if (mAnimOpacity != null) {
            mAnimOpacity.cancel();
            mAnimOpacity = null;
        }

        if (mAnimX != null) {
            mAnimX.cancel();
            mAnimX = null;
        }

        if (mAnimY != null) {
            mAnimY.cancel();
            mAnimY = null;
        }
    }

    private void removeSelf() {
        // The owner will invalidate itself.
        if (!mCanceled) {
            mOwner.removeRipple(this);
        }
    }

    private void invalidateSelf() {
        mOwner.invalidateSelf();
    }

    private final AnimatorListenerAdapter mAnimationListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            removeSelf();
        }
    };

    /**
    * Interpolator with a smooth log deceleration
    */
    private static final class LogInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float input) {
            return 1 - (float) Math.pow(400, -input * 1.4);
        }
    }
}
