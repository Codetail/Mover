package io.codetail.recycle;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import codetail.utils.ResourceUtils;
import io.codetail.adapters.WatchMeAdapterNew;

/**
 * ItemDecoration implementation that applies and inset margin
 * around each child of the RecyclerView. It also draws item dividers
 * that are expected from a vertical list implementation, such as
 * ListView.
 */
public class DividerDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = { android.R.attr.listDivider };

    private Drawable mDivider;
    private int mInsets;

    public DividerDecoration(Context context) {
        TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();

        mInsets = ResourceUtils.dp(16);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        drawVertical(c, parent);
    }

    /** Draw dividers underneath each child view */
    public void drawVertical(Canvas c, RecyclerView parent) {
        final int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();

            WatchMeAdapterNew.WatchMeHolder holder = (WatchMeAdapterNew.WatchMeHolder)
                    parent.findViewHolderForPosition(params.getViewPosition());

            if(holder == null){
                continue;
            }

            if(holder.getItemViewType() == WatchMeAdapterNew.TYPE_VIDEO){
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                final int left = child.getPaddingLeft() + child.getLeft();
                final int right = child.getRight() - child.getPaddingRight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }
}