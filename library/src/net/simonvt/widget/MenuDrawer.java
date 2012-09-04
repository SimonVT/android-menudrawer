package net.simonvt.widget;

import net.simonvt.menudrawer.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class MenuDrawer extends ViewGroup {

    /**
     * Callback interface for changing state of the drawer.
     */
    public interface OnDrawerStateChangeListener {

        /**
         * Called when the drawer state changes.
         *
         * @param oldState The old drawer state.
         * @param newState The new drawer state.
         */
        void onDrawerStateChange(int oldState, int newState);
    }

    /**
     * Tag used when logging.
     */
    private static final String TAG = "MenuDrawer";

    /**
     * Indicates whether debug code should be enabled.
     */
    private static final boolean DEBUG = false;

    /**
     * Key used when saving menu visibility state.
     */
    private static final String STATE_MENU_VISIBLE = "net.simonvt.menudrawer.view.menu.menuVisible";

    /**
     * The time between each frame when animating the drawer.
     */
    private static final int ANIMATION_DELAY = 1000 / 60;

    /**
     * Interpolator used for stretching/retracting the arrow indicator.
     */
    private static final Interpolator ARROW_INTERPOLATOR = new AccelerateInterpolator();

    /**
     * The maximum touch area width of the drawer in dp.
     */
    private static final int MAX_DRAG_BEZEL_DP = 16;

    /**
     * The maximum animation duration.
     */
    private static final int DURATION_MAX = 600;

    /**
     * The maximum alpha of the dark menu overlay used for dimming the menu.
     */
    private static final int MAX_MENU_OVERLAY_ALPHA = 185;

    /**
     * Drag mode for sliding only the content view.
     */
    public static final int MENU_DRAG_CONTENT = 0;

    /**
     * Drag mode for sliding the entire window.
     */
    public static final int MENU_DRAG_WINDOW = 1;

    /**
     * Indicates that the drawer is currently closed.
     */
    public static final int STATE_CLOSED = 0;

    /**
     * Indicates that the drawer is currently closing.
     */
    public static final int STATE_CLOSING = 1;

    /**
     * Indicates that the drawer is currently being dragged by the user.
     */
    public static final int STATE_DRAGGING = 2;

    /**
     * Indicates that the drawer is currently opening.
     */
    public static final int STATE_OPENING = 4;

    /**
     * Indicates that the drawer is currently open.
     */
    public static final int STATE_OPEN = 8;

    private static final int CLOSE_ENOUGH = 3;

    /**
     * Drawable used as menu overlay.
     */
    private Drawable mMenuOverlay;

    /**
     * Drawable used as content drop shadow onto the menu.
     */
    private Drawable mContentDropShadow;

    /**
     * The width of the content drop shadow.
     */
    private int mDropShadowWidth;

    /**
     * Arrow bitmap used to indicate the active view.
     */
    private Bitmap mArrowBitmap;

    /**
     * The currently active view.
     */
    private View mActiveView;

    /**
     * Position of the active view. This is compared to View#getTag(R.id.mdActiveViewPosition) when drawing the arrow.
     */
    private int mActivePosition;

    /**
     * Used when reading the position of the active view.
     */
    private Rect mActiveRect = new Rect();

    /**
     * The parent of the menu view.
     */
    private FrameLayout mMenuContainer;

    /**
     * The parent of the content view.
     */
    private FrameLayout mContentView;

    /**
     * The width of the menu.
     */
    private int mMenuWidth;

    /**
     * Indicates whether the menu width has been set in the theme.
     */
    private boolean mMenuWidthFromTheme;

    /**
     * Current left position of the content.
     */
    private int mContentLeft;

    /**
     * Indicates whether the menu is currently visible.
     */
    private boolean mMenuVisible;

    /**
     * The drag mode of the drawer. Can be either {@link #MENU_DRAG_CONTENT} or {@link #MENU_DRAG_WINDOW}.
     */
    private int mDragMode;

    /**
     * The current drawer state.
     *
     * @see #STATE_CLOSED
     * @see #STATE_CLOSING
     * @see #STATE_DRAGGING
     * @see #STATE_OPENING
     * @see #STATE_OPEN
     */
    private int mDrawerState = STATE_CLOSED;

    /**
     * The maximum touch area width of the drawer in px.
     */
    private int mMaxDragBezelSize;

    /**
     * The touch area width of the drawer in px.
     */
    private int mDragBezelSize;

    /**
     * Indicates whether the drawer is currently being dragged.
     */
    private boolean mIsDragging;

    /**
     * Slop before starting a drag.
     */
    private final int mTouchSlop;

    /**
     * The initial X position of a drag.
     */
    private float mInitialMotionX;

    /**
     * The last X position of a drag.
     */
    private float mLastMotionX = -1;

    /**
     * The last Y position of a drag.
     */
    private float mLastMotionY = -1;

    /**
     * Runnable used when animating the drawer open/closed.
     */
    private final Runnable mDragRunnable = new Runnable() {
        public void run() {
            postAnimationInvalidate();
        }
    };

    /**
     * Scroller used when animating the drawer open/closed.
     */
    private Scroller mScroller;

    /**
     * Interpolator used when animating the drawer open/closed.
     */
    private static final Interpolator SMOOTH_INTERPOLATOR = new SmoothInterpolator();

    /**
     * Velocity tracker used when animating the drawer open/closed after a drag.
     */
    private VelocityTracker mVelocityTracker;

    /**
     * Maximum velocity allowed when animating the drawer open/closed.
     */
    private int mMaxVelocity;

    /**
     * Listener used to dispatch state change events.
     */
    private OnDrawerStateChangeListener mOnDrawerStateChangeListener;

    /**
     * Indicates whether the menu should be offset when dragging the drawer.
     */
    private boolean mOffsetMenu = true;

    private int mCloseEnough;

    /**
     * Indicates whether to use {@link View#setTranslationX(float)} when positioning views.
     */
    static final boolean USE_TRANSLATIONS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    /**
     * Indicates whether the current layer type is {@link View#LAYER_TYPE_HARDWARE}.
     */
    private boolean mLayerTypeHardware;

    public MenuDrawer(Context context) {
        this(context, null);
    }

    public MenuDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.menuDrawerStyle);
    }

    public MenuDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setWillNotDraw(false);
        setFocusable(false);

        TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.MenuDrawer, defStyle, R.style.Widget_MenuDrawer);

        final Drawable contentBackground = a.getDrawable(R.styleable.MenuDrawer_mdContentBackground);
        final Drawable menuBackground = a.getDrawable(R.styleable.MenuDrawer_mdMenuBackground);
        mMenuWidth = a.getDimensionPixelSize(R.styleable.MenuDrawer_mdMenuWidth, -1);
        mMenuWidthFromTheme = mMenuWidth != -1;
        final int arrowResId = a.getResourceId(R.styleable.MenuDrawer_mdArrowDrawable, 0);
        if (arrowResId != 0) {
            mArrowBitmap = BitmapFactory.decodeResource(getResources(), arrowResId);
        }

        a.recycle();

        mMenuContainer = new BuildLayerFrameLayout(context);
        mMenuContainer.setId(R.id.md__menu);
        mMenuContainer.setBackgroundDrawable(menuBackground);
        addView(mMenuContainer);

        mContentView = new NoClickThroughFrameLayout(context);
        mContentView.setId(R.id.md__content);
        mContentView.setBackgroundDrawable(contentBackground);
        addView(mContentView);

        mContentDropShadow = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[] {
                0xFF000000,
                0x00000000,
        });
        mDropShadowWidth = (int) (6 * getResources().getDisplayMetrics().density + 0.5f);

        mMenuOverlay = new ColorDrawable(0xFF000000);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();

        mScroller = new Scroller(context, SMOOTH_INTERPOLATOR);

        final float density = getResources().getDisplayMetrics().density;
        mMaxDragBezelSize = (int) (MAX_DRAG_BEZEL_DP * density + 0.5f);
        mCloseEnough = (int) (CLOSE_ENOUGH * density);
    }

    /**
     * Toggles the menu open and close.
     */
    public void toggleMenu() {
        if (mDrawerState == STATE_OPEN || mDrawerState == STATE_OPENING) {
            closeMenu();
        } else if (mDrawerState == STATE_CLOSED || mDrawerState == STATE_CLOSING) {
            openMenu();
        }
    }

    /**
     * Opens the menu.
     */
    public void openMenu() {
        animateContent(true, 0);
    }

    /**
     * Closes the menu.
     */
    public void closeMenu() {
        animateContent(false, 0);
    }

    /**
     * Indicates whether the menu is currently visible.
     *
     * @return True if the menu is open, false otherwise.
     */
    public boolean isMenuVisible() {
        return mMenuVisible;
    }

    /**
     * Set the active view. If the mdArrowDrawable attribute is set, this View will have an arrow drawn next to it.
     *
     * @param v        The active view.
     * @param position Optional position, usually used with ListView. v.setTag(R.id.mdActiveViewPosition, position)
     *                 must be called first.
     */
    public void setActiveView(View v, int position) {
        mActiveView = v;
        mActivePosition = position;
        invalidate();
    }

    /**
     * Enables or disables offsetting the menu when dragging the drawer.
     *
     * @param offsetMenu True to offset the menu, false otherwise.
     */
    public void setOffsetMenuEnabled(boolean offsetMenu) {
        if (offsetMenu != mOffsetMenu) {
            mOffsetMenu = offsetMenu;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Indicates whether the menu is being offset when dragging the drawer.
     *
     * @return True if the menu is being offset, false otherwise.
     */
    public boolean getOffsetMenuEnabled() {
        return mOffsetMenu;
    }

    /**
     * Returns the state of the drawer. Can be one of {@link #STATE_CLOSED}, {@link #STATE_CLOSING},
     * {@link #STATE_DRAGGING}, {@link #STATE_OPENING} or {@link #STATE_OPEN}.
     *
     * @return The drawers state.
     */
    public int getDrawerState() {
        return mDrawerState;
    }

    /**
     * Register a callback to be invoked when the drawer state changes.
     *
     * @param listener The callback that will run.
     */
    public void setOnDrawerStateChangeListener(OnDrawerStateChangeListener listener) {
        mOnDrawerStateChangeListener = listener;
    }

    /**
     * Sets the drawer state.
     *
     * @param state The drawer state. Must be one of {@link #STATE_CLOSED}, {@link #STATE_CLOSING},
     *              {@link #STATE_DRAGGING}, {@link #STATE_OPENING} or {@link #STATE_OPEN}.
     */
    private void setDrawerState(int state) {
        if (state != mDrawerState) {
            final int oldState = mDrawerState;
            mDrawerState = state;
            if (mOnDrawerStateChangeListener != null) mOnDrawerStateChangeListener.onDrawerStateChange(oldState, state);
            if (DEBUG) logDrawerState(state);
        }
    }

    private void logDrawerState(int state) {
        switch (state) {
            case STATE_CLOSED:
                Log.d(TAG, "[DrawerState] STATE_CLOSED");
                break;

            case STATE_CLOSING:
                Log.d(TAG, "[DrawerState] STATE_CLOSING");
                break;

            case STATE_DRAGGING:
                Log.d(TAG, "[DrawerState] STATE_DRAGGING");
                break;

            case STATE_OPENING:
                Log.d(TAG, "[DrawerState] STATE_OPENING");
                break;

            case STATE_OPEN:
                Log.d(TAG, "[DrawerState] STATE_OPEN");
                break;

            default:
        }
    }

    /**
     * Sets the drawer drag mode. Can be either {@link #MENU_DRAG_CONTENT} or {@link #MENU_DRAG_WINDOW}.
     *
     * @param dragMode
     */
    public void setDragMode(int dragMode) {
        mDragMode = dragMode;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        final int height = getHeight();
        final int contentLeft = mContentLeft;
        final int dropShadowWidth = mDropShadowWidth;
        final float openRatio = ((float) contentLeft) / mMenuWidth;

        mMenuOverlay.setBounds(0, 0, contentLeft, height);
        mMenuOverlay.setAlpha((int) (MAX_MENU_OVERLAY_ALPHA * (1.f - openRatio)));
        mMenuOverlay.draw(canvas);

        mContentDropShadow.setBounds(contentLeft - dropShadowWidth, 0, contentLeft, height);
        mContentDropShadow.draw(canvas);

        if (mArrowBitmap != null) drawArrow(canvas, contentLeft, openRatio);
    }

    /**
     * Draws the arrow indicating the currently active view.
     *
     * @param canvas    The canvas on which to draw.
     * @param openRatio [0..1] value indicating how open the drawer is.
     */
    private void drawArrow(Canvas canvas, int contentLeft, float openRatio) {
        if (mActiveView != null && mActiveView.getParent() != null) {
            Integer position = (Integer) mActiveView.getTag(R.id.mdActiveViewPosition);
            final int pos = position == null ? 0 : position;

            if (pos == mActivePosition) {
                mActiveView.getDrawingRect(mActiveRect);
                offsetDescendantRectToMyCoords(mActiveView, mActiveRect);

                final float interpolatedRatio = 1.f - ARROW_INTERPOLATOR.getInterpolation((1.f - openRatio));
                final int interpolatedArrowWidth = (int) (mArrowBitmap.getWidth() * interpolatedRatio);

                final int top = mActiveRect.top + ((mActiveRect.height() - mArrowBitmap.getHeight()) / 2);
                final int right = contentLeft;
                final int left = right - interpolatedArrowWidth;

                canvas.save();
                canvas.clipRect(left, 0, right, getHeight());
                canvas.drawBitmap(mArrowBitmap, left, top, null);
                canvas.restore();
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;
        final int contentLeft = mContentLeft;

        mMenuContainer.layout(0, 0, mMenuWidth, height);
        offsetMenu(contentLeft);

        if (USE_TRANSLATIONS) {
            mContentView.layout(0, 0, width, height);
        } else {
            mContentView.layout(contentLeft, 0, width + contentLeft, height);
        }
    }

    /**
     * Offsets the menu relative to its original position based on the left position of the content.
     *
     * @param contentLeft The left position of the content.
     */
    private void offsetMenu(float contentLeft) {
        if (mOffsetMenu && mMenuWidth != 0) {
            final int menuWidth = mMenuWidth;
            final float openRatio = (menuWidth - contentLeft) / menuWidth;

            if (USE_TRANSLATIONS) {
                final int menuLeft = (int) (0.25f * (-openRatio * menuWidth));
                mMenuContainer.setTranslationX(menuLeft);

            } else {
                final int oldMenuLeft = mMenuContainer.getLeft();
                final int offset = (int) (0.25f * (-openRatio * menuWidth)) - oldMenuLeft;
                mMenuContainer.offsetLeftAndRight(offset);
            }
        }
    }

    /**
     * Set the left position of the content view.
     *
     * @param contentLeft The left position of the content view.
     */
    private void setContentLeft(int contentLeft) {
        if (contentLeft != mContentLeft) {
            if (USE_TRANSLATIONS) {
                mContentView.setTranslationX(contentLeft);
                offsetMenu(contentLeft);
                invalidate();
            } else {
                mContentView.offsetLeftAndRight(contentLeft - mContentView.getLeft());
                offsetMenu(contentLeft);
                invalidate();
            }

            mContentLeft = contentLeft;
            mMenuVisible = contentLeft != 0;
        }
    }

    /**
     * If possible, set the layer type to {@link View#LAYER_TYPE_HARDWARE}.
     */
    private void startLayerTranslation() {
        if (USE_TRANSLATIONS && !mLayerTypeHardware) {
            mLayerTypeHardware = true;
            mContentView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            mMenuContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    /**
     * If the current layer type is {@link View#LAYER_TYPE_HARDWARE}, this will set it to @link View#LAYER_TYPE_NONE}.
     */
    private void stopLayerTranslation() {
        if (mLayerTypeHardware) {
            mLayerTypeHardware = false;
            mContentView.setLayerType(View.LAYER_TYPE_NONE, null);
            mMenuContainer.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Must measure with an exact size");
        }

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        if (!mMenuWidthFromTheme) mMenuWidth = (int) (width * 0.8f);
        if (mContentLeft == -1) setContentLeft(mMenuWidth);

        final int menuWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, mMenuWidth);
        final int menuHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
        mMenuContainer.measure(menuWidthMeasureSpec, menuHeightMeasureSpec);

        final int contentWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width);
        final int contentHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
        mContentView.measure(contentWidthMeasureSpec, contentHeightMeasureSpec);

        if (mDragMode == MENU_DRAG_WINDOW) {
            View v = mContentView.getChildAt(0);
            final int topPadding = v.getPaddingTop();
            mMenuContainer.setPadding(0, topPadding, 0, 0);
        } else {
            mMenuContainer.setPadding(0, 0, 0, 0);
        }

        setMeasuredDimension(width, height);

        final int measuredWidth = getMeasuredWidth();
        mDragBezelSize = Math.min(measuredWidth / 10, mMaxDragBezelSize);
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
    private void stopAnimation() {
        removeCallbacks(mDragRunnable);
        mScroller.abortAnimation();
        final int contentLeft = mContentLeft;

        stopLayerTranslation();

        setContentLeft(contentLeft);
    }

    /**
     * Called when a drawer animation has successfully completed.
     */
    private void completeAnimation() {
        mScroller.abortAnimation();
        stopLayerTranslation();

        final int finalX = mScroller.getFinalX();
        setContentLeft(finalX);
        setDrawerState(finalX == 0 ? STATE_CLOSED : STATE_OPEN);
    }

    /**
     * Animates the drawer open or closed.
     *
     * @param open     Indicates whether the drawer should be opened.
     * @param velocity Optional velocity if called by releasing a drag event.
     */
    private void animateContent(boolean open, int velocity) {
        endDrag();

        final int startX = mContentLeft;
        int dx = open ? mMenuWidth - startX : startX;
        if (dx == 0) {
            setDrawerState(startX == 0 ? STATE_CLOSED : STATE_OPEN);
            stopLayerTranslation();
            return;
        }

        int duration;

        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000.f * Math.abs((float) dx / velocity));
        } else {
            duration = (int) (600.f * ((float) dx / mMenuWidth));
        }

        duration = Math.min(duration, DURATION_MAX);

        if (open) {
            setDrawerState(STATE_OPENING);
            mScroller.startScroll(startX, 0, dx, 0, duration);
        } else {
            setDrawerState(STATE_CLOSING);
            mScroller.startScroll(startX, 0, -startX, 0, duration);
        }

        startLayerTranslation();

        postAnimationInvalidate();
    }

    /**
     * Callback when each frame in the drawer animation should be drawn.
     */
    private void postAnimationInvalidate() {
        if (mScroller.computeScrollOffset()) {
            final int oldX = mContentLeft;
            final int x = mScroller.getCurrX();

            if (x != oldX) setContentLeft(x);
            if (x != mScroller.getFinalX()) {
                postDelayed(mDragRunnable, ANIMATION_DELAY);
                return;
            }
        }

        completeAnimation();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN && mMenuVisible && mContentLeft <= mCloseEnough) {
            setContentLeft(0);
            stopAnimation();
            setDrawerState(STATE_CLOSED);
        }

        // Always intercept events over the content while menu is visible.
        if (mMenuVisible && ev.getX() > mContentLeft) return true;

        if (action != MotionEvent.ACTION_DOWN) {
            if (mIsDragging) return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = ev.getY();
                final int contentLeft = mContentLeft;
                final boolean allowDrag = (!mMenuVisible && mInitialMotionX < mDragBezelSize) ||
                        (mMenuVisible && mInitialMotionX > contentLeft);

                if (allowDrag) {
                    setDrawerState(mMenuVisible ? STATE_OPEN : STATE_CLOSED);
                    stopAnimation();
                    mIsDragging = false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final float x = ev.getX();
                final float dx = x - mLastMotionX;
                final float xDiff = Math.abs(dx);
                final float y = ev.getY();
                final float yDiff = Math.abs(y - mLastMotionY);
                final int contentLeft = mContentLeft;

                if (xDiff > mTouchSlop && xDiff > yDiff) {
                    final boolean allowDrag = (!mMenuVisible && mInitialMotionX < mDragBezelSize)
                            || (mMenuVisible && mInitialMotionX >= contentLeft);
                    if (allowDrag) {
                        setDrawerState(STATE_DRAGGING);
                        mIsDragging = true;
                        mLastMotionX = x;
                        mLastMotionY = y;
                    }
                }
                break;
            }

            /**
             * If you click really fast, an up or cancel event is delivered here.
             * Just snap content to whatever is closest.
             * */
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                final int contentLeft = mContentLeft;
                animateContent(contentLeft > mMenuWidth / 2, 0);
                break;
            }
        }

        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);

        return mIsDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                final int contentLeft = mContentLeft;
                final boolean allowDrag = (!mMenuVisible && mInitialMotionX <= mDragBezelSize)
                        || (mMenuVisible && mInitialMotionX >= contentLeft);

                if (allowDrag) {
                    stopAnimation();
                    setDrawerState(STATE_DRAGGING);
                    stopAnimation();
                    mIsDragging = true;
                    startLayerTranslation();
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int contentLeft = mContentLeft;

                if (!mIsDragging) {
                    final float x = ev.getX();
                    final float xDiff = Math.abs(x - mLastMotionX);
                    final float y = ev.getY();
                    final float yDiff = Math.abs(y - mLastMotionY);

                    if (xDiff > mTouchSlop && xDiff > yDiff) {
                        final boolean allowDrag = (!mMenuVisible && mInitialMotionX <= mDragBezelSize)
                                || (mMenuVisible && mInitialMotionX >= contentLeft);

                        if (allowDrag) {
                            setDrawerState(STATE_DRAGGING);
                            mIsDragging = true;
                            mLastMotionX = x - mInitialMotionX > 0
                                    ? mInitialMotionX + mTouchSlop
                                    : mInitialMotionX - mTouchSlop;
                        }
                    }
                }

                if (mIsDragging) {
                    startLayerTranslation();

                    final float x = ev.getX();
                    final float dx = x - mLastMotionX;

                    mLastMotionX = x;
                    setContentLeft(Math.min(Math.max(contentLeft + (int) dx, 0), mMenuWidth));
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                final int contentLeft = mContentLeft;

                if (mIsDragging) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final int initialVelocity = (int) mVelocityTracker.getXVelocity();
                    mLastMotionX = ev.getX();
                    animateContent(mVelocityTracker.getXVelocity() > 0, initialVelocity);

                    // Close the menu when content is clicked while the menu is visible.
                } else if (mMenuVisible && ev.getX() > contentLeft) {
                    closeMenu();
                }
                break;
            }
        }

        return true;
    }

    /**
     * Saves the state of the drawer.
     *
     * @return Returns a Parcelable containing the drawer state.
     */
    public Parcelable saveState() {
        Bundle state = new Bundle();
        final boolean menuVisible = mDrawerState == STATE_OPEN || mDrawerState == STATE_OPENING;
        state.putBoolean(STATE_MENU_VISIBLE, menuVisible);
        return state;
    }

    /**
     * Restores the state of the drawer.
     *
     * @param in A parcelable containing the drawer state.
     */
    public void restoreState(Parcelable in) {
        Bundle state = (Bundle) in;
        final boolean menuOpen = state.getBoolean(STATE_MENU_VISIBLE);
        setContentLeft(menuOpen ? mMenuWidth : 0);
    }
}
