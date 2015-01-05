package io.codetail.client.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BasicAuthService extends Service{

    BasicAccountAuthenticator mAuthenticator;

    public BasicAuthService(){

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenticator = new BasicAccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
