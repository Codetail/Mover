package io.codetail;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;

import io.codetail.work.JobManager;
import io.codetail.work.config.Configuration;
import retrofit.client.OkClient;

public final class Constants {

    public static final int MOVER_VIDEO_TYPE = 0;

    public static OkHttpClient sOkHttpClient;
    public static OkClient sOkClient;
    public static Bus sEventBus;
    public static JobManager sJobManager;

    public static void init(Context context){
        //TODO initialize singletons or global instance of class
        Configuration config = new Configuration.Builder(context)
                .injector((WatchMeApplication) context)
                .build();

        sJobManager = new JobManager(context, config);

        sOkHttpClient = new OkHttpClient();
        sOkClient = new OkClient(sOkHttpClient);

        sEventBus = new Bus();
    }

    public static JobManager getJobManager(){
        return sJobManager;
    }

    public static OkHttpClient getOkHttpClient(){
        return sOkHttpClient;
    }

    public static Bus getEventBus(){
        return sEventBus;
    }

    public static OkClient getOkClient(){
        return sOkClient;
    }

}
