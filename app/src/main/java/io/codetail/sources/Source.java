package io.codetail.sources;

import android.accounts.Account;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.FragmentManager;

import io.codetail.WatchMeActivity;
import io.codetail.client.auth.Authenticator;
import io.codetail.fragments.BaseWatchMeFragment;
import io.codetail.work.JobManager;

public abstract class Source {

    /**
     * @return the default source to use
     */
    public static Source getDefaultSource(){
        return new MoverSource();
    }

    /**
     * Invoked from {@link android.accounts.AbstractAccountAuthenticator}
     * to retrieve valid token for {@link android.accounts.Account}
     *
     * @param account Account requested to auth token
     * @param password Account password
     *
     * @return valid authentication token, can be null
     */
    public abstract String getAuthToken(Account account, String password);

    /**
     * @return Ask the authenticator for a localized label for the given authTokenType.
     * {@link android.accounts.AbstractAccountAuthenticator#getAuthTokenLabel(String)}
     */
    public abstract String getLabel();

    /**
     * TODO
     *
     * @return authenticator
     */
    public abstract Authenticator getAuthenticator();

    /**
     * @param token auth token
     * @return true if given token is valid and can be used
     */
    public abstract boolean isValidAuthToken(String token);

    /**
     * @return categories or any navigation items to show
     */
    public abstract CharSequence[] getNavigationItems();

    /**
     * @return navigation icons to show
     */
    public abstract Drawable[] getNavigationIcons();

    /**
     * Source id name
     * example: example.com id will be com.example
     *
     * @return source id
     */
    public abstract String getSourceId();

    /**
     * On new navigation item selected from {@link io.codetail.fragments.NavigationFragment}
     *
     * @param position position of new selected item
     */
    public abstract void onNavigationItemSelected(FragmentManager manager, int position);

    /**
     * Open default fragment
     *
     * @return default fragment
     */
    public abstract BaseWatchMeFragment openDefaultFragment(FragmentManager manager, boolean addToBackStack);

    /**
     * Open search fragment
     *
     * @return search fragment
     */
    public abstract BaseWatchMeFragment openSearchFragment(FragmentManager manager);

    /**
     * Invoked when new external intent available and has
     * {@link android.content.Intent#ACTION_VIEW},
     * {@link android.content.Intent#CATEGORY_BROWSABLE}
     * flags and not empty {@link android.content.Intent#getData()}
     *
     * Return true if you handled it
     *
     * @return true if this source can handle intercepted intent
     */
    public abstract boolean onIntentCatched(WatchMeActivity activity, JobManager manager, Uri uri);

}

