package io.codetail.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.squareup.otto.Bus;

import io.codetail.Constants;
import io.codetail.WatchMeActivity;
import io.codetail.WatchMeApplication;
import io.codetail.sources.Source;

public abstract class BaseWatchMeFragment extends Fragment{

    private WatchMeApplication mWatchMeApplication;

    Bus mEventBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWatchMeApplication = WatchMeApplication.getApplication();
        mEventBus = Constants.getEventBus();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mWatchMeApplication.inject(this);
    }

    public Bus getEventBus(){
        return mEventBus;
    }

    public Source getSource(){
        return mWatchMeApplication.getSelectedSource();
    }

    public boolean onBackPressed(){
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        mEventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mEventBus.unregister(this);
    }

    public WatchMeActivity getWatchMeActivity(){
        return (WatchMeActivity) getActivity();
    }

    public WatchMeApplication getWatchMeApplication() {
        return mWatchMeApplication;
    }
}
