package com.eftimoff.pulltorefresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.eftimoff.androipathview.PathView;
import com.eftimoff.pulltorefresh.listeners.OnPullToRefreshListener;
import com.eftimoff.pulltorefresh.utils.Utils;


public class PathPullToRefreshLayout extends BasePullToRefreshLayout {

    private static final int MAX_DISTANCE = 150;

    private PathView pathView;

    //attributes.
    private int svgResourceId;
    private int pathWidth;
    private int pathColor;
    private int backgroundColor;

    public PathPullToRefreshLayout(final Context context) {
        this(context, null);
    }

    public PathPullToRefreshLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    private void setup(final Context context, final AttributeSet attrs) {

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PathPullToRefreshLayout, 0, 0);
        try {
            svgResourceId = typedArray.getResourceId(R.styleable.PathPullToRefreshLayout_pptrSvgResourceId, 0);
            setSvgResourceId(svgResourceId);

            pathWidth = typedArray.getInteger(R.styleable.PathPullToRefreshLayout_pptrPathWidth, 1);
            setPathWidth(pathWidth);

            pathColor = typedArray.getColor(R.styleable.PathPullToRefreshLayout_pptrPathColor, Color.WHITE);
            setPathColor(pathColor);

            backgroundColor = typedArray.getColor(R.styleable.PathPullToRefreshLayout_pptrBackgroundColor, Color.LTGRAY);
            setBackgroundAColor(backgroundColor);
        } finally {
            typedArray.recycle();
        }
        setOnPullToRefreshListener(onPullToRefreshListener);
    }

    @Override
    public View getRefreshView() {
        pathView = new PathView(getContext());
        pathView.setFillAfter(true);
        final int width = Utils.convertDpToPixel(getContext(), MAX_DISTANCE);
        pathView.setLayoutParams(new LinearLayout.LayoutParams(width, width));

        final LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, width));
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.addView(pathView);
        return linearLayout;
    }

    @Override
    public int getDragMaxDistance() {
        return MAX_DISTANCE;
    }

    @Override
    public boolean parallax() {
        return false;
    }

    private OnPullToRefreshListener onPullToRefreshListener = new OnPullToRefreshListener() {
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
            Log.i("!!!!!!!!!!!!!!!!!!!!!", "onStop");
        }
    };

    private void setSvgResourceId(final int svgResourceId) {
        this.svgResourceId = svgResourceId;
        pathView.setSvgResource(svgResourceId);
    }

    public int getSvgResourceId() {
        return svgResourceId;
    }

    public int getPathWidth() {
        return pathWidth;
    }

    public void setPathWidth(final int pathWidth) {
        this.pathWidth = pathWidth;
        pathView.setPathWidth(pathWidth);
    }

    public int getPathColor() {
        return pathColor;
    }

    public void setPathColor(final int pathColor) {
        this.pathColor = pathColor;
        pathView.setPathColor(pathColor);
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundAColor(final int backgroundColor) {
        this.backgroundColor = backgroundColor;
        setBackgroundColor(backgroundColor);
    }
}
