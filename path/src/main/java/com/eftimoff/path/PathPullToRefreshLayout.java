package com.eftimoff.path;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.eftimoff.androipathview.PathView;
import com.eftimoff.pulltorefresh.BasePullToRefreshLayout;
import com.eftimoff.pulltorefresh.listeners.OnPullToRefreshListener;


public class PathPullToRefreshLayout extends BasePullToRefreshLayout {

    private PathView pathView;

    public PathPullToRefreshLayout(final Context context) {
        super(context);
    }

    public PathPullToRefreshLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setOnPullToRefreshListener(new OnPullToRefreshListener() {
            @Override
            public void onStart() {
                Log.i("!!!!!!!!!!!!!!!!!!!!!", "onStart");
            }

            @Override
            public void onPercent(final float percent, final boolean invalidate) {
                pathView.setPercentage(percent);
                Log.i("!!!!!!!!!!!!!!!!!!!!!", "onPercent : " + percent + " " + invalidate);
            }

            @Override
            public void onStop() {
                pathView.setFillAfter(true);
                Log.i("!!!!!!!!!!!!!!!!!!!!!", "onStop");
            }
        });
    }

    @Override
    public View getRefreshView() {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.example_paths, null, false);
        pathView = (PathView) view.findViewById(R.id.pathView);
        return view;
    }

    @Override
    public int getDragMaxDistance() {
        return 150;
    }

    @Override
    public boolean parallax() {
        return false;
    }
}
