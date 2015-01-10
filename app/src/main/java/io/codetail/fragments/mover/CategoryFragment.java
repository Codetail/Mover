package io.codetail.fragments.mover;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import io.codetail.WatchMeApplication;
import io.codetail.adapters.WatchMeAdapterNew;
import io.codetail.client.State;
import io.codetail.client.models.Video;
import io.codetail.client.mover.FetchAvailableVideoQualities;
import io.codetail.client.mover.FetchCategoryPage;
import io.codetail.client.mover.Mover;
import io.codetail.fragments.NavigationFragment;
import io.codetail.utils.ScrollManager;
import io.codetail.watchme.R;
import io.codetail.work.JobManager;

import static android.view.View.NO_ID;

public class CategoryFragment extends MoverRecycleFragment {

    public final static String SELECTED_CATEGORY = "selected_category";
    public final static String CURRENT_PAGE = "current_page";
    final static String PAGES_COUNT = "pages_count  ";

    final static int UNKNOWN_STATE = -2;

    private String[] mCategories;
    private String mSelectedCategory;

    private int mSelectedCategoryPosition;
    private int mCurrentPageNumber = 1;
    private int mCategoryPagesCount;

    private WatchMeAdapterNew mWatchMeAdapter;

    @Inject
    JobManager mJobManager;

    private long mPendingJobId = NO_ID;

    private boolean mAlreadyCreated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mSelectedCategoryPosition = savedInstanceState.getInt(SELECTED_CATEGORY, 0);

            mCurrentPageNumber = savedInstanceState.getInt(CURRENT_PAGE);
            mCategoryPagesCount = savedInstanceState.getInt(PAGES_COUNT);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavigationFragment navigation = (NavigationFragment) getFragmentManager()
                .findFragmentById(R.id.navigation_fragment);

        mWatchMeAdapter = getWatchMeAdapter();

        WatchMeApplication application = WatchMeApplication.getApplication();
        application.inject(this);
        application.inject(getScrollManager());
        application.inject(mWatchMeAdapter);

        mCategories = getResources().getStringArray(R.array.mover_category_list_for_app);

        if(savedInstanceState != null){
            mSelectedCategory = mCategories[mSelectedCategoryPosition];
            navigation.setSelected(mSelectedCategoryPosition);
        }else{
            if(getArguments() != null) {
                Bundle args = getArguments();

                String watchId = args.getString("watchId");
                if(!TextUtils.isEmpty(watchId)){
                    mJobManager.addJob(new FetchAvailableVideoQualities(watchId));
                    setSelected(0);
                    return;
                }

                setSelected(args.getString(SELECTED_CATEGORY), args.getInt(CURRENT_PAGE));
                navigation.setSelected(mSelectedCategoryPosition);
                return;
            }

            if(!mAlreadyCreated) {
                navigation.setSelected(0);
                setSelected(0);
            }
        }

        mAlreadyCreated = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        NavigationFragment navigation = (NavigationFragment) getFragmentManager()
                .findFragmentById(R.id.navigation_fragment);

        navigation.setSelected(mSelectedCategoryPosition);
    }

    @DebugLog
    public void renderPaginatedPage(List<Video> videos){
        mWatchMeAdapter.add(getString(R.string.section_paginated_page, mCurrentPageNumber), videos, videos.size());
    }

    @DebugLog
    public void renderMainPage(List<Video> recommends, List<Video> popular, List<Video> videos){
        mWatchMeAdapter.add(getString(R.string.section_recommendations), recommends, Math.min(recommends.size(), 3));
        mWatchMeAdapter.add(getString(R.string.section_popular), popular, Math.min(popular.size(), 3));
        mWatchMeAdapter.add(getString(R.string.section_last_added), videos, videos.size());

        if(!getScrollManager().isToolbarVisible()){
            ViewGroup toolbarWrapper = (ViewGroup) (getWatchMeActivity()).getToolbar()
                    .getParent();

            getScrollManager().show(toolbarWrapper);
        }
    }

    @DebugLog @Subscribe @SuppressWarnings("unused")
    public void onLoadMoreItemsEvent(ScrollManager.LoadMoreItems items){
        if(isPageLoading() || !canLoadMorePages()){
            return;
        }

        mPendingJobId = mJobManager.addJob(new FetchCategoryPage(mSelectedCategory, mCurrentPageNumber + 1));
    }

    @Override @DebugLog @Subscribe @SuppressWarnings("unused")
    public void onSuggestionAvailable(Mover.Suggestion suggestion) {
        super.onSuggestionAvailable(suggestion);
    }

    boolean isPageLoading(){
        return mPendingJobId != NO_ID;
    }

    boolean canLoadMorePages(){
        return mCategoryPagesCount > 1 || mCategoryPagesCount == UNKNOWN_STATE;
    }

    @Subscribe @SuppressWarnings("unused")
    public void onStartLoadingPage(State.OnStartLoadingPage event){
        if(event.page < 2){
            getRecycleView().scrollToPosition(0);
            mWatchMeAdapter.clear();

            if(!isProgressVisible()){
                showProgress();
            }
        }
    }

    @Subscribe @SuppressWarnings("unused")
    public void onResponse(State.OnPageResponseEvent event){
        Mover.CategoryPage page = (Mover.CategoryPage) event.page;

        mPendingJobId = NO_ID;
        mCategoryPagesCount = page.getPagesCount();
        mCurrentPageNumber = 1;

        if(mCategoryPagesCount == -1) {
            mCategoryPagesCount = UNKNOWN_STATE;
        }

        showContent();
        renderMainPage(page.getRecommends(), page.getPopular(), page.getVideos());
    }

    @Subscribe @SuppressWarnings("unused")
    public void onResponse(State.OnPaginatedPageResponseEvent event){
        Mover.PaginatedPage page = (Mover.PaginatedPage) event.page;

        mPendingJobId = NO_ID;
        mCurrentPageNumber = page.getPageNumber();
        mCategoryPagesCount = page.getPagesCount();

        mCategoryPagesCount = page.getPagesCount();

        showContent();
        renderPaginatedPage(page.getVideos());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SELECTED_CATEGORY, mSelectedCategoryPosition);
        outState.putInt(CURRENT_PAGE, mCurrentPageNumber);
        outState.putInt(PAGES_COUNT, mCategoryPagesCount);
    }

    public void setSelected(String item, int pageNum){
        int position = 0;
        for(int index = 0; index < mCategories.length; index++){
            if(mCategories[index].equals(item)){
                position = index;
            }
        }

        mSelectedCategoryPosition = position;
        mSelectedCategory = item.trim();

        if(mPendingJobId != NO_ID){
            mJobManager.cancelJobInBackground(mPendingJobId, false);
        }

        mPendingJobId = mJobManager.addJob(new FetchCategoryPage(mSelectedCategory, pageNum));
    }

    public void setSelected(int position){
        mSelectedCategoryPosition = position;
        mSelectedCategory = mCategories[position].trim();

        if(mPendingJobId != NO_ID){
            mJobManager.cancelJobInBackground(mPendingJobId, false);
        }

        mPendingJobId = mJobManager.addJob(new FetchCategoryPage(mSelectedCategory, 1));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mPendingJobId != NO_ID){
            mJobManager.cancelJobInBackground(mPendingJobId, false);
        }
    }

    @Override
    public JobManager getJobManager() {
        return mJobManager;
    }

}

