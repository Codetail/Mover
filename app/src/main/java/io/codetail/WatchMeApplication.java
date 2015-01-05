package io.codetail;

import android.app.Application;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;
import io.codetail.dependecy.ApplicationModule;
import io.codetail.dependecy.UiModule;
import io.codetail.dependecy.WorkerModule;
import io.codetail.dependecy.library.ContextProvider;
import io.codetail.sources.Source;
import io.codetail.watchme.BuildConfig;
import io.codetail.work.BaseJob;
import io.codetail.work.di.DependencyInjector;


public class WatchMeApplication extends Application implements DependencyInjector{

    ObjectGraph mObjectGraph;

    public final static String PICASSO_INSTANCE = "PicassoInstance";

    private static WatchMeApplication sApplicationInstance;

    private Source mSelectedSource;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplicationInstance = this;

        Constants.init(sApplicationInstance);
        mSelectedSource = Source.getDefaultSource();

        mObjectGraph = ObjectGraph.create(
                new ContextProvider(this),
                new ApplicationModule(),
                new WorkerModule(),
                new UiModule()
        );
    }

    public static WatchMeApplication getApplication(){
        return sApplicationInstance;
    }

    public List<Source> getSources(){
        return Arrays.asList(mSelectedSource);
    }

    public Source getSelectedSource(){
        return mSelectedSource;
    }

    public void inject(Object object){
        mObjectGraph.inject(object);
    }

    @Override
    public void inject(BaseJob job) {
        mObjectGraph.inject(job);
    }
}
