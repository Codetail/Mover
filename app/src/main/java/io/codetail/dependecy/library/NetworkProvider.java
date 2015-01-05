package io.codetail.dependecy.library;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.codetail.Constants;
import io.codetail.client.mover.MoverService;
import io.codetail.sources.MoverSource;
import retrofit.client.OkClient;

@Module(
        library = true
)
public class NetworkProvider {

    @Provides @Singleton
    public OkHttpClient provideOkHttpClient(){
        return Constants.getOkHttpClient();
    }

    @Provides @Singleton
    public OkClient provideOkClient(OkHttpClient client){
        return Constants.getOkClient();
    }

    @Provides @Singleton
    public MoverService provideMoverService(OkClient client){
        return MoverSource.sService;
    }

}
