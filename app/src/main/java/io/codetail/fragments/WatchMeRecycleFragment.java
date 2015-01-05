package io.codetail.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.telly.mrvector.MrVector;

import butterknife.ButterKnife;
import codetail.graphics.drawables.DrawableHelper;
import codetail.utils.ThemeUtils;
import codetail.utils.ViewUtils;
import codetail.widget.FloatingActionButton;
import io.codetail.WatchMeActivity;
import io.codetail.adapters.WatchMeAdapterNew;
import io.codetail.recycle.DividerDecoration;
import io.codetail.recycle.ItemClickSupport;
import io.codetail.utils.ScrollManager;
import io.codetail.watchme.R;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;

public abstract class WatchMeRecycleFragment extends BaseWatchMeFragment
        implements ItemClickSupport.OnItemClickListener, ItemClickSupport.OnItemLongClickListener{

    private WatchMeAdapterNew mWatchMeAdapter;
    private ScrollManager mScrollManager;

    private RecyclerView mRecycleView;
    private ProgressBar mContentProgress;

    private final View.OnClickListener mScrollToTopClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            scrollToTop();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWatchMeAdapter = new WatchMeAdapterNew(WatchMeAdapterNew.generateKey(this));

        if(savedInstanceState != null) {
            mWatchMeAdapter.onRestoreInstanceState(savedInstanceState);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watchme, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecycleView = ButterKnife.findById(view, R.id.recycler_view);
        mContentProgress = ButterKnife.findById(view, R.id.content_progress);

        FloatingActionButton scrollUpFab = ButterKnife.findById(view, R.id.fab_scroll_up);
        scrollUpFab.setOnClickListener(mScrollToTopClick);

        WatchMeActivity context = getWatchMeActivity();
        mWatchMeAdapter.initResources(context);

        Drawable drawable = MrVector.inflate(getResources(), R.drawable.ic_arrow_scroll_to_top);
        DrawableHelper.setTint(drawable, ThemeUtils.getThemeColor(context, R.attr.fabIconColor));

        scrollUpFab.setActionIcon(drawable);

        ItemClickSupport itemClickSupport = ItemClickSupport.addTo(mRecycleView);
        itemClickSupport.setOnItemClickListener(this);
        itemClickSupport.setOnItemLongClickListener(this);

        ViewGroup toolbarWrapper = (ViewGroup) context.getToolbar().getParent();
        mScrollManager = new ScrollManager(toolbarWrapper, scrollUpFab);

        mRecycleView.setVerticalScrollBarEnabled(true);
        mRecycleView.addItemDecoration(new DividerDecoration(context));
        mRecycleView.setOnScrollListener(mScrollManager);
        mRecycleView.setHasFixedSize(false);
        mRecycleView.setLayoutManager(new LinearLayoutManager(context, VERTICAL, false));
        mRecycleView.setAdapter(mWatchMeAdapter);

        showContent();
    }

    public void scrollToTop(){
        mRecycleView.scrollToPosition(0);
        mScrollManager.showToolbar();
        mScrollManager.hideFab();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWatchMeAdapter.notifyDataSetChanged();
    }

    public void showProgress(){
        ViewUtils.setVisibilityWithGoneFlag(mRecycleView, false);
        ViewUtils.setVisibilityWithGoneFlag(mContentProgress, true);
    }

    public void showContent(){
        ViewUtils.setVisibilityWithGoneFlag(mRecycleView, true);
        ViewUtils.setVisibilityWithGoneFlag(mContentProgress, false);

        mScrollManager.toggleScrollUp(mRecycleView);
    }

    public boolean isProgressVisible(){
        return false;
    }

    public WatchMeAdapterNew getWatchMeAdapter() {
        return mWatchMeAdapter;
    }

    public ScrollManager getScrollManager() {
        return mScrollManager;
    }

    public RecyclerView getRecycleView() {
        return mRecycleView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWatchMeAdapter.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mRecycleView != null) {
            mRecycleView.setOnScrollListener(null);
            mScrollManager = null;

            ItemClickSupport.removeFrom(mRecycleView);
        }
    }

    @Override
    public void onItemClick(RecyclerView parent, View view, int position, long id) {

    }

    @Override
    public boolean onItemLongClick(RecyclerView parent, View view, int position, long id) {
        return false;
    }
}
