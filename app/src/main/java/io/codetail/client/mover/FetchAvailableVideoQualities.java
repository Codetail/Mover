package io.codetail.client.mover;


import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.codetail.client.BaseNetworkJob;
import io.codetail.work.Params;

public class FetchAvailableVideoQualities extends BaseNetworkJob<Mover.Suggestion> {

    private List<String> mQualities;

    String mId;

    public FetchAvailableVideoQualities(String id) {
        super(new Params(PRIORITY_LOW)
                .groupBy("availableVideoQualities")
                .requireNetwork());

        mQualities = new ArrayList<>();
        mQualities.add("b");
        mQualities.add("m");
        mQualities.add("s");

        mId = id;
    }

    @Override
    public void onPreRunning() {

    }

    @Override
    public Mover.Suggestion doBackgroundJob(){
        OkHttpClient client = new OkHttpClient();

        final int length = mQualities.size();
        for(int i = 0; i < length; i++){
            if(checkQuality(mId, mQualities.get(0), client)){
                break;
            }

            mQualities.remove(0);
        }

        return new Mover.Suggestion(mQualities, mId);
    }

    static <T> T last(List<T> items){
        return items.get(items.size() - 1);
    }

    boolean checkQuality(String id, String quality, OkHttpClient client){
        try {
            Response response = client.newCall(new Request.Builder()
                    .url(Mover.MoverVideo.createVideoLink(id, quality))
                    .get().build())
                    .execute();

            return response.isSuccessful();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onJobPostResult(Mover.Suggestion result) {
        android.util.Log.i("Suggestion", "income");
        result.postEvent(getEventBus());
    }

    @Override
    public void onAdded() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        throwable.printStackTrace();
        return false;
    }
}
