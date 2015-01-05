package io.codetail.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class CardLayout extends ViewGroup{

    public CardLayout(Context context) {
        this(context, null);
    }

    public CardLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setClickable(true);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int contentLeft = getPaddingLeft();
        int contentTop = getPaddingTop();

        View thumbnail = getChildAt(0);
        MarginLayoutParams params = (MarginLayoutParams) thumbnail.getLayoutParams();

        int width = thumbnail.getMeasuredWidth();
        int height = thumbnail.getMeasuredHeight();

        thumbnail.layout(contentLeft, contentTop,
                contentLeft + width, contentTop + height);

        contentLeft += width;
        contentLeft += params.rightMargin;

        final int childrenCount = getChildCount();

        for(int index = 1; index < childrenCount; index++){
            View view = getChildAt(index);
            params = (MarginLayoutParams) view.getLayoutParams();

            width = view.getMeasuredWidth();
            height = view.getMeasuredHeight();

            contentLeft += params.leftMargin;
            contentTop += params.topMargin;

            view.layout(contentLeft, contentTop,
                    contentLeft + width, contentTop + height);

            contentTop += height;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthUsed = 0;
        int heightUsed = 0;

        View thumbnail = getChildAt(0);
        measureChildWithMargins(thumbnail, widthMeasureSpec, widthUsed,
                heightMeasureSpec, heightUsed);

        widthUsed += getMeasuredWidthWithMargins(thumbnail);

        TextView title = (TextView) getChildAt(1);
        measureChildWithMargins(title, widthMeasureSpec, widthUsed, heightMeasureSpec, 0);

        heightUsed += getMeasuredHeightWithMargins(title);

        TextView author = (TextView) getChildAt(2);
        measureChildWithMargins(author, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);

        heightUsed += getMeasuredHeightWithMargins(author);

        TextView views = (TextView) getChildAt(3);
        measureChildWithMargins(views, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);

        heightUsed += getMeasuredHeightWithMargins(views);

        heightUsed = Math.max(heightUsed, getMeasuredHeightWithMargins(thumbnail));
        heightUsed += getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), heightUsed);
    }

    private int getMeasuredWidthWithMargins(View target){
        MarginLayoutParams params = (MarginLayoutParams) target.getLayoutParams();
        return params.leftMargin + target.getMeasuredWidth() + params.rightMargin;
    }

    private int getMeasuredHeightWithMargins(View target){
        MarginLayoutParams params = (MarginLayoutParams) target.getLayoutParams();
        return params.topMargin + target.getMeasuredHeight() + params.bottomMargin;
    }


    @Override
    protected MarginLayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT);
    }

    @Override
    public MarginLayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected MarginLayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }
}
