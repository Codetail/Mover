package io.codetail.client.mover;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.text.Html;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;

import codetail.utils.ResourceUtils;
import io.codetail.Constants;
import io.codetail.WatchMeApplication;
import io.codetail.client.BaseNetworkJob;
import io.codetail.client.auth.Authenticator;
import io.codetail.watchme.R;
import io.codetail.work.Params;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

import static android.accounts.AccountManager.ERROR_CODE_NETWORK_ERROR;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_ERROR_CODE;
import static io.codetail.client.auth.BasicAccountAuthenticator.ERROR_CODE_INVALID_USER_DATA;
import static io.codetail.fragments.NavigationFragment.USER_PICTURE_URL;

public class MoverAuthenticator implements Authenticator {
    MoverService mService;

    Account mAccount;
    String mPassword;
    boolean mSigning;

    public MoverAuthenticator(MoverService service){
        mService = service;
    }

    @Override
    public CharSequence getHeadline() {
        return ResourceUtils.getString(R.string.mover_auth_headline);
    }

    @Override
    public CharSequence getDescription() {
        return Html.fromHtml(ResourceUtils.getString(R.string.mover_auth_description));
    }

    @Override
    public void login(Account account, String password) {
        mAccount = account;
        mPassword = password;
        mSigning = true;

        Constants.getJobManager().addJob(new AuthorizeJob(this));
    }

    @Override
    public void logout(Account account) {

    }

    @Override
    public Bundle getAuthToken(AccountManager manager, Account account) {
        if(!mSigning){
            mPassword = manager.getPassword(account);
        }

        Bundle result = new Bundle();
        try {
            Response response = mService.signIn(account.name, mPassword);

            if (isResponseSuccess(response)) {
                JSONObject json = asJson(asString(response));

                if (json == null || json.has("errors") || json.has("error")) {
                    result.putInt(KEY_ERROR_CODE, ERROR_CODE_INVALID_USER_DATA);
                    return result;
                }

                for (Header header : response.getHeaders()) {
                    if (isCookiesHeader(header)) {
                        String cookies = header.getValue();
                        String token = findToken(HttpCookie.parse(cookies));

                        if (token != null) {
                            result.putString(KEY_AUTHTOKEN, token);
                            result.putString(USER_PICTURE_URL, findUserImage(mService.channel(account.name)));
                        }
                    }
                }
            } else {
                result.putInt(KEY_ERROR_CODE, ERROR_CODE_NETWORK_ERROR);
            }
        }catch (RetrofitError error){
            result.putInt(KEY_ERROR_CODE, ERROR_CODE_NETWORK_ERROR);
        }

        return result;
    }

    boolean isResponseSuccess(Response response){
        return response.getStatus() >= 200 && response.getStatus() < 300;
    }

    private String findUserImage(Response response){
        try {
            if (isResponseSuccess(response)) {
                return Jsoup.parse(asString(response))
                        .select("#channel-box .userpic img").attr("src");
            }
        }catch (RetrofitError er){
            return  null;
        }

        return null;
    }

    private JSONObject asJson(String input){
        try {
            return new JSONObject(input);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String asString(Response body){
        return new String(((TypedByteArray) body.getBody()).getBytes());
    }

    private String findToken(List<HttpCookie> cookies){
        for(HttpCookie cookie : cookies){
            if(cookie.getName().equalsIgnoreCase("auth_sess")){
                return cookie.getValue();
            }
        }

        return null;
    }

    private boolean isCookiesHeader(Header header){
        return header.getName() != null && header.getName().equalsIgnoreCase("set-cookie");
    }

    public static class AuthorizeJob extends BaseNetworkJob<Bundle>{

        MoverAuthenticator mAuth;

        public AuthorizeJob(MoverAuthenticator auth) {
            super(new Params(PRIORITY_HIGH)
                    .requireNetwork()
                    .groupBy("auth"));

            mAuth = auth;
        }

        @Override
        public void onPreRunning() {

        }

        @Override
        public Bundle doBackgroundJob() throws IOException {
            AccountManager manager = AccountManager.get(WatchMeApplication.getApplication());
            return mAuth.getAuthToken(manager, mAuth.mAccount);
        }

        @Override
        public void onJobPostResult(Bundle result) {
            getEventBus().post(new AuthenticationEvent(result.getInt(KEY_ERROR_CODE) == 0, result));
        }

        @Override
        public void onAdded() {

        }

        @Override
        protected boolean shouldReRunOnThrowable(Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
