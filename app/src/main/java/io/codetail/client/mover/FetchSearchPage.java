package io.codetail.client.mover;

import javax.inject.Inject;

import io.codetail.client.BaseNetworkJob;
import io.codetail.client.State;
import io.codetail.work.Params;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class FetchSearchPage extends BaseNetworkJob<Mover>{

    String mQuery;
    int mPageNumber;

    @Inject
    MoverService mService;

    public FetchSearchPage(String query, int pageNum){
        this(query, pageNum,
                new Params(PRIORITY_LOW)
                .groupBy("categoryPages")
                .requireNetwork()
        );
    }

    public FetchSearchPage(String category, int pageNum, Params params) {
        super(params);

        mPageNumber = pageNum;
        mQuery = category;
    }

    @Override
    public void onPreRunning() {
        getEventBus().post(new State.OnStartLoadingPage(mPageNumber));
    }

    @Override
    public Mover doBackgroundJob() {
        /**
         * NOTES
         *
         * if category page equals empty quotes("") it's means
         * that selected page is category_home page :)
         *
         */

        Response response = mService.search(mQuery, mPageNumber);

        if(response.getStatus() >= 200 && response.getStatus() <= 300) {

            byte[] data = ((TypedByteArray) response.getBody()).getBytes();
            String htmlSource = new String(data);

            return new Mover.SearchPage(mQuery, mPageNumber).from(htmlSource);
        }

        throw RetrofitError.httpError(response.getUrl(), response, null, Response.class);
    }

    @Override
    public void onJobPostResult(Mover result) {
        if(!isCancelled()) {
            result.postEvent(getEventBus());
        }
    }

    @Override
    public void onAdded() {}

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        android.util.Log.e("FetchCategoryPage", throwable.toString());
        return false;
    }

}
