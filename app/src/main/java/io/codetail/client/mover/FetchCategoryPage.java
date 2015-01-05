package io.codetail.client.mover;

import javax.inject.Inject;

import io.codetail.client.BaseNetworkJob;
import io.codetail.client.State;
import io.codetail.work.Params;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class FetchCategoryPage extends BaseNetworkJob<Mover>{

    String mCategory;
    int mPageNumber;

    @Inject
    MoverService mService;

    public FetchCategoryPage(String category, int pageNum){
        this(category, pageNum,
                new Params(PRIORITY_LOW)
                .groupBy("categoryPages")
                .requireNetwork()
        );
    }

    public FetchCategoryPage(String category, int pageNum, Params params) {
        super(params);

        mPageNumber = pageNum;
        mCategory = category;
    }

    @Override
    public void onPreRunning() {
        getEventBus().post(new State.OnStartLoadingPage(mPageNumber));
    }

    @Override
    public Mover doBackgroundJob() {
        Response response;

        /**
         * NOTES
         *
         * if category page equals empty quotes("") it's means
         * that selected page is category_home page :)
         *
         */

        if(mCategory.equals("") && mPageNumber < 2){
            response = mService.home();
        }else if(mCategory.equals("") && mPageNumber >= 2){
            response = mService.home(mPageNumber);
        }else{
            response = mService.category(mCategory, mPageNumber);
        }

        if(response.getStatus() >= 200 && response.getStatus() <= 300) {

            byte[] data = ((TypedByteArray) response.getBody()).getBytes();
            String htmlSource = new String(data);

            if(mPageNumber > 1){
                return new Mover.PaginatedPage(mCategory, mPageNumber).from(htmlSource);
            }

            return new Mover.CategoryPage(mCategory).from(htmlSource);
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
