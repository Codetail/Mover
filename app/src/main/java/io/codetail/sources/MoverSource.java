package io.codetail.sources;

import android.accounts.Account;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.telly.mrvector.MrVector;

import java.util.List;

import codetail.utils.ResourceUtils;
import io.codetail.Constants;
import io.codetail.WatchMeActivity;
import io.codetail.client.auth.Authenticator;
import io.codetail.client.mover.MoverAuthenticator;
import io.codetail.client.mover.MoverService;
import io.codetail.fragments.BaseWatchMeFragment;
import io.codetail.fragments.mover.CategoryFragment;
import io.codetail.fragments.mover.SearchFragment;
import io.codetail.watchme.R;
import io.codetail.work.JobManager;
import retrofit.RestAdapter;

public final class MoverSource extends Source{

    public static MoverService sService;

    final static String CATEGORY_FRAGMENT = "moverCategoryFragment";
    final static String SEARCH_FRAGMENT = "moverSearchFragment";

    CharSequence[] mNavigationCategories;
    Drawable[] mNavigationDrawables;

    MoverAuthenticator mAuthenticator;

    public MoverSource() {
        sService = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setEndpoint(MoverService.ENDPOINT)
                .setClient(Constants.getOkClient())
                .build()
                .create(MoverService.class);

        mNavigationCategories = null;
        mNavigationDrawables = null;

        mAuthenticator = new MoverAuthenticator(sService);
    }

    @Override
    public String getSourceId() {
        return "codetail.auth.UZ_MOVER";
    }

    @Override
    public String getAuthToken(Account account, String password) {
        return null;
    }

    @Override
    public String getLabel() {
        return ResourceUtils.getString(R.string.uz_mover_source_label);
    }

    @Override
    public Authenticator getAuthenticator() {
        return mAuthenticator;
    }

    @Override
    public boolean isValidAuthToken(String token) {
        return false;
    }

    @Override
    public CharSequence[] getNavigationItems() {
        if(mNavigationCategories == null) {
            mNavigationCategories = ResourceUtils.getTextArray(R.array.main_categories_list);
        }

        return mNavigationCategories;
    }

    @Override
    public Drawable[] getNavigationIcons() {
        if(mNavigationDrawables == null){
            Resources resources = ResourceUtils.getResources();
            TypedArray array = resources.obtainTypedArray(R.array.mover_categories_icons);
            final int length = array.length();

            Drawable[] drawables = new Drawable[length];
            for(int index = 0; index < length; index++){
                drawables[index] = MrVector.inflate(resources, array.getResourceId(index, -1));
            }
            array.recycle();
            mNavigationDrawables = drawables;
        }

        return mNavigationDrawables;
    }

    @Override
    public void onNavigationItemSelected(FragmentManager manager, int position) {
        CategoryFragment fragment = (CategoryFragment) manager.findFragmentByTag(CATEGORY_FRAGMENT);
        if(fragment == null){
            throw new RuntimeException("Strange statement, CategoryFragment is not in fragments stack");
        }

        if(fragment.isDetached()){
            throw new IllegalStateException("CategoryFragment is detached");
        }

        fragment.setSelected(position);
    }

    @Override
    public BaseWatchMeFragment openDefaultFragment(FragmentManager manager, boolean addToBackStack) {
        if (addToBackStack) {
            return openFragment(manager, new CategoryFragment(), null, CATEGORY_FRAGMENT);
        }

        return openFragmentWithoutAddingBackStack(manager, new CategoryFragment(), null, CATEGORY_FRAGMENT);
    }

    @Override
    public BaseWatchMeFragment openSearchFragment(FragmentManager manager) {
        return openFragment(manager, new SearchFragment(), null, SEARCH_FRAGMENT);
    }

    @Override
    public boolean onIntentCatched(WatchMeActivity activity, JobManager manager, Uri url) {
        if(!"mover.uz".equals(url.getHost()) && !"www.mover.uz".equals(url.getHost())){
            return false;
        }

        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        List<String> pathSegments = url.getPathSegments();
        String fragment2open = pathSegments.size() >= 1 ? pathSegments.get(0) : "doNothing";

        if(pathSegments.size() == 0){
            fragment2open = "home";
        }

        Bundle arguments = new Bundle();
        String tagName;
        BaseWatchMeFragment target;

        switch (fragment2open){
            case "search":
                target = new SearchFragment();
                tagName = SEARCH_FRAGMENT;

                String query = url.getQueryParameter("val");
                int pageNumber = getPageNumber(url.getQueryParameter("page"));
                //TODO support search filters

                arguments.putString(SearchFragment.SEARCH_QUERY, query);
                arguments.putInt(SearchFragment.SEARCH_CURRENT_PAGE, pageNumber);
                break;

            case "watch":
                //FIXME temporary decision
                target = new CategoryFragment();
                tagName = CATEGORY_FRAGMENT;

                arguments.putString("watchId", pathSegments.get( pathSegments.size() - 1 ));
                break;

            case "video":
                target = new CategoryFragment();
                tagName = CATEGORY_FRAGMENT;

                String category = pathSegments.get(1);

                pageNumber = getPageNumber(url.getQueryParameter("page"));

                arguments.putString(CategoryFragment.SELECTED_CATEGORY, category);
                arguments.putInt(CategoryFragment.CURRENT_PAGE, pageNumber);
                break;

            case "home":
                target = new CategoryFragment();
                tagName = CATEGORY_FRAGMENT;
                arguments = null;
                break;

            case "doNothing": // Unsupported link
            default:
                return false;
        }

        openFragment(fragmentManager, target, arguments, tagName);
        return true;
    }

    /**
     * Replace current fragment with new one,
     * plus adds it to back stack, then commit
     *
     * @param manager Fragment manager
     * @param fragment fragment to open
     * @param args target arguments
     * @param name target id
     *
     * @return target
     */
    private BaseWatchMeFragment openFragment(FragmentManager manager,
                                     BaseWatchMeFragment fragment,
                                     Bundle args,  String name){
        if(args != null){
            fragment.setArguments(args);
        }

        manager.beginTransaction()
                .replace(R.id.container, fragment, name)
                .addToBackStack(name)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        return fragment;
    }

    /**
     * Replace current fragment with new one then commit
     *
     * @param manager Fragment manager
     * @param fragment fragment to open
     * @param args target arguments
     * @param name target id
     *
     * @return target
     */
    private BaseWatchMeFragment openFragmentWithoutAddingBackStack(FragmentManager manager,
                                     BaseWatchMeFragment fragment,
                                     Bundle args,  String name){
        if(args != null){
            fragment.setArguments(args);
        }

        manager.beginTransaction()
                .replace(R.id.container, fragment, name)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        return fragment;
    }

    /**
     * Parse string to find integers
     *
     * @param value Destination of integers
     * @return parsed integers in given argument
     */
    static int getPageNumber(String value){
        int pageNumber = 1;
        if(!TextUtils.isEmpty(value)){
            pageNumber = Integer.getInteger(value, pageNumber);
        }
        return pageNumber;
    }
}
