package com.eftimoff.pulltorefresh.listeners;

/**
 * Created by georgi.eftimov on 28/07/2015.
 */
public interface OnPullToRefreshListener {

    void onStart();

    void onPercent(final float percent, final boolean invalidate);

    void onStop();
}
