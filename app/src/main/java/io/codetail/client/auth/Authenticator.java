package io.codetail.client.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;

public interface Authenticator {

    public CharSequence getHeadline();

    public CharSequence getDescription();

    public void login(Account account, String password);

    public Bundle getAuthToken(AccountManager manager, Account account);

    public void logout(Account account);

    public static class AuthenticationEvent{

        private final boolean mSuccess;
        private final Bundle mResult;

        public AuthenticationEvent(boolean success, Bundle result) {
            mSuccess = success;
            mResult = result;
        }

        public boolean isSuccess(){
            return mSuccess;
        }

        public Bundle getResult(){
            return mResult;
        }

    }

}
