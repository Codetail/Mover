package io.codetail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import java.util.List;

import codetail.utils.ResourceUtils;
import codetail.widget.Toolbar;
import io.codetail.fragments.BaseWatchMeFragment;
import io.codetail.sources.Source;
import io.codetail.watchme.BuildConfig;

public abstract class WatchMeActivity extends ActionBarActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ResourceUtils.init(this);
        FragmentManager.enableDebugLogging(BuildConfig.EXTRAS_LOGGING);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ResourceUtils.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Block navigation drawer, if true user can't use
     * swipe to show navigation menu
     *
     * @param lockMode lock or node
     */
    public abstract void setNavigationLockMode(boolean lockMode);

    /**
     * @return current present of toolbar
     */
    public abstract Toolbar getToolbar();

    /**
     * @return Currently used source
     */
    public Source getSource(){
        return ((WatchMeApplication) getApplicationContext())
                        .getSelectedSource();
    }

    public void forceBack(){
        super.onBackPressed();
    }


    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        List<Fragment> fragments = manager.getFragments();
        BaseWatchMeFragment currentFragment = null;

        if(fragments == null){
            super.onBackPressed();
            return;
        }

        if(fragments.size() > 0){
            // current visible fragment
            currentFragment = (BaseWatchMeFragment) fragments.get(fragments.size() - 1);
        }

        if(currentFragment == null || !currentFragment.onBackPressed()){
            super.onBackPressed();
        }
    }

}
