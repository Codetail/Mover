package io.codetail.fragments.mover;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import codetail.graphics.drawables.MenuDrawable;
import codetail.widget.SearchView;
import codetail.widget.Toolbar;
import hugo.weaving.DebugLog;
import io.codetail.WatchMeApplication;
import io.codetail.adapters.WatchMeAdapterNew;
import io.codetail.client.State;
import io.codetail.client.mover.FetchSearchPage;
import io.codetail.client.mover.Mover;
import io.codetail.utils.ScrollManager;
import io.codetail.watchme.R;
import io.codetail.work.JobManager;

import static android.view.View.NO_ID;

public class SearchFragment extends MoverRecycleFragment implements SearchView.QueryCallback{

    public final static String SEARCH_QUERY = "search_query";
    public final static String SEARCH_CURRENT_PAGE = "search_current_page";

    private final static String SEARCH_PAGES_COUNT = "search_pages_count  ";

    private final static int UNKNOWN_STATE = -2;

    private int mCurrentPageNumber = UNKNOWN_STATE;
    private int mSearchPagesCount = UNKNOWN_STATE;

    private WatchMeAdapterNew mWatchMeAdapter;

    private String mQuery;

    @Inject
    JobManager mJobManager;

    private long mPendingJobId = NO_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            mQuery = savedInstanceState.getString(SEARCH_QUERY);
            mSearchPagesCount = savedInstanceState.getInt(SEARCH_PAGES_COUNT, UNKNOWN_STATE);
            mCurrentPageNumber = savedInstanceState.getInt(SEARCH_CURRENT_PAGE, UNKNOWN_STATE);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWatchMeAdapter = getWatchMeAdapter();

        WatchMeApplication application = WatchMeApplication.getApplication();
        application.inject(this);
        application.inject(getScrollManager());
        application.inject(mWatchMeAdapter);

        setupToolbar(true, savedInstanceState);
        setupParent();

        if(getArguments() != null){
            Bundle args = getArguments();
            mQuery = args.getString(SEARCH_QUERY);
            mCurrentPageNumber = args.getInt(SEARCH_CURRENT_PAGE);

            SearchView search = getWatchMeActivity().getToolbar().getSearchView();
            search.setQuery(mQuery);

            mPendingJobId = mJobManager.addJob(new FetchSearchPage(mQuery, mCurrentPageNumber));
        }
    }

    void setupParent(){
        DrawerLayout drawer = ButterKnife.findById(getActivity(), R.id.drawer_container);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawer.closeDrawer(GravityCompat.START);
    }

    void setupToolbar(boolean init){
        setupToolbar(init, null);
    }

    void setupToolbar(boolean init, Bundle state){
        Toolbar toolbar = getWatchMeActivity().getToolbar();
        toolbar.setTitleVisible(!init);

        MenuDrawable drawable = (MenuDrawable) toolbar.getNavigationIcon();
        drawable.setRotation(init ? 1 : 0, true);

        SearchView search = toolbar.getSearchView();
        search.setCallback(init ? this : null);

        if(init){
            search.expand();
            search.setQueryHintText(getString(R.string.toolbar_search_hint));

            if(state != null){
                search.setQuery(state.getString(SEARCH_QUERY));
            }

        }else{
            search.collapse();
        }
    }

    @Subscribe @SuppressWarnings("unused")
    public void onSearchResult(State.OnSearchResponseEvent event){
        Mover.SearchPage response = (Mover.SearchPage) event.page;

        mPendingJobId = NO_ID;

        if(!response.hasResult()){
            return;
        }

        mCurrentPageNumber = response.getPageNumber();
        mSearchPagesCount = response.getPagesCount();

        showContent();

        mWatchMeAdapter.add(getString(R.string.section_paginated_page, mCurrentPageNumber), response.getVideos(),
                response.getVideos().size());
    }

    public void setSearchQuery(String query){
        if(mPendingJobId != NO_ID){
            mJobManager.cancelJobInBackground(mPendingJobId, false);
        }

        mQuery = query;
        mPendingJobId = mJobManager.addJob(new FetchSearchPage(mQuery, 1));

        mWatchMeAdapter.clear();

        if(!isProgressVisible()){
            showProgress();
        }
    }

    boolean isPageLoading(){
        return mPendingJobId != NO_ID;
    }

    boolean canLoadMorePages(){
        return mSearchPagesCount > 1 || mSearchPagesCount == UNKNOWN_STATE;
    }

    @Subscribe @SuppressWarnings("unused")
    public void onStartLoadingPage(State.OnStartLoadingPage event){}

    @Subscribe @SuppressWarnings("unused")
    public void onLoadMoreItemsEvent(ScrollManager.LoadMoreItems items){
        if(isPageLoading() || !canLoadMorePages()){
            return;
        }

        mPendingJobId = mJobManager.addJob(new FetchSearchPage(mQuery, mCurrentPageNumber + 1));
    }

    @Override @DebugLog @Subscribe @SuppressWarnings("unused")
    public void onSuggestionAvailable(Mover.Suggestion suggestion) {
        super.onSuggestionAvailable(suggestion);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SEARCH_CURRENT_PAGE, mCurrentPageNumber);
        outState.putInt(SEARCH_PAGES_COUNT, mSearchPagesCount);
        outState.putString(SEARCH_QUERY, mQuery);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        DrawerLayout drawer = ButterKnife.findById(getActivity(), R.id.drawer_container);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        setupToolbar(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mPendingJobId != NO_ID) {
            mJobManager.cancelJobInBackground(mPendingJobId, false);
        }
    }

    @Override
    public JobManager getJobManager() {
        return mJobManager;
    }

    @Override
    public void onSearchQuery(CharSequence query) {
        setSearchQuery(query.toString());
    }
}

