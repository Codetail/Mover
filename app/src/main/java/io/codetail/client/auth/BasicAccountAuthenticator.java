package io.codetail.client.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.List;

import hugo.weaving.DebugLog;
import io.codetail.WatchMeApplication;
import io.codetail.sources.Source;

import static android.accounts.AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE;
import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_ERROR_CODE;
import static android.accounts.AccountManager.KEY_INTENT;
import static android.accounts.AccountManager.get;
import static io.codetail.fragments.NavigationFragment.USER_PICTURE_URL;


public class BasicAccountAuthenticator extends AbstractAccountAuthenticator{

    public final static String ACCOUNT_TYPE = "codetail.auth.WATCH_ME";

    public final static int ERROR_CODE_INVALID_USER_DATA = 100;

    /**
     * Given authTokenType is not found in {@link io.codetail.WatchMeApplication#getSources()}
     */
    public final static int INVALID_AUTH_TOKEN_TYPE = 404;

    /**
     *  Activity Action: Display form to authenticate user. Used by
     *  {@link android.accounts.AccountManager#addAccount(String, String, String[], android.os.Bundle, android.app.Activity, android.accounts.AccountManagerCallback, android.os.Handler)}
     *  to authorize user into service
     */
    public final static String ACTION_AUTHENTICATE = "codetail.intent.action.AUTHENTICATE";

    /**
     * Activity Action: Provide user to edit account properties. Used by
     * {@link android.accounts.AccountManager#editProperties(String, android.app.Activity, android.accounts.AccountManagerCallback, android.os.Handler)}
     */
    public final static String ACTION_EDIT_ACCOUNT_PROPERTIES = "codetail.intent.action.EDIT_ACCOUNT_PROPERTIES";

    /**
     * Activity Action: Let user check his credentials
     */
    public final static String ACTION_CONFIRM_CREDENTIALS = "codetail.intent.action.CONFIRM_CREDENTIALS";

    /**
     * Parcelable data {@link android.accounts.Account}
     */
    public final static String EXTRA_ACCOUNT = "codetail.intent.extra.ACCOUNT";

    /**
     * String data used with {@link #ACTION_AUTHENTICATE} contains authorize service name
     */
    public final static String EXTRA_AUTH_TOKEN_TYPE = "codetail.intent.extra.AUTH_TOKEN_TYPE";

    /**
     * a String array of authenticator specific features that added account must support
     * may be null
     */
    public final static String EXTRA_REQUIRED_FEATURES = "codetail.intent.extra.REQUIRED_FEATURES";

    AccountManager mAccountManager;
    Bundle mExtras;

    public BasicAccountAuthenticator(Context context) {
        super(context);
        mExtras = new Bundle();
        mAccountManager = get(context);
    }

    @Override
    @DebugLog
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        mExtras.clear();
        mExtras.putParcelable(KEY_INTENT, new Intent(ACTION_EDIT_ACCOUNT_PROPERTIES));
        return mExtras;
    }

    @Override
    @DebugLog
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        mExtras.clear();

        Intent intent = new Intent(ACTION_AUTHENTICATE)
                .putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                .putExtra(KEY_ACCOUNT_TYPE, accountType)
                .putExtra(EXTRA_AUTH_TOKEN_TYPE, authTokenType)
                .putExtra(EXTRA_REQUIRED_FEATURES, requiredFeatures)
                .putExtras(options);

        mExtras.putParcelable(KEY_INTENT, intent);
        return mExtras;
    }

    @Override
    @DebugLog
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        Intent intent = new Intent(ACTION_CONFIRM_CREDENTIALS)
                .putExtra(EXTRA_ACCOUNT, account)
                .putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                .putExtras(options);

        mExtras.clear();
        mExtras.putParcelable(KEY_INTENT, intent);
        return mExtras;
    }

    @Override
    @DebugLog
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        mExtras.clear();

        Source source = getSource(authTokenType);

        if(source == null){
            mExtras.putInt(KEY_ERROR_CODE, INVALID_AUTH_TOKEN_TYPE);
            return mExtras;
        }

        AccountManager manager = mAccountManager;
        String authToken = mAccountManager.peekAuthToken(account, authTokenType);

        if(!source.isValidAuthToken(authToken)){
            Bundle bundle = source.getAuthenticator().getAuthToken(manager, account);

            authToken = bundle.getString(KEY_AUTHTOKEN);

            if(bundle.containsKey(USER_PICTURE_URL)){
                manager.setUserData(account, USER_PICTURE_URL, bundle.getString(USER_PICTURE_URL));
            }
        }

        if(TextUtils.isEmpty(authToken)){
            Intent intent = new Intent(ACTION_AUTHENTICATE)
                    .putExtra(EXTRA_ACCOUNT, account)
                    .putExtra(EXTRA_AUTH_TOKEN_TYPE, authTokenType)
                    .putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                    .putExtras(options);

            mExtras.putParcelable(KEY_INTENT, intent);
        }else{
            mExtras.putString(KEY_AUTHTOKEN, authToken);
            mExtras.putString(KEY_ACCOUNT_NAME, account.name);
            mExtras.putString(KEY_ACCOUNT_TYPE, account.type);
            mExtras.putString(EXTRA_AUTH_TOKEN_TYPE, authTokenType);
        }

        return mExtras;
    }

    @Override
    @DebugLog
    public String getAuthTokenLabel(String authTokenType) {
        return getSource(authTokenType).getLabel();
    }

    @Override
    @DebugLog
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    @DebugLog
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }

    Source getSource(String accountType){
        List<Source> sources = WatchMeApplication.getApplication().getSources();
        for(Source source: sources){
            if(source.getSourceId().equals(accountType)){
                return source;
            }
        }

        return null;
    }
}
