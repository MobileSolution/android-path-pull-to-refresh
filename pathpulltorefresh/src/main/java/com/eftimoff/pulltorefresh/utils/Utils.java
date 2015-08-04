package com.eftimoff.pulltorefresh.utils;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.AbsListView;

/**
 * TODO Add Javadoc.
 * <p/>
 * Created by georgi.eftimov on 28/07/2015.
 */
public class Utils {

    /**
     * TODO Add Javadoc.
     */
    public static int convertDpToPixel(final Context context, final int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    /**
     * Determine if the View can scroll vertically up.
     */
    public static boolean canChildScrollUp(final View target) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (target instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) target;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return target.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(target, -1);
        }
    }
}
