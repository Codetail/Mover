package io.codetail.dependecy;

import dagger.Module;
import io.codetail.client.BaseNetworkJob;
import io.codetail.client.mover.FetchAvailableVideoQualities;
import io.codetail.client.mover.FetchCategoryPage;
import io.codetail.client.mover.FetchSearchPage;
import io.codetail.dependecy.library.NetworkProvider;
import io.codetail.dependecy.library.UtilsProvider;

import static io.codetail.client.mover.MoverAuthenticator.AuthorizeJob;

@Module(
        injects = {
                BaseNetworkJob.class,
                FetchCategoryPage.class,
                FetchSearchPage.class,
                FetchAvailableVideoQualities.class,
                AuthorizeJob.class
        },
        includes = {
                UtilsProvider.class,
                NetworkProvider.class,
        }
)
public class WorkerModule {

}
