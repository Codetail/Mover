package io.codetail.dependecy.library;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.codetail.dependecy.ApplicationContext;

@Module(
        library = true
)
public class ContextProvider {
    private final Context mApplicationContext;

    public ContextProvider(Context context) {
        mApplicationContext = context;
    }


    @Provides @ApplicationContext
    public Context provideApplicationContext(){
        return mApplicationContext;
    }

    @Provides @Singleton
    public AccountManager provideAccountManager(){
        return AccountManager.get(mApplicationContext);
    }

    @Provides @Singleton
    public AssetManager provideAssetManager(){
        return mApplicationContext.getAssets();
    }

    @Provides
    public File getPrivateFilesDir(){
        return mApplicationContext.getFilesDir();
    }

}
