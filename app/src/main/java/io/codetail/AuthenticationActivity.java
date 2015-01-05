package io.codetail;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import codetail.text.MaterialEditText;
import codetail.widget.Toolbar;
import io.codetail.client.auth.Authenticator;
import io.codetail.sources.Source;
import io.codetail.watchme.R;

import static android.accounts.AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_ERROR_CODE;
import static io.codetail.client.auth.Authenticator.AuthenticationEvent;
import static io.codetail.client.auth.BasicAccountAuthenticator.ACTION_AUTHENTICATE;
import static io.codetail.client.auth.BasicAccountAuthenticator.ERROR_CODE_INVALID_USER_DATA;
import static io.codetail.fragments.NavigationFragment.USER_PICTURE_URL;

public class AuthenticationActivity extends WatchMeActivity{

    Bus mEventBus;

    @InjectView(R.id.username)
    MaterialEditText mUsernameField;

    @InjectView(R.id.password)
    MaterialEditText mPasswordField;

    @InjectView(R.id.login)
    View mSignIn;

    @InjectView(R.id.headline)
    TextView mHeadline;

    @InjectView(R.id.description)
    TextView mDescription;

    Authenticator mAuthenticator;
    AccountAuthenticatorResponse mAuthenticatorResponse;
    String mSourceId;

    Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_auth);
        ButterKnife.inject(this);
        mEventBus = Constants.getEventBus();

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if(!ACTION_AUTHENTICATE.equals(action)){
            throw new RuntimeException("Only authentication supported");
        }

        mAuthenticatorResponse = intent.getParcelableExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        mSourceId = intent.getStringExtra(KEY_ACCOUNT_TYPE);

        if(mSourceId == null){
            throw new NullPointerException("Account type is not given");
        }

        List<Source> sources = WatchMeApplication.getApplication().getSources();
        for(Source source : sources){
            if(source.getSourceId().equals(mSourceId)){
                mAuthenticator = source.getAuthenticator();
            }
        }

        if(mAuthenticator == null){
            throw new NullPointerException(String.format("Source for %s not found", mSourceId));
        }

        if(mAuthenticatorResponse != null){
            mAuthenticatorResponse.onRequestContinued();
        }

        mHeadline.setText(mAuthenticator.getHeadline());
        mDescription.setText(mAuthenticator.getDescription());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mEventBus.register(this);
    }

    @OnClick(R.id.login)
    public void canLogin(){
        if(!mUsernameField.isCharactersCountValid()){
            mUsernameField.setError(getString(R.string.sign_in_field_must_be_more_than_4_chars));
        }

        if(!mPasswordField.isCharactersCountValid()){
            mPasswordField.setError(getString(R.string.sign_in_field_must_be_more_than_4_chars));
        }

        if(mUsernameField.isCharactersCountValid() && mPasswordField.isCharactersCountValid()){
            mUsernameField.setError(null);
            mPasswordField.setError(null);

            tryAuthenticateUser();
        }

    }

    public void tryAuthenticateUser(){
        setLockForm(true);

        String username = mUsernameField.getText().toString();
        String password = mPasswordField.getText().toString();

        mAccount = new Account(username,  mSourceId);

        mAuthenticator.login(mAccount, password);
    }

    @Subscribe
    public void onEvent(AuthenticationEvent event){
        Bundle result = event.getResult();
        setLockForm(false);

        if(event.isSuccess()){
            String password = mPasswordField.getText().toString();
            AccountManager manager = AccountManager.get(this);

            Bundle userData = new Bundle();
            userData.putString(USER_PICTURE_URL, result.getString(USER_PICTURE_URL));

            manager.addAccountExplicitly(mAccount, password, userData);
            manager.setAuthToken(mAccount, mSourceId, result.getString(KEY_AUTHTOKEN));

            mAuthenticatorResponse.onResult(result);
            mAuthenticatorResponse = null;

            setResult(RESULT_OK);
            finish();

        }else{
            int errorCode = result.getInt(KEY_ERROR_CODE);

            if(errorCode == ERROR_CODE_INVALID_USER_DATA){
                mUsernameField.setError(getString(R.string.sign_in_failed_with_wrong_data_username));
                mPasswordField.setError(getString(R.string.sign_in_failed_with_wrong_data_password));
            }
        }
    }

    void setLockForm(boolean lockForm){
        mUsernameField.setEnabled(!lockForm);
        mPasswordField.setEnabled(!lockForm);

        mSignIn.setEnabled(!lockForm);
    }

    public void cancel(){
        mAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED, "cancelled");
        mAuthenticatorResponse = null;
        setResult(RESULT_CANCELED);
        super.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ButterKnife.reset(this);
        mEventBus.unregister(this);
    }

    @Override
    public void onBackPressed() {
        cancel();
    }

    @Override
    public void setNavigationLockMode(boolean lockMode) {
        throw new UnsupportedOperationException("Lock mode is unsupported");
    }

    @Override
    public Toolbar getToolbar() {
        throw new UnsupportedOperationException("No toolbar provided");
    }
}
