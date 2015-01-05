package io.codetail.work.di;

import io.codetail.work.BaseJob;
import io.codetail.work.Job;

/**
 * interface that can be provided to {@link io.codetail.work.JobManager} for dependency injection
 * it is called before the job's onAdded method is called. for persistent jobs, also run after job is brought
 * back from disk.
 */
public interface DependencyInjector {
    public void inject(BaseJob job);
}
