package io.codetail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import codetail.graphics.drawables.MenuDrawable;
import codetail.utils.ThemeUtils;
import codetail.widget.SearchView;
import codetail.widget.Toolbar;
import io.codetail.sources.Source;
import io.codetail.watchme.R;
import io.codetail.work.JobManager;

public class LauncherActivity extends WatchMeActivity{

    @InjectView(R.id.drawer_container)
    DrawerLayout mNavigationLayout;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.toolbar_layout)
    ViewGroup mToolbarWrapper;

    private FragmentManager mFragmentManager;
    @Inject JobManager mJobManager;

    private final View.OnClickListener mHamburgerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SearchView searchView = mToolbar.getSearchView();
            if(!searchView.isSearchExpanded()){
                mNavigationLayout.openDrawer(GravityCompat.START);
            }else{
                // transform from back button to hamburger
                setNavigationLockMode(false);
                forceBack();
            }
        }
    };

    private final View.OnClickListener mSearchClick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            MenuDrawable drawable = (MenuDrawable) mToolbar.getNavigationIcon();
            SearchView view = mToolbar.getSearchView();
            Source source = getSource();

            if(!view.isSearchExpanded()) {
                // transform from hamburger to back button
                source.openSearchFragment(mFragmentManager);
            }else{
                if(TextUtils.isEmpty(view.getQuery())){
                    // transform from back button to hamburger
                    drawable.setRotation(0, true);
                    view.collapse();
                    forceBack();
                }

                view.setQuery("");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        WatchMeApplication application = WatchMeApplication.getApplication();
        application.inject(this);

        mFragmentManager = getSupportFragmentManager();

        setupToolbar();
        setupNavigationMenu();

        if(savedInstanceState != null){
            return;
        }

        if (!createdWithNonStandardWay()) {
            getSource().openDefaultFragment(mFragmentManager, false);
        }
    }

    /**
     * Checks {@link #getIntent()} if contains
     * {@link android.content.Intent#ACTION_VIEW} and not null
     * {@link android.content.Intent#getData()} creates by the not standard way
     *
     * @return true if created from outcome intent
     */
    boolean createdWithNonStandardWay(){
        Intent intent = getIntent();
        return Intent.ACTION_VIEW.equals(intent.getAction()) && !TextUtils.isEmpty(intent.getDataString())
                && catchedByOriginalSource(intent);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(Intent.ACTION_VIEW.equals(intent.getAction()) &&
                !TextUtils.isEmpty(intent.getDataString())) {
            catchedByOriginalSource(intent);
        }
    }

    boolean catchedByOriginalSource(Intent intent){
        WatchMeApplication application = WatchMeApplication.getApplication();
        Uri unknownSourceUrl = intent.getData();
        List<Source> sources = application.getSources();

        for(Source source : sources){
            if(source.onIntentCatched(this, mJobManager, unknownSourceUrl)){
                //TODO set as default source?
                return true;
            }
        }

        return false;
    }

    /**
     * Block navigation drawer, if true user can't use
     * swipe to show navigation menu
     *
     * @param lockMode lock or node
     */
    @Override
    public void setNavigationLockMode(boolean lockMode){
        mNavigationLayout.setDrawerLockMode(lockMode ?  DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                : DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private void setupToolbar(){
        int defaultColor = ThemeUtils.getThemeColor(this, R.attr.toolbarActionsPrimaryColor);

        MenuDrawable hamburger = new MenuDrawable(this, defaultColor);
        mToolbar.setNavigationIcon(hamburger);
        mToolbar.setOnNavigationClickListener(mHamburgerClick);

        SearchView searchButton = mToolbar.getSearchView();
        searchButton.setIconsTint(defaultColor);
        searchButton.setHintTextColor(defaultColor);
        searchButton.setOnSearchClickListener(mSearchClick);
    }

    private void setupNavigationMenu(){
        mNavigationLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if(mNavigationLayout.isDrawerOpen(GravityCompat.START)){
            mNavigationLayout.closeDrawer(GravityCompat.START);
            return;
        }

        super.onBackPressed();
    }

    /**
     * @return toolbar of activity
     */
    public Toolbar getToolbar(){
        return mToolbar;
    }

    /**
     * @return parent view of {@link #getToolbar()}
     */
    public ViewGroup getToolbarWrapper(){
        return mToolbarWrapper;
    }

}
