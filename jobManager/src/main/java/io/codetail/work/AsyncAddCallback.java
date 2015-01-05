package io.codetail.work;


/**
 * If you are adding the job via the async adder, you can provide a callback method to receive the ID.
 * Please keep in mind that job manager will keep a strong reference to this callback. So if the callback is an
 * anonymous class inside an {@link android.app.Activity} context, it may leak the activity until the job is added.
 */
public interface AsyncAddCallback {
    public void onAdded(long jobId);
}
