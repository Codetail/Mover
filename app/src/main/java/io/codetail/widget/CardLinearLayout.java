package io.codetail.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import io.codetail.watchme.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class CardLinearLayout extends CardView{

    LayoutInflater mFactory;

    Paint mDividerPaint;
    final int mDividerHeight;
    final int mDividerOffset;

    View mTitle;
    View mShowMore;

    int mItemsCount;

    public CardLinearLayout(Context context) {
        this(context, null);
    }

    public CardLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);

        mFactory = LayoutInflater.from(context);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CardLinearLayout);

        int dividerColor = array.getColor(R.styleable.CardLinearLayout_cl_dividerColor, Color.TRANSPARENT);
        mDividerHeight = array.getDimensionPixelSize(R.styleable.CardLinearLayout_cl_divider, 0);
        mDividerOffset = array.getDimensionPixelSize(R.styleable.CardLinearLayout_cl_dividersOffset, 0);

        array.recycle();

        mDividerPaint = new Paint();
        mDividerPaint.setColor(dividerColor);
        mDividerPaint.setStyle(Paint.Style.FILL);
    }

    public View[] beginBinding(int itemsCount){
        if(mTitle == null && mShowMore == null){
            mTitle = findViewById(R.id.cardTitle);
            mShowMore = findViewById(R.id.showMoreButton);
        }

        int childCount = getChildCount();

        int newChildLength = itemsCount - (childCount - 2);

        // if children count less than items inflate them
        for(int index = 0; index < newChildLength; index++){
            mFactory.inflate(R.layout.card_video_item, this);
        }

        childCount = getChildCount();

        for(int index = 2; index < childCount; index++){
            getChildAt(index).setVisibility(View.GONE);
        }

        mItemsCount = itemsCount;

        View[] bindViews = new View[itemsCount];

        for(int index = 0; index < itemsCount; index++){
            bindViews[index] = getChildAt(index + 2);
            bindViews[index].setVisibility(View.VISIBLE);
        }

        return bindViews;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int childCount = getChildCount();

        int paddingLeft = getContentPaddingLeft();
        int paddingRight = getContentPaddingRight();

        for(int index = 0; index < childCount; index++) {
            View view = getChildAt(index);
            if(view.getVisibility() == VISIBLE) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();

                int dividerOffset = params.dividerOffset == -1 ? mDividerOffset : params.dividerOffset;

                if (params.dividersEnabled) {
                    canvas.drawRect(paddingLeft, view.getBottom() + dividerOffset,
                            (getWidth() - paddingRight),
                            view.getBottom() + dividerOffset + mDividerHeight, mDividerPaint);
                }
            }
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int childCount = getChildCount();

        int contentLeft = getContentPaddingLeft();
        int contentTop = getContentPaddingTop();

        LayoutParams params = (LayoutParams) mTitle .getLayoutParams();

        contentTop += params.topMargin;

        mTitle.layout(contentLeft + params.leftMargin, contentTop,
                contentLeft + params.leftMargin + mTitle.getMeasuredWidth(),
                contentTop + mTitle.getMeasuredHeight());

        contentTop += mTitle.getMeasuredHeight();
        contentTop += params.bottomMargin;

        for(int index = 0; index < childCount; index++){

            View view = getChildAt(index);
            if(view.getVisibility() == VISIBLE && view != mTitle && view != mShowMore) {

                params = (LayoutParams) view.getLayoutParams();

                int l = contentLeft + params.leftMargin;
                int t = contentTop + params.topMargin;

                view.layout(l, t, l + view.getMeasuredWidth(), t + view.getMeasuredHeight());
                contentTop += params.topMargin + view.getMeasuredHeight() + params.bottomMargin;
            }
        }

        if(mShowMore.getVisibility() != VISIBLE){
            params.dividersEnabled = false;
            return;
        }

        params = (LayoutParams) mShowMore .getLayoutParams();

        contentTop += params.topMargin;
        int l = right - getPaddingRight() - params.rightMargin - mShowMore.getMeasuredWidth();

        mShowMore.layout(l, contentTop, l + mShowMore.getMeasuredWidth(),
                contentTop + mShowMore.getMeasuredHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int childCount = getChildCount();
        int heightUsed = 0;
        int widthUsed = MeasureSpec.getSize(widthMeasureSpec);

        for(int index = 0; index < childCount; index++){
            View child = getChildAt(index);

            if(child.getVisibility() == VISIBLE) {

                measureChildWithMargins(child,
                        widthMeasureSpec, 0, heightMeasureSpec, heightUsed);

                heightUsed += getMeasuredHeightWithMargins(child);

            }
        }

        widthUsed = Math.max(widthUsed, getSuggestedMinimumWidth());
        heightUsed = Math.max(heightUsed, getSuggestedMinimumHeight());
        heightUsed += getContentPaddingTop() + getContentPaddingBottom();

        setMeasuredDimension(widthUsed, heightUsed);
    }

    private int getMeasuredHeightWithMargins(View target){
        MarginLayoutParams params = (MarginLayoutParams) target.getLayoutParams();
        return params.topMargin + target.getMeasuredHeight() + params.bottomMargin;
    }

    @Override
    protected FrameLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(@NonNull ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends FrameLayout.LayoutParams{

        public int dividerOffset = -1;
        public boolean dividersEnabled = true;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray array = c.obtainStyledAttributes(attrs, R.styleable.CardLinearLayout_Layout);
            dividerOffset = array.getDimensionPixelOffset(R.styleable.CardLinearLayout_Layout_clLayout_dividerOffset, -1);
            dividersEnabled = array.getBoolean(R.styleable.CardLinearLayout_Layout_clLayout_dividerEnabled, true);
            array.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public LayoutParams(FrameLayout.LayoutParams source) {
            super(source);
        }
    }
}
