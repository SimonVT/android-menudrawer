package net.simonvt.menudrawer;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

public abstract class DraggableDrawer extends MenuDrawer {

    /**
     * Key used when saving menu visibility state.
     */
    private static final String STATE_MENU_VISIBLE = "net.simonvt.menudrawer.MenuDrawer.menuVisible";

    /**
     * Interpolator used for peeking at the drawer.
     */
    private static final Interpolator PEEK_INTERPOLATOR = new PeekInterpolator();

    /**
     * The maximum alpha of the dark menu overlay used for dimming the menu.
     */
    protected static final int MAX_MENU_OVERLAY_ALPHA = 185;

    /**
     * Default delay from {@link #peekDrawer()} is called until first animation is run.
     */
    private static final long DEFAULT_PEEK_START_DELAY = 5000;

    /**
     * Default delay between each subsequent animation, after {@link #peekDrawer()} has been called.
     */
    private static final long DEFAULT_PEEK_DELAY = 10000;

    /**
     * The duration of the peek animation.
     */
    protected static final int PEEK_DURATION = 5000;

    /**
     * Distance in dp from closed position from where the drawer is considered closed with regards to touch events.
     */
    private static final int CLOSE_ENOUGH = 3;

    protected static final int INVALID_POINTER = -1;

    /**
     * Slop before starting a drag.
     */
    protected int mTouchSlop;

    /**
     * Runnable used when the peek animation is running.
     */
    protected final Runnable mPeekRunnable = new Runnable() {
        @Override
        public void run() {
            peekDrawerInvalidate();
        }
    };

    /**
     * Runnable used when animating the drawer open/closed.
     */
    private final Runnable mDragRunnable = new Runnable() {
        @Override
        public void run() {
            postAnimationInvalidate();
        }
    };

    /**
     * Indicates whether the drawer is currently being dragged.
     */
    protected boolean mIsDragging;

    /**
     * The current pointer id.
     */
    protected int mActivePointerId = INVALID_POINTER;

    /**
     * The initial X position of a drag.
     */
    protected float mInitialMotionX;

    /**
     * The initial Y position of a drag.
     */
    protected float mInitialMotionY;

    /**
     * The last X position of a drag.
     */
    protected float mLastMotionX = -1;

    /**
     * The last Y position of a drag.
     */
    protected float mLastMotionY = -1;

    /**
     * Default delay between each subsequent animation, after {@link #peekDrawer()} has been called.
     */
    protected long mPeekDelay;

    /**
     * Scroller used for the peek drawer animation.
     */
    protected Scroller mPeekScroller;

    /**
     * Velocity tracker used when animating the drawer open/closed after a drag.
     */
    protected VelocityTracker mVelocityTracker;

    /**
     * Maximum velocity allowed when animating the drawer open/closed.
     */
    protected int mMaxVelocity;

    /**
     * Indicates whether the menu should be offset when dragging the drawer.
     */
    protected boolean mOffsetMenu = true;

    /**
     * Distance in px from closed position from where the drawer is considered closed with regards to touch events.
     */
    protected int mCloseEnough;

    /**
     * Runnable used for first call to {@link #startPeek()} after {@link #peekDrawer()}  has been called.
     */
    private Runnable mPeekStartRunnable;

    protected boolean mIsPeeking;

    /**
     * Scroller used when animating the drawer open/closed.
     */
    private Scroller mScroller;

    /**
     * Indicates whether the current layer type is {@link android.view.View#LAYER_TYPE_HARDWARE}.
     */
    protected boolean mLayerTypeHardware;

    DraggableDrawer(Activity activity, int dragMode) {
        super(activity, dragMode);
    }

    public DraggableDrawer(Context context) {
        super(context);
    }

    public DraggableDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DraggableDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initDrawer(Context context, AttributeSet attrs, int defStyle) {
        super.initDrawer(context, attrs, defStyle);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();

        mScroller = new Scroller(context, MenuDrawer.SMOOTH_INTERPOLATOR);
        mPeekScroller = new Scroller(context, DraggableDrawer.PEEK_INTERPOLATOR);

        mCloseEnough = dpToPx(DraggableDrawer.CLOSE_ENOUGH);
    }

    public void toggleMenu(boolean animate) {
        if (mDrawerState == STATE_OPEN || mDrawerState == STATE_OPENING) {
            closeMenu(animate);
        } else if (mDrawerState == STATE_CLOSED || mDrawerState == STATE_CLOSING) {
            openMenu(animate);
        }
    }

    public boolean isMenuVisible() {
        return mMenuVisible;
    }

    public void setMenuSize(final int size) {
        mMenuSize = size;
        if (mDrawerState == STATE_OPEN || mDrawerState == STATE_OPENING) {
            setOffsetPixels(mMenuSize);
        }
        requestLayout();
        invalidate();
    }

    public void setOffsetMenuEnabled(boolean offsetMenu) {
        if (offsetMenu != mOffsetMenu) {
            mOffsetMenu = offsetMenu;
            requestLayout();
            invalidate();
        }
    }

    public boolean getOffsetMenuEnabled() {
        return mOffsetMenu;
    }

    public void peekDrawer() {
        peekDrawer(DEFAULT_PEEK_START_DELAY, DEFAULT_PEEK_DELAY);
    }

    public void peekDrawer(long delay) {
        peekDrawer(DEFAULT_PEEK_START_DELAY, delay);
    }

    public void peekDrawer(final long startDelay, final long delay) {
        if (startDelay < 0) {
            throw new IllegalArgumentException("startDelay must be zero or larger.");
        }
        if (delay < 0) {
            throw new IllegalArgumentException("delay must be zero or larger");
        }

        removeCallbacks(mPeekRunnable);
        removeCallbacks(mPeekStartRunnable);

        mPeekDelay = delay;
        mPeekStartRunnable = new Runnable() {
            @Override
            public void run() {
                startPeek();
            }
        };
        postDelayed(mPeekStartRunnable, startDelay);
    }

    public void setHardwareLayerEnabled(boolean enabled) {
        if (enabled != mHardwareLayersEnabled) {
            mHardwareLayersEnabled = enabled;
            mMenuContainer.setHardwareLayersEnabled(enabled);
            mContentContainer.setHardwareLayersEnabled(enabled);
            stopLayerTranslation();
        }
    }

    public int getTouchMode() {
        return mTouchMode;
    }

    public void setTouchMode(int mode) {
        if (mTouchMode != mode) {
            mTouchMode = mode;
            updateTouchAreaSize();
        }
    }

    public void setTouchBezelSize(int size) {
        mTouchBezelSize = size;
    }

    public int getTouchBezelSize() {
        return mTouchBezelSize;
    }

    /**
     * If possible, set the layer type to {@link android.view.View#LAYER_TYPE_HARDWARE}.
     */
    protected void startLayerTranslation() {
        if (USE_TRANSLATIONS && mHardwareLayersEnabled && !mLayerTypeHardware) {
            mLayerTypeHardware = true;
            mContentContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            mMenuContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    /**
     * If the current layer type is {@link android.view.View#LAYER_TYPE_HARDWARE}, this will set it to
     * {@link View#LAYER_TYPE_NONE}.
     */
    protected void stopLayerTranslation() {
        if (mLayerTypeHardware) {
            mLayerTypeHardware = false;
            mContentContainer.setLayerType(View.LAYER_TYPE_NONE, null);
            mMenuContainer.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    /**
     * Called when a drag has been ended.
     */
    protected void endDrag() {
        mIsDragging = false;

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * Stops ongoing animation of the drawer.
     */
    protected void stopAnimation() {
        removeCallbacks(mDragRunnable);
        mScroller.abortAnimation();
        stopLayerTranslation();
    }

    /**
     * Called when a drawer animation has successfully completed.
     */
    private void completeAnimation() {
        mScroller.abortAnimation();
        final int finalX = mScroller.getFinalX();
        setOffsetPixels(finalX);
        setDrawerState(finalX == 0 ? STATE_CLOSED : STATE_OPEN);
        stopLayerTranslation();
    }

    protected void cancelContentTouch() {
        final long now = SystemClock.uptimeMillis();
        final MotionEvent cancelEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).dispatchTouchEvent(cancelEvent);
        }
        mContentContainer.dispatchTouchEvent(cancelEvent);
        cancelEvent.recycle();
    }

    /**
     * Moves the drawer to the position passed.
     *
     * @param position The position the content is moved to.
     * @param velocity Optional velocity if called by releasing a drag event.
     * @param animate  Whether the move is animated.
     */
    protected void animateOffsetTo(int position, int velocity, boolean animate) {
        endDrag();
        endPeek();

        final int startX = (int) mOffsetPixels;
        final int dx = position - startX;
        if (dx == 0 || !animate) {
            setOffsetPixels(position);
            setDrawerState(position == 0 ? STATE_CLOSED : STATE_OPEN);
            stopLayerTranslation();
            return;
        }

        int duration;

        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000.f * Math.abs((float) dx / velocity));
        } else {
            duration = (int) (600.f * Math.abs((float) dx / mMenuSize));
        }

        duration = Math.min(duration, mMaxAnimationDuration);
        animateOffsetTo(position, duration);
    }

    protected void animateOffsetTo(int position, int duration) {
        final int startX = (int) mOffsetPixels;
        final int dx = position - startX;

        if (dx > 0) {
            setDrawerState(STATE_OPENING);
            mScroller.startScroll(startX, 0, dx, 0, duration);
        } else {
            setDrawerState(STATE_CLOSING);
            mScroller.startScroll(startX, 0, dx, 0, duration);
        }

        startLayerTranslation();

        postAnimationInvalidate();
    }

    /**
     * Callback when each frame in the drawer animation should be drawn.
     */
    private void postAnimationInvalidate() {
        if (mScroller.computeScrollOffset()) {
            final int oldX = (int) mOffsetPixels;
            final int x = mScroller.getCurrX();

            if (x != oldX) setOffsetPixels(x);
            if (x != mScroller.getFinalX()) {
                postOnAnimation(mDragRunnable);
                return;
            }
        }

        completeAnimation();
    }

    /**
     * Starts peek drawer animation.
     */
    protected void startPeek() {
        mIsPeeking = true;
        initPeekScroller();

        startLayerTranslation();
        peekDrawerInvalidate();
    }

    protected abstract void initPeekScroller();

    /**
     * Callback when each frame in the peek drawer animation should be drawn.
     */
    private void peekDrawerInvalidate() {
        if (mPeekScroller.computeScrollOffset()) {
            final int oldX = (int) mOffsetPixels;
            final int x = mPeekScroller.getCurrX();
            if (x != oldX) setOffsetPixels(x);

            if (!mPeekScroller.isFinished()) {
                postOnAnimation(mPeekRunnable);
                return;

            } else if (mPeekDelay > 0) {
                mPeekStartRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startPeek();
                    }
                };
                postDelayed(mPeekStartRunnable, mPeekDelay);
            }
        }

        completePeek();
    }

    /**
     * Called when the peek drawer animation has successfully completed.
     */
    private void completePeek() {
        mPeekScroller.abortAnimation();

        setOffsetPixels(0);

        setDrawerState(STATE_CLOSED);
        stopLayerTranslation();
        mIsPeeking = false;
    }

    /**
     * Stops ongoing peek drawer animation.
     */
    protected void endPeek() {
        removeCallbacks(mPeekStartRunnable);
        removeCallbacks(mPeekRunnable);
        stopLayerTranslation();
        mIsPeeking = false;
    }

    protected boolean isCloseEnough() {
        return Math.abs(mOffsetPixels) <= mCloseEnough;
    }

    protected boolean canChildrenScroll(int dx, int dy, int x, int y) {
        boolean canScroll = false;

        switch (getPosition()) {
            case LEFT:
            case RIGHT:
                if (!mMenuVisible) {
                    canScroll = canChildScrollHorizontally(mContentContainer, false, dx,
                            x - ViewHelper.getLeft(mContentContainer), y - ViewHelper.getTop(mContentContainer));
                } else {
                    canScroll = canChildScrollHorizontally(mMenuContainer, false, dx,
                            x - ViewHelper.getLeft(mMenuContainer), y - ViewHelper.getTop(mContentContainer));
                }
                break;

            case TOP:
            case BOTTOM:
                if (!mMenuVisible) {
                    canScroll = canChildScrollVertically(mContentContainer, false, dy,
                            x - ViewHelper.getLeft(mContentContainer), y - ViewHelper.getTop(mContentContainer));
                } else {
                    canScroll = canChildScrollVertically(mMenuContainer, false, dy,
                            x - ViewHelper.getLeft(mMenuContainer), y - ViewHelper.getTop(mContentContainer));
                }
        }

        return canScroll;
    }

    /**
     * Tests scrollability within child views of v given a delta of dx.
     *
     * @param v      View to test for horizontal scrollability
     * @param checkV Whether the view should be checked for draggability
     * @param dx     Delta scrolled in pixels
     * @param x      X coordinate of the active touch point
     * @param y      Y coordinate of the active touch point
     * @return true if child views of v can be scrolled by delta of dx.
     */
    protected boolean canChildScrollHorizontally(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;

            final int count = group.getChildCount();
            // Count backwards - let topmost views consume scroll distance first.
            for (int i = count - 1; i >= 0; i--) {
                final View child = group.getChildAt(i);

                final int childLeft = child.getLeft() + supportGetTranslationX(child);
                final int childRight = child.getRight() + supportGetTranslationX(child);
                final int childTop = child.getTop() + supportGetTranslationY(child);
                final int childBottom = child.getBottom() + supportGetTranslationY(child);

                if (x >= childLeft && x < childRight && y >= childTop && y < childBottom
                        && canChildScrollHorizontally(child, true, dx, x - childLeft, y - childTop)) {
                    return true;
                }
            }
        }

        return checkV && mOnInterceptMoveEventListener.isViewDraggable(v, dx, x, y);
    }

    /**
     * Tests scrollability within child views of v given a delta of dx.
     *
     * @param v      View to test for horizontal scrollability
     * @param checkV Whether the view should be checked for draggability
     * @param dx     Delta scrolled in pixels
     * @param x      X coordinate of the active touch point
     * @param y      Y coordinate of the active touch point
     * @return true if child views of v can be scrolled by delta of dx.
     */
    protected boolean canChildScrollVertically(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;

            final int count = group.getChildCount();
            // Count backwards - let topmost views consume scroll distance first.
            for (int i = count - 1; i >= 0; i--) {
                final View child = group.getChildAt(i);

                final int childLeft = child.getLeft() + supportGetTranslationX(child);
                final int childRight = child.getRight() + supportGetTranslationX(child);
                final int childTop = child.getTop() + supportGetTranslationY(child);
                final int childBottom = child.getBottom() + supportGetTranslationY(child);

                if (x >= childLeft && x < childRight && y >= childTop && y < childBottom
                        && canChildScrollVertically(child, true, dx, x - childLeft, y - childTop)) {
                    return true;
                }
            }
        }

        return checkV && mOnInterceptMoveEventListener.isViewDraggable(v, dx, x, y);
    }

    protected float getXVelocity(VelocityTracker velocityTracker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return velocityTracker.getXVelocity(mActivePointerId);
        }

        return velocityTracker.getXVelocity();
    }

    protected float getYVelocity(VelocityTracker velocityTracker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return velocityTracker.getYVelocity(mActivePointerId);
        }

        return velocityTracker.getYVelocity();
    }

    private int supportGetTranslationY(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return (int) v.getTranslationY();
        }

        return 0;
    }

    private int supportGetTranslationX(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return (int) v.getTranslationX();
        }

        return 0;
    }

    void saveState(Bundle state) {
        final boolean menuVisible = mDrawerState == STATE_OPEN || mDrawerState == STATE_OPENING;
        state.putBoolean(STATE_MENU_VISIBLE, menuVisible);
    }

    public void restoreState(Parcelable in) {
        super.restoreState(in);
        Bundle state = (Bundle) in;
        final boolean menuOpen = state.getBoolean(STATE_MENU_VISIBLE);
        if (menuOpen) {
            openMenu(false);
        } else {
            setOffsetPixels(0);
        }
        mDrawerState = menuOpen ? STATE_OPEN : STATE_CLOSED;
    }
}
