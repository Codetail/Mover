package io.codetail.dependecy.library;


import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.codetail.Constants;
import io.codetail.dependecy.ApplicationContext;
import io.codetail.watchme.BuildConfig;
import io.codetail.work.JobManager;

@Module(
        includes = ContextProvider.class,
        library = true
)
public class UtilsProvider {

    @Provides @Singleton
    public Bus provideEventBus(){
        return Constants.getEventBus();
    }

    @Provides @Singleton
    public JobManager provideJobManager(@ApplicationContext Context context){
        return Constants.getJobManager();
    }

    @Provides @Singleton
    public Picasso providePicasso(@ApplicationContext Context context){
        return new Picasso.Builder(context)
                .loggingEnabled(BuildConfig.EXTRAS_LOGGING)
                .build();
    }
}
