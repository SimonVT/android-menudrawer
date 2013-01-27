package net.simonvt.menudrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

public abstract class DraggableDrawer extends MenuDrawer {

    /**
     * Key used when saving menu visibility state.
     */
    private static final String STATE_MENU_VISIBLE = "net.simonvt.menudrawer.MenuDrawer.menuVisible";

    /**
     * Interpolator used for stretching/retracting the active indicator.
     */
    protected static final Interpolator INDICATOR_INTERPOLATOR = new AccelerateInterpolator();

    /**
     * Interpolator used for peeking at the drawer.
     */
    private static final Interpolator PEEK_INTERPOLATOR = new PeekInterpolator();

    /**
     * The time between each frame when animating the drawer.
     */
    protected static final int ANIMATION_DELAY = 1000 / 60;

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
     * The maximum animation duration.
     */
    private static final int DURATION_MAX = 600;

    /**
     * Distance in dp from closed position from where the drawer is considered closed with regards to touch events.
     */
    private static final int CLOSE_ENOUGH = 3;

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
     * Current left position of the content.
     */
    protected float mOffsetPixels;

    /**
     * Indicates whether the drawer is currently being dragged.
     */
    protected boolean mIsDragging;

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

    /**
     * Scroller used when animating the drawer open/closed.
     */
    private Scroller mScroller;

    /**
     * Indicates whether the current layer type is {@link android.view.View#LAYER_TYPE_HARDWARE}.
     */
    private boolean mLayerTypeHardware;

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
        mMenuSizeSet = true;
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
     * Sets the number of pixels the content should be offset.
     *
     * @param offsetPixels The number of pixels to offset the content by.
     */
    protected void setOffsetPixels(float offsetPixels) {
        final int oldOffset = (int) mOffsetPixels;
        final int newOffset = (int) offsetPixels;

        mOffsetPixels = offsetPixels;

        if (newOffset != oldOffset) {
            onOffsetPixelsChanged(newOffset);
            mMenuVisible = newOffset != 0;
        }
    }

    /**
     * Called when the number of pixels the content should be offset by has changed.
     *
     * @param offsetPixels The number of pixels to offset the content by.
     */
    protected abstract void onOffsetPixelsChanged(int offsetPixels);

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
    private void stopLayerTranslation() {
        if (mLayerTypeHardware) {
            mLayerTypeHardware = false;
            mContentContainer.setLayerType(View.LAYER_TYPE_NONE, null);
            mMenuContainer.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    /**
     * Compute the touch area based on the touch mode.
     */
    protected void updateTouchAreaSize() {
        if (mTouchMode == TOUCH_MODE_BEZEL) {
            mTouchSize = mTouchBezelSize;
        } else if (mTouchMode == TOUCH_MODE_FULLSCREEN) {
            mTouchSize = getMeasuredWidth();
        } else {
            mTouchSize = 0;
        }
    }

    /**
     * Called when a drag has been ended.
     */
    private void endDrag() {
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

        duration = Math.min(duration, DURATION_MAX);

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
    }

    /**
     * Stops ongoing peek drawer animation.
     */
    protected void endPeek() {
        removeCallbacks(mPeekStartRunnable);
        removeCallbacks(mPeekRunnable);
        stopLayerTranslation();
    }

    @Override
    public void postOnAnimation(Runnable action) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            super.postOnAnimation(action);
        } else {
            postDelayed(action, ANIMATION_DELAY);
        }
    }

    protected boolean isCloseEnough() {
        return Math.abs(mOffsetPixels) <= mCloseEnough;
    }

    /**
     * Returns true if the touch event occurs over the content.
     *
     * @param ev The motion event.
     * @return True if the touch event occurred over the content, false otherwise.
     */
    protected abstract boolean isContentTouch(MotionEvent ev);

    /**
     * Returns true if dragging the content should be allowed.
     *
     * @param ev The motion event.
     * @return True if dragging the content should be allowed, false otherwise.
     */
    protected abstract boolean onDownAllowDrag(MotionEvent ev);

    /**
     * Returns true if dragging the content should be allowed.
     *
     * @param ev The motion event.
     * @return True if dragging the content should be allowed, false otherwise.
     */
    protected abstract boolean onMoveAllowDrag(MotionEvent ev, float dx);

    /**
     * Called when a move event has happened while dragging the content is in progress.
     *
     * @param dx The X difference between the last motion event and the current motion event.
     */
    protected abstract void onMoveEvent(float dx);

    /**
     * Called when {@link android.view.MotionEvent#ACTION_UP} of {@link android.view.MotionEvent#ACTION_CANCEL} is
     * delivered to {@link net.simonvt.menudrawer.MenuDrawer#onTouchEvent(android.view.MotionEvent)}.
     *
     * @param ev The motion event.
     */
    protected abstract void onUpEvent(MotionEvent ev);

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        final int offsetPixels = (int) mOffsetPixels;

        if (offsetPixels != 0) drawMenuOverlay(canvas, offsetPixels);
        if (mDropShadowEnabled) drawDropShadow(canvas, offsetPixels);
        if (mActiveIndicator != null) drawIndicator(canvas, offsetPixels);
    }

    /**
     * Called when the content drop shadow should be drawn.
     *
     * @param canvas       The canvas on which to draw.
     * @param offsetPixels Value in pixels indicating the offset.
     */
    protected abstract void drawDropShadow(Canvas canvas, int offsetPixels);

    /**
     * Called when the menu overlay should be drawn.
     *
     * @param canvas       The canvas on which to draw.
     * @param offsetPixels Value in pixels indicating the offset.
     */
    protected abstract void drawMenuOverlay(Canvas canvas, int offsetPixels);

    /**
     * Called when the active indicator should be drawn.
     *
     * @param canvas       The canvas on which to draw.
     * @param offsetPixels Value in pixels indicating the offset.
     */
    protected abstract void drawIndicator(Canvas canvas, int offsetPixels);

    public Parcelable saveState() {
        Bundle state = new Bundle();
        final boolean menuVisible = mDrawerState == STATE_OPEN || mDrawerState == STATE_OPENING;
        state.putBoolean(STATE_MENU_VISIBLE, menuVisible);
        return state;
    }

    public void restoreState(Parcelable in) {
        Bundle state = (Bundle) in;
        final boolean menuOpen = state.getBoolean(STATE_MENU_VISIBLE);
        setOffsetPixels(menuOpen ? mMenuSize : 0);
        mDrawerState = menuOpen ? STATE_OPEN : STATE_CLOSED;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState state = new SavedState(superState);
        state.mMenuVisible = mDrawerState == STATE_OPEN || mDrawerState == STATE_OPENING;

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        setOffsetPixels(savedState.mMenuVisible ? mMenuSize : 0);
        mDrawerState = savedState.mMenuVisible ? STATE_OPEN : STATE_CLOSED;
    }

    static class SavedState extends BaseSavedState {

        boolean mMenuVisible;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel in) {
            super(in);
            mMenuVisible = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mMenuVisible ? 1 : 0);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
