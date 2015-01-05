package io.codetail.dependecy;

import dagger.Module;
import io.codetail.LauncherActivity;
import io.codetail.WatchMeActivity;
import io.codetail.adapters.WatchMeAdapterNew;
import io.codetail.dependecy.library.ContextProvider;
import io.codetail.dependecy.library.NetworkProvider;
import io.codetail.dependecy.library.UtilsProvider;
import io.codetail.fragments.BaseWatchMeFragment;
import io.codetail.fragments.NavigationFragment;
import io.codetail.fragments.mover.CategoryFragment;
import io.codetail.fragments.mover.SearchFragment;
import io.codetail.utils.ScrollManager;

@Module(
        injects = {
                BaseWatchMeFragment.class,
                CategoryFragment.class,
                NavigationFragment.class,
                SearchFragment.class,

                WatchMeAdapterNew.class,
                ScrollManager.class,

                LauncherActivity.class,
                WatchMeActivity.class,
        },
        includes = {
                UtilsProvider.class,
                ContextProvider.class,
                NetworkProvider.class
        }
)
public class UiModule {
}
