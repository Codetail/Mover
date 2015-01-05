package io.codetail.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import codetail.animation.ViewAnimationUtils;
import codetail.utils.HideExtraOnScrollHelper;
import codetail.utils.ResourceUtils;
import io.codetail.Constants;
import io.codetail.WatchMeApplication;

public class ScrollManager extends RecyclerView.OnScrollListener{

    public static class LoadMoreItems{}

    public final static int MIN_VISIBLE_ITEMS = 20 * 3;
    public final static int LOAD_IF_LESS_THAN = 5;

    final static Interpolator ACCELERATE = new AccelerateInterpolator();
    final static Interpolator DECELERATE = new DecelerateInterpolator();

    Bus mEventBus;
    @Inject Picasso mPicasso;

    WeakReference<View> mTargetToolbar;
    WeakReference<View> mTargetFab;
    HideExtraOnScrollHelper mScrollHelper;

    LoadMoreItems mMoreEvent;
 
    boolean isExtraObjectsOutside;
    boolean isScrollUpFloating;
 
    public ScrollManager(View target, View fab) {
        int minimumFlingVelocity = ViewConfiguration.get(target.getContext())
                .getScaledMinimumFlingVelocity();
 
        mScrollHelper = new HideExtraOnScrollHelper(minimumFlingVelocity);
        mTargetToolbar = new WeakReference<>(target);
        mTargetFab = new WeakReference<>(fab);
        mMoreEvent = new LoadMoreItems();

        mEventBus = Constants.getEventBus();
    }

    public void setEventBus(Bus bus) {
        mEventBus = bus;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        switch (newState){
            case RecyclerView.SCROLL_STATE_DRAGGING:
                mPicasso.pauseTag(WatchMeApplication.PICASSO_INSTANCE);
                break;

            case RecyclerView.SCROLL_STATE_IDLE:
                mPicasso.resumeTag(WatchMeApplication.PICASSO_INSTANCE);
                break;
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
 
        final View target = mTargetToolbar.get();
        final View fab = mTargetFab.get();

        if(target == null || fab == null) {
            return;
        }
 
        boolean needsToHideExtraObjects = mScrollHelper.isObjectsShouldBeOutside(dy);
 
        if(!isToolbarVisible() && !needsToHideExtraObjects){
            show(target);
            isExtraObjectsOutside = false;
        }else if(isToolbarVisible() && needsToHideExtraObjects){
            hide(target, -target.getHeight());
            isExtraObjectsOutside = true;
        }

        toggleScrollUp(recyclerView);

        loadMoreItemsIfNecessary(recyclerView);
    }

    public boolean isUserNeedInFastScrollUp(RecyclerView view){
        LinearLayoutManager manager = (LinearLayoutManager) view.getLayoutManager();
        return manager.findFirstCompletelyVisibleItemPosition() >= MIN_VISIBLE_ITEMS;
    }

    public void toggleScrollUp(RecyclerView view){
        boolean showFab = isUserNeedInFastScrollUp(view);

        if(showFab && !isFabVisible()){
            isScrollUpFloating = true;
            showFab();
        }else if(!showFab && isFabVisible()){
            isScrollUpFloating = false;
            hideFab();
        }
    }

    public void showFab(){
        View fab = mTargetFab.get();
        fab.setVisibility(View.VISIBLE);

        ViewHelper.setTranslationY(fab, fab.getHeight() + ResourceUtils.dp(16));

        ViewPropertyAnimator
                .animate(fab)
                .setInterpolator(ViewAnimationUtils.ACCELERATE_DECELERATE)
                .setDuration(350)
                .translationY(0)
                .start();
    }

    public void showToolbar(){
        show(mTargetToolbar.get());
    }

    public void hideFab(){
        View fab = mTargetFab.get();

        ViewPropertyAnimator
                .animate(fab)
                .setInterpolator(ViewAnimationUtils.ACCELERATE_DECELERATE)
                .setDuration(350)
                .translationY(fab.getHeight() + ResourceUtils.dp(16))
                .start();
    }

    public void loadMoreItemsIfNecessary(RecyclerView recyclerView){
        LinearLayoutManager layout = (LinearLayoutManager) recyclerView.getLayoutManager();

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = layout.getItemCount();
        int firstVisibleItem = layout.findFirstVisibleItemPosition();

        if ((totalItemCount - (firstVisibleItem +
                visibleItemCount)) <= LOAD_IF_LESS_THAN){
            mEventBus.post(mMoreEvent);
        }
    }

    public boolean isToolbarVisible(){
        return !isExtraObjectsOutside;
    }

    public boolean isFabVisible(){
        return isScrollUpFloating;
    }

    public void hide(final View target, float distance){
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationY",
                ViewHelper.getTranslationY(target), distance);
        animator.setAutoCancel(true);
        animator.setInterpolator(DECELERATE);
        animator.start();
    }
 
    public void show(final View target){
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationY",
                ViewHelper.getTranslationY(target), 0f);
        animator.setAutoCancel(true);
        animator.setInterpolator(ACCELERATE);
        animator.start();
    }
 
}