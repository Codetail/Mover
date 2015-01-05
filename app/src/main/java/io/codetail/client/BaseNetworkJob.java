package io.codetail.client;

import android.os.Handler;
import android.os.Looper;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;

import java.io.IOException;

import io.codetail.Constants;
import io.codetail.work.Job;
import io.codetail.work.Params;
import retrofit.RestAdapter;
import retrofit.client.Client;

public abstract class BaseNetworkJob<T> extends Job{

    public static int PRIORITY_LOW = 0;
    public static int PRIORITY_NORMAL = 1;
    public static int PRIORITY_HIGH = 2;

    private final static Handler sHandler = new Handler(Looper.getMainLooper());

    boolean isCancelled;

    public BaseNetworkJob(Params params) {
        super(params);
        isCancelled = false;
    }

    /**
     * when job running before doing hard work
     * update ui
     */
    public abstract void onPreRunning();

    /**
     * do any hard works
     *
     * @return result of job
     */
    public abstract T doBackgroundJob() throws IOException;

    /**
     * @param result of hard work network job
     */
    public abstract void onJobPostResult(T result);

    public boolean isCancelled(){
        return isCancelled;
    }

    @Override
    protected void onCancel() {
        isCancelled = true;
    }

    @Override
    public void onRun() throws Throwable {
        sHandler.post(new OnPreRunning(this));

        T result = doBackgroundJob();

        sHandler.post(new OnPostRunning<T>(result, this));
    }

    public OkHttpClient getClient(){
        return Constants.getOkHttpClient();
    }

    public Bus getEventBus(){
        return Constants.getEventBus();
    }

    public static <T> T create(String endpoint, Class<T> service, Client client){
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setClient(client)
                .build();

        return adapter.create(service);
    }

    private static class OnPreRunning implements Runnable{
        volatile BaseNetworkJob job;

        public OnPreRunning(BaseNetworkJob job) {
            this.job = job;
        }

        @Override
        public void run() {
            job.onPreRunning();
        }
    }

    private static class OnPostRunning<T> implements Runnable{
        volatile T object;
        volatile BaseNetworkJob<T> job;

        public OnPostRunning(T object, BaseNetworkJob<T> job) {
            this.object = object;
            this.job = job;
        }

        @Override
        public void run() {
            job.onJobPostResult(object);
        }
    }

}
