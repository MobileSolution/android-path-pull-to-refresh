package com.eftimoff.pulltorefresh;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

import com.eftimoff.pulltorefresh.listeners.OnPullToRefreshListener;
import com.eftimoff.pulltorefresh.listeners.OnRefreshListener;
import com.eftimoff.pulltorefresh.utils.Utils;

/**
 * TODO Add Javadoc.
 * <p/>
 * Created by georgi.eftimov on 28/07/2015.
 */
public abstract class BasePullToRefreshLayout extends ViewGroup {

    /**
     * The default max distance in pixels can be dragged.
     */
    private static final int DRAG_MAX_DISTANCE = 120;

    /**
     * The drag rate.
     */
    private static final float DRAG_RATE = .5f;

    /**
     * The pointer index that is not valid.
     */
    private static final int INVALID_POINTER = -1;

    /**
     * The animation duration. It is multiplied by the procentage of the shown layout.
     */
    public static final int MAX_OFFSET_ANIMATION_DURATION = 700;

    /**
     * The pixels before we are sure that the user is starting scrolling.
     */
    private int touchSlop;

    /**
     * Total distance to be drag in dp.
     */
    private int totalDragDistance;

    /**
     * The view that will be used for refreshing.
     */
    private View refreshView;

    /**
     * Interpolator for both animations (Start , Correct)
     */
    private Interpolator interpolator;

    private View target;
    private int targetPaddingTop;
    private int targetPaddingBottom;
    private int targetPaddingRight;
    private int targetPaddingLeft;

    /**
     * The current offset being dragged.
     */
    private int currentOffsetTop;

    /**
     * If the view is refreshing at the moment.
     */
    private boolean refreshing;
    private int activePointerId;
    private boolean isBeingDragged;
    private float initialMotionY;
    private float currentDragPercent;
    private boolean notify;
    private int from;
    private float fromDragPercent;
    private boolean parallax;

    /**
     * Listener for all the inner operations.
     */
    private OnPullToRefreshListener onPullToRefreshListener;

    /**
     * Listener when the view is refreshed.
     */
    private OnRefreshListener onRefreshListener;

    public BasePullToRefreshLayout(final Context context) {
        this(context, null);
    }

    public BasePullToRefreshLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * TODO Add Javadoc.
     *
     * @param context
     * @param attrs
     */
    private void init(final Context context, final AttributeSet attrs) {
        //set the interpolator
        interpolator = new DecelerateInterpolator(2f);
        //get the pixels that are sure to be start scrolling.
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        //convert the max drag pixels to dp.
        initDragMaxDistance(context);
        //get the refresh view and add it to the layout.
        refreshView = getRefreshView();
        //do the refresh view make parallax effect.
        parallax = parallax();

        setRefreshing(false);

        addView(refreshView);
        //tell that we will draw to canvas.
        setWillNotDraw(false);
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
    }

    private void initDragMaxDistance(final Context context) {
        int dragMaxDistance = getDragMaxDistance();
        dragMaxDistance = dragMaxDistance <= 0 ? DRAG_MAX_DISTANCE : dragMaxDistance;
        totalDragDistance = Utils.convertDpToPixel(context, dragMaxDistance);
    }

    private void ensureTarget() {
        if (target != null)
            return;
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != refreshView) {
                    target = child;
                    targetPaddingBottom = target.getPaddingBottom();
                    targetPaddingLeft = target.getPaddingLeft();
                    targetPaddingRight = target.getPaddingRight();
                    targetPaddingTop = target.getPaddingTop();
                }
            }
        }
    }

    private void setTargetOffsetTop(int offset, boolean requiresUpdate) {
        target.offsetTopAndBottom(offset);
        if (parallax) {
            refreshView.offsetTopAndBottom(offset);
        }
        currentOffsetTop = target.getTop();
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        ensureTarget();
        if (target == null)
            return;

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingRight() - getPaddingLeft(), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);
        target.measure(widthMeasureSpec, heightMeasureSpec);
        refreshView.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled() || Utils.canChildScrollUp(target) || refreshing) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTop(0, true);
                activePointerId = MotionEventCompat.getPointerId(ev, 0);
                isBeingDragged = false;
                final float initialMotionY = getMotionEventY(ev, activePointerId);
                if (initialMotionY == -1) {
                    return false;
                }
                this.initialMotionY = initialMotionY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (activePointerId == INVALID_POINTER) {
                    return false;
                }
                final float y = getMotionEventY(ev, activePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - this.initialMotionY;
                if (yDiff > touchSlop && !isBeingDragged) {
                    isBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isBeingDragged = false;
                activePointerId = INVALID_POINTER;
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        return isBeingDragged;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent motionEvent) {
        if (!isBeingDragged) {
            return super.onTouchEvent(motionEvent);
        }

        final int action = MotionEventCompat.getActionMasked(motionEvent);

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(motionEvent, activePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                final float y = MotionEventCompat.getY(motionEvent, pointerIndex);
                final float yDiff = y - initialMotionY;
                final float scrollTop = yDiff * DRAG_RATE;
                currentDragPercent = scrollTop / totalDragDistance;
                if (currentDragPercent < 0) {
                    return false;
                }
                float boundedDragPercent = Math.min(1f, Math.abs(currentDragPercent));
                float extraOS = Math.abs(scrollTop) - totalDragDistance;
                float slingshotDist = totalDragDistance;
                float tensionSlingshotPercent = Math.max(0,
                        Math.min(extraOS, slingshotDist * 2) / slingshotDist);
                float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                        (tensionSlingshotPercent / 4), 2)) * 2f;
                float extraMove = (slingshotDist) * tensionPercent / 2;
                int targetY = (int) ((slingshotDist * boundedDragPercent) + extraMove);

                notifyOnPercentage(currentDragPercent, true);
                setTargetOffsetTop(targetY - currentOffsetTop, true);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN:
                final int index = MotionEventCompat.getActionIndex(motionEvent);
                activePointerId = MotionEventCompat.getPointerId(motionEvent, index);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(motionEvent);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (activePointerId == INVALID_POINTER) {
                    return false;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(motionEvent, activePointerId);
                final float y = MotionEventCompat.getY(motionEvent, pointerIndex);
                final float overScrollTop = (y - initialMotionY) * DRAG_RATE;
                isBeingDragged = false;
                if (overScrollTop > totalDragDistance) {
                    setRefreshing(true, true);
                } else {
                    refreshing = false;
                    animateOffsetToStartPosition();
                }
                activePointerId = INVALID_POINTER;
                return false;
            }
        }

        return true;
    }

    public void setRefreshing(boolean refreshing) {
        if (this.refreshing != refreshing) {
            setRefreshing(refreshing, false /* notify */);
        }
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (this.refreshing != refreshing) {
            this.notify = notify;
            ensureTarget();
            this.refreshing = refreshing;
            if (refreshing) {
                notifyOnPercentage(1f, true);
                animateOffsetToCorrectPosition();
            } else {
                animateOffsetToStartPosition();
            }
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == activePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            activePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    private float getMotionEventY(final MotionEvent motionEvent, final int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(motionEvent, activePointerId);
        if (index < 0) {
            return INVALID_POINTER;
        }
        return MotionEventCompat.getY(motionEvent, index);
    }

    private void animateOffsetToStartPosition() {
        from = currentOffsetTop;
        fromDragPercent = currentDragPercent;
        long animationDuration = Math.abs((long) (MAX_OFFSET_ANIMATION_DURATION * fromDragPercent));

        animateToStartPosition.reset();
        animateToStartPosition.setDuration(animationDuration);
        animateToStartPosition.setInterpolator(interpolator);
        animateToStartPosition.setAnimationListener(mToStartListener);
        refreshView.clearAnimation();
        refreshView.startAnimation(animateToStartPosition);
    }

    private void animateOffsetToCorrectPosition() {
        from = currentOffsetTop;
        fromDragPercent = currentDragPercent;

        animateToCorrectPosition.reset();
        animateToCorrectPosition.setDuration(MAX_OFFSET_ANIMATION_DURATION);
        animateToCorrectPosition.setInterpolator(interpolator);
        refreshView.clearAnimation();
        refreshView.startAnimation(animateToCorrectPosition);

        if (refreshing) {
            notifyOnStart();
            if (notify) {
                notifyOnRefresh();
            }
        } else {
            notifyOnStop();
            animateOffsetToStartPosition();
        }
        currentOffsetTop = target.getTop();
        target.setPadding(targetPaddingLeft, targetPaddingTop, targetPaddingRight, totalDragDistance);
    }

    private final Animation animateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private final Animation animateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop;
            int endTarget = totalDragDistance;
            targetTop = (from + (int) ((endTarget - from) * interpolatedTime));
            int offset = targetTop - target.getTop();

            currentDragPercent = fromDragPercent - (fromDragPercent - 1.0f) * interpolatedTime;
            notifyOnPercentage(currentDragPercent, false);
            setTargetOffsetTop(offset, false /* requires update */);
        }
    };

    private Animation.AnimationListener mToStartListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            notifyOnStop();
            currentOffsetTop = target.getTop();
        }
    };

    private void moveToStart(float interpolatedTime) {
        int targetTop = from - (int) (from * interpolatedTime);
        float targetPercent = fromDragPercent * (1.0f - interpolatedTime);
        int offset = targetTop - target.getTop();

        currentDragPercent = targetPercent;
        notifyOnPercentage(currentDragPercent, true);
        target.setPadding(targetPaddingLeft, targetPaddingTop, targetPaddingRight, targetPaddingBottom);
        setTargetOffsetTop(offset, false);
    }


    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {

        ensureTarget();
        if (target == null)
            return;

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();

        target.layout(left, top + currentOffsetTop, left + width - right, top + height - bottom + currentOffsetTop);
        refreshView.layout(left, top, left + width - right, top + height - bottom);
    }

    private void notifyOnStart() {
        if (onPullToRefreshListener != null) {
            onPullToRefreshListener.onStart();
        }
    }

    private void notifyOnStop() {
        if (onPullToRefreshListener != null) {
            onPullToRefreshListener.onStop();
        }
    }

    private void notifyOnPercentage(float percentage, final boolean invalidate) {
        if (percentage < 0f) {
            percentage = 0f;
        } else if (percentage > 1f) {
            percentage = 1f;
        }

        if (onPullToRefreshListener != null) {
            onPullToRefreshListener.onPercent(percentage, invalidate);
        }
    }

    private void notifyOnRefresh() {
        if (onRefreshListener != null) {
            onRefreshListener.onRefresh();
        }
    }

    public void setOnPullToRefreshListener(final OnPullToRefreshListener onPullToRefreshListener) {
        this.onPullToRefreshListener = onPullToRefreshListener;
    }

    public void setOnRefreshListener(final OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public abstract View getRefreshView();

    public abstract int getDragMaxDistance();

    public abstract boolean parallax();
}
