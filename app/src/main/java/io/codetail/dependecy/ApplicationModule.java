package io.codetail.dependecy;

import dagger.Module;
import io.codetail.WatchMeApplication;
import io.codetail.dependecy.library.UtilsProvider;

@Module(
        injects = WatchMeApplication.class,
        includes = UtilsProvider.class
)
public class ApplicationModule {
}
