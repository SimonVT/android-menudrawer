package net.simonvt.menudrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class OverlayDrawer extends DraggableDrawer {

    private static final String TAG = "OverlayDrawer";

    private int mPeekSize;

    private Runnable mRevealRunnable = new Runnable() {
        @Override
        public void run() {
            cancelContentTouch();
            int animateTo = 0;
            switch (getPosition()) {
                case RIGHT:
                case BOTTOM:
                    animateTo = -mPeekSize;
                    break;

                default:
                    animateTo = mPeekSize;
                    break;
            }
            animateOffsetTo(animateTo, 250);
        }
    };

    OverlayDrawer(Activity activity, int dragMode) {
        super(activity, dragMode);
    }

    public OverlayDrawer(Context context) {
        super(context);
    }

    public OverlayDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverlayDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initDrawer(Context context, AttributeSet attrs, int defStyle) {
        super.initDrawer(context, attrs, defStyle);
        super.addView(mContentContainer, -1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        if (USE_TRANSLATIONS) {
            mContentContainer.setLayerType(View.LAYER_TYPE_NONE, null);
        }
        mContentContainer.setHardwareLayersEnabled(false);
        super.addView(mMenuContainer, -1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mPeekSize = dpToPx(20);
    }

    @Override
    protected void drawOverlay(Canvas canvas) {
        final int width = getWidth();
        final int height = getHeight();
        final int offsetPixels = (int) mOffsetPixels;
        final float openRatio = Math.abs(mOffsetPixels) / mMenuSize;

        switch (getPosition()) {
            case LEFT:
                mMenuOverlay.setBounds(offsetPixels, 0, width, height);
                break;

            case RIGHT:
                mMenuOverlay.setBounds(0, 0, width + offsetPixels, height);
                break;

            case TOP:
                mMenuOverlay.setBounds(0, offsetPixels, width, height);
                break;

            case BOTTOM:
                mMenuOverlay.setBounds(0, 0, width, height + offsetPixels);
                break;
        }

        mMenuOverlay.setAlpha((int) (MAX_MENU_OVERLAY_ALPHA * openRatio));
        mMenuOverlay.draw(canvas);
    }

    @Override
    public void openMenu(boolean animate) {
        int animateTo = 0;
        switch (getPosition()) {
            case LEFT:
            case TOP:
                animateTo = mMenuSize;
                break;

            case RIGHT:
            case BOTTOM:
                animateTo = -mMenuSize;
                break;
        }

        animateOffsetTo(animateTo, 0, animate);
    }

    @Override
    public void closeMenu(boolean animate) {
        animateOffsetTo(0, 0, animate);
    }

    @Override
    protected void onOffsetPixelsChanged(int offsetPixels) {
        if (USE_TRANSLATIONS) {
            switch (getPosition()) {
                case LEFT:
                    mMenuContainer.setTranslationX(offsetPixels - mMenuSize);
                    break;

                case TOP:
                    mMenuContainer.setTranslationY(offsetPixels - mMenuSize);
                    break;

                case RIGHT:
                    mMenuContainer.setTranslationX(offsetPixels + mMenuSize);
                    break;

                case BOTTOM:
                    mMenuContainer.setTranslationY(offsetPixels + mMenuSize);
                    break;
            }
        } else {
            switch (getPosition()) {
                case TOP:
                    mMenuContainer.offsetTopAndBottom(offsetPixels - mMenuContainer.getBottom());
                    break;

                case BOTTOM:
                    mMenuContainer.offsetTopAndBottom(offsetPixels - (mMenuContainer.getTop() - getHeight()));
                    break;

                case LEFT:
                    mMenuContainer.offsetLeftAndRight(offsetPixels - mMenuContainer.getRight());
                    break;

                case RIGHT:
                    mMenuContainer.offsetLeftAndRight(offsetPixels - (mMenuContainer.getLeft() - getWidth()));
                    break;
            }
        }

        invalidate();
    }

    @Override
    protected void initPeekScroller() {
        switch (getPosition()) {
            case RIGHT:
            case BOTTOM: {
                final int dx = -mPeekSize;
                mPeekScroller.startScroll(0, 0, dx, 0, PEEK_DURATION);
                break;
            }

            default: {
                final int dx = mPeekSize;
                mPeekScroller.startScroll(0, 0, dx, 0, PEEK_DURATION);
                break;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onOffsetPixelsChanged((int) mOffsetPixels);
    }

    @Override
    protected GradientDrawable.Orientation getDropShadowOrientation() {
        switch (getPosition()) {
            case TOP:
                return GradientDrawable.Orientation.TOP_BOTTOM;

            case RIGHT:
                return GradientDrawable.Orientation.RIGHT_LEFT;

            case BOTTOM:
                return GradientDrawable.Orientation.BOTTOM_TOP;

            default:
                return GradientDrawable.Orientation.LEFT_RIGHT;
        }
    }

    @Override
    protected void updateDropShadowRect() {
        final float openRatio = Math.abs(mOffsetPixels) / mMenuSize;
        final int dropShadowSize = (int) (mDropShadowSize * openRatio);

        switch (getPosition()) {
            case LEFT:
                mDropShadowRect.top = 0;
                mDropShadowRect.bottom = getHeight();
                mDropShadowRect.left = ViewHelper.getRight(mMenuContainer);
                mDropShadowRect.right = mDropShadowRect.left + dropShadowSize;
                break;

            case TOP:
                mDropShadowRect.left = 0;
                mDropShadowRect.right = getWidth();
                mDropShadowRect.top = ViewHelper.getBottom(mMenuContainer);
                mDropShadowRect.bottom = mDropShadowRect.top + dropShadowSize;
                break;

            case RIGHT:
                mDropShadowRect.top = 0;
                mDropShadowRect.bottom = getHeight();
                mDropShadowRect.right = ViewHelper.getLeft(mMenuContainer);
                mDropShadowRect.left = mDropShadowRect.right - dropShadowSize;
                break;

            case BOTTOM:
                mDropShadowRect.left = 0;
                mDropShadowRect.right = getWidth();
                mDropShadowRect.bottom = ViewHelper.getTop(mMenuContainer);
                mDropShadowRect.top = mDropShadowRect.bottom - dropShadowSize;
                break;
        }
    }

    @Override
    protected void startLayerTranslation() {
        if (USE_TRANSLATIONS && mHardwareLayersEnabled && !mLayerTypeHardware) {
            mLayerTypeHardware = true;
            mMenuContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    @Override
    protected void stopLayerTranslation() {
        if (mLayerTypeHardware) {
            mLayerTypeHardware = false;
            mMenuContainer.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;

        mContentContainer.layout(0, 0, width, height);

        if (USE_TRANSLATIONS) {
            switch (getPosition()) {
                case LEFT:
                    mMenuContainer.layout(0, 0, mMenuSize, height);
                    break;

                case RIGHT:
                    mMenuContainer.layout(width - mMenuSize, 0, width, height);
                    break;

                case TOP:
                    mMenuContainer.layout(0, 0, width, mMenuSize);
                    break;

                case BOTTOM:
                    mMenuContainer.layout(0, height - mMenuSize, width, height);
                    break;
            }

        } else {
            final int offsetPixels = (int) mOffsetPixels;
            final int menuSize = mMenuSize;

            switch (getPosition()) {
                case LEFT:
                    mMenuContainer.layout(-menuSize + offsetPixels, 0, offsetPixels, height);
                    break;

                case RIGHT:
                    mMenuContainer.layout(width + offsetPixels, 0, width + menuSize + offsetPixels, height);
                    break;

                case TOP:
                    mMenuContainer.layout(0, -menuSize + offsetPixels, width, offsetPixels);
                    break;

                case BOTTOM:
                    mMenuContainer.layout(0, height + offsetPixels, width, height + menuSize + offsetPixels);
                    break;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
            throw new IllegalStateException("Must measure with an exact size");
        }

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        if (mOffsetPixels == -1) openMenu(false);

        int menuWidthMeasureSpec;
        int menuHeightMeasureSpec;
        switch (getPosition()) {
            case TOP:
            case BOTTOM:
                menuWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width);
                menuHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, mMenuSize);
                break;

            default:
                // LEFT/RIGHT
                menuWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, mMenuSize);
                menuHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
        }
        mMenuContainer.measure(menuWidthMeasureSpec, menuHeightMeasureSpec);

        final int contentWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width);
        final int contentHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
        mContentContainer.measure(contentWidthMeasureSpec, contentHeightMeasureSpec);

        setMeasuredDimension(width, height);

        updateTouchAreaSize();
    }

    private boolean isContentTouch(int x, int y) {
        boolean contentTouch = false;

        switch (getPosition()) {
            case LEFT:
                contentTouch = ViewHelper.getRight(mMenuContainer) < x;
                break;

            case RIGHT:
                contentTouch = ViewHelper.getLeft(mMenuContainer) > x;
                break;

            case TOP:
                contentTouch = ViewHelper.getBottom(mMenuContainer) < y;
                break;

            case BOTTOM:
                contentTouch = ViewHelper.getTop(mMenuContainer) > y;
                break;
        }

        return contentTouch;
    }

    protected boolean onDownAllowDrag(int x, int y) {
        switch (getPosition()) {
            case LEFT:
                return (!mMenuVisible && mInitialMotionX <= mTouchSize)
                        || (mMenuVisible && mInitialMotionX <= mOffsetPixels);

            case RIGHT:
                final int width = getWidth();
                final int initialMotionX = (int) mInitialMotionX;

                return (!mMenuVisible && initialMotionX >= width - mTouchSize)
                        || (mMenuVisible && initialMotionX >= width + mOffsetPixels);

            case TOP:
                return (!mMenuVisible && mInitialMotionY <= mTouchSize)
                        || (mMenuVisible && mInitialMotionY <= mOffsetPixels);

            case BOTTOM:
                final int height = getHeight();
                return (!mMenuVisible && mInitialMotionY >= height - mTouchSize)
                        || (mMenuVisible && mInitialMotionY >= height + mOffsetPixels);
        }

        return false;
    }

    protected boolean onMoveAllowDrag(int x, int y, float dx, float dy) {
        if (mMenuVisible && mTouchMode == TOUCH_MODE_FULLSCREEN) {
            return true;
        }

        switch (getPosition()) {
            case LEFT:
                return (!mMenuVisible && mInitialMotionX <= mTouchSize && (dx > 0)) // Drawer closed
                        || (mMenuVisible && x <= mOffsetPixels) // Drawer open
                        || (Math.abs(mOffsetPixels) <= mPeekSize && mMenuVisible); // Drawer revealed

            case RIGHT:
                final int width = getWidth();
                return (!mMenuVisible && mInitialMotionX >= width - mTouchSize && (dx < 0))
                        || (mMenuVisible && x >= width - mOffsetPixels)
                        || (Math.abs(mOffsetPixels) <= mPeekSize && mMenuVisible);

            case TOP:
                return (!mMenuVisible && mInitialMotionY <= mTouchSize && (dy > 0))
                        || (mMenuVisible && x <= mOffsetPixels)
                        || (Math.abs(mOffsetPixels) <= mPeekSize && mMenuVisible);

            case BOTTOM:
                final int height = getHeight();
                return (!mMenuVisible && mInitialMotionY >= height - mTouchSize && (dy < 0))
                        || (mMenuVisible && x >= height - mOffsetPixels)
                        || (Math.abs(mOffsetPixels) <= mPeekSize && mMenuVisible);
        }

        return false;
    }

    protected void onMoveEvent(float dx, float dy) {
        switch (getPosition()) {
            case LEFT:
                setOffsetPixels(Math.min(Math.max(mOffsetPixels + dx, 0), mMenuSize));
                break;

            case RIGHT:
                setOffsetPixels(Math.max(Math.min(mOffsetPixels + dx, 0), -mMenuSize));
                break;

            case TOP:
                setOffsetPixels(Math.min(Math.max(mOffsetPixels + dy, 0), mMenuSize));
                break;

            case BOTTOM:
                setOffsetPixels(Math.max(Math.min(mOffsetPixels + dy, 0), -mMenuSize));
                break;
        }
    }

    protected void onUpEvent(int x, int y) {
        final int offsetPixels = (int) mOffsetPixels;

        switch (getPosition()) {
            case LEFT: {
                if (mIsDragging) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final int initialVelocity = (int) getXVelocity(mVelocityTracker);
                    mLastMotionX = x;
                    animateOffsetTo(initialVelocity > 0 ? mMenuSize : 0, initialVelocity, true);

                    // Close the menu when content is clicked while the menu is visible.
                } else if (mMenuVisible) {
                    closeMenu();
                }
                break;
            }

            case TOP: {
                if (mIsDragging) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final int initialVelocity = (int) getYVelocity(mVelocityTracker);
                    mLastMotionY = y;
                    animateOffsetTo(initialVelocity > 0 ? mMenuSize : 0, initialVelocity, true);

                    // Close the menu when content is clicked while the menu is visible.
                } else if (mMenuVisible) {
                    closeMenu();
                }
                break;
            }

            case RIGHT: {
                final int width = getWidth();

                if (mIsDragging) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final int initialVelocity = (int) getXVelocity(mVelocityTracker);
                    mLastMotionX = x;
                    animateOffsetTo(initialVelocity > 0 ? 0 : -mMenuSize, initialVelocity, true);

                    // Close the menu when content is clicked while the menu is visible.
                } else if (mMenuVisible) {
                    closeMenu();
                }
                break;
            }

            case BOTTOM: {
                if (mIsDragging) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final int initialVelocity = (int) getYVelocity(mVelocityTracker);
                    mLastMotionY = y;
                    animateOffsetTo(initialVelocity < 0 ? -mMenuSize : 0, initialVelocity, true);

                    // Close the menu when content is clicked while the menu is visible.
                } else if (mMenuVisible) {
                    closeMenu();
                }
                break;
            }
        }
    }

    protected boolean checkTouchSlop(float dx, float dy) {
        switch (getPosition()) {
            case TOP:
            case BOTTOM:
                return Math.abs(dy) > mTouchSlop && Math.abs(dy) > Math.abs(dx);

            default:
                return Math.abs(dx) > mTouchSlop && Math.abs(dx) > Math.abs(dy);
        }
    }

    @Override
    protected void stopAnimation() {
        super.stopAnimation();
        removeCallbacks(mRevealRunnable);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        removeCallbacks(mRevealRunnable);
        if (mIsPeeking) {
            endPeek();
            animateOffsetTo(0, PEEK_DURATION);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            removeCallbacks(mRevealRunnable);
            mActivePointerId = INVALID_POINTER;
            mIsDragging = false;
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }

            if (Math.abs(mOffsetPixels) > mMenuSize / 2) {
                openMenu();
            } else {
                closeMenu();
            }

            return false;
        }

        if (action == MotionEvent.ACTION_DOWN && mMenuVisible && isCloseEnough()) {
            setOffsetPixels(0);
            stopAnimation();
            endPeek();
            setDrawerState(STATE_CLOSED);
            mIsDragging = false;
        }

        // Always intercept events over the content while menu is visible.
        if (mMenuVisible) {
            int index = 0;
            if (mActivePointerId != INVALID_POINTER) {
                index = ev.findPointerIndex(mActivePointerId);
                index = index == -1 ? 0 : index;
            }

            final int x = (int) ev.getX(index);
            final int y = (int) ev.getY(index);
            if (isContentTouch(x, y)) {
                return true;
            }
        }

        if (!mMenuVisible && !mIsDragging && mTouchMode == TOUCH_MODE_NONE) {
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN && mIsDragging) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                final boolean allowDrag = onDownAllowDrag((int) mLastMotionX, (int) mLastMotionY);
                mActivePointerId = ev.getPointerId(0);

                if (allowDrag) {
                    setDrawerState(mMenuVisible ? STATE_OPEN : STATE_CLOSED);
                    stopAnimation();
                    endPeek();

                    if (!mMenuVisible && mInitialMotionX <= mPeekSize) {
                        postDelayed(mRevealRunnable, 160);
                    }

                    mIsDragging = false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    mIsDragging = false;
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                    closeMenu(true);
                    return false;
                }

                final float x = ev.getX(pointerIndex);
                final float dx = x - mLastMotionX;
                final float y = ev.getY(pointerIndex);
                final float dy = y - mLastMotionY;

                if (Math.abs(dx) >= mTouchSlop || Math.abs(dy) >= mTouchSlop) {
                    removeCallbacks(mRevealRunnable);
                    endPeek();
                }

                if (checkTouchSlop(dx, dy)) {
                    if (mOnInterceptMoveEventListener != null && (mTouchMode == TOUCH_MODE_FULLSCREEN || mMenuVisible)
                            && canChildrenScroll((int) dx, (int) dy, (int) x, (int) y)) {
                        endDrag(); // Release the velocity tracker
                        requestDisallowInterceptTouchEvent(true);
                        return false;
                    }

                    final boolean allowDrag = onMoveAllowDrag((int) x, (int) y, dx, dy);

                    if (allowDrag) {
                        endPeek();
                        stopAnimation();
                        setDrawerState(STATE_DRAGGING);
                        mIsDragging = true;
                        mLastMotionX = x;
                        mLastMotionY = y;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(ev);
                mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }

        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);

        return mIsDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mMenuVisible && !mIsDragging && mTouchMode == TOUCH_MODE_NONE) {
            return false;
        }
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                final boolean allowDrag = onDownAllowDrag((int) mLastMotionX, (int) mLastMotionY);

                mActivePointerId = ev.getPointerId(0);

                if (allowDrag) {
                    stopAnimation();
                    endPeek();

                    if (!mMenuVisible && mLastMotionX <= mPeekSize) {
                        postDelayed(mRevealRunnable, 160);
                    }

                    startLayerTranslation();
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex == -1) {
                    mIsDragging = false;
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                    closeMenu(true);
                    return false;
                }

                if (!mIsDragging) {
                    final float x = ev.getX(pointerIndex);
                    final float dx = x - mLastMotionX;
                    final float y = ev.getY(pointerIndex);
                    final float dy = y - mLastMotionY;

                    if (checkTouchSlop(dx, dy)) {
                        final boolean allowDrag = onMoveAllowDrag((int) x, (int) y, dx, dy);

                        if (allowDrag) {
                            endPeek();
                            stopAnimation();
                            setDrawerState(STATE_DRAGGING);
                            mIsDragging = true;
                            mLastMotionX = x;
                            mLastMotionY = y;
                        } else {
                            mInitialMotionX = x;
                            mInitialMotionY = y;
                        }
                    }
                }

                if (mIsDragging) {
                    startLayerTranslation();

                    final float x = ev.getX(pointerIndex);
                    final float dx = x - mLastMotionX;
                    final float y = ev.getY(pointerIndex);
                    final float dy = y - mLastMotionY;

                    mLastMotionX = x;
                    mLastMotionY = y;
                    onMoveEvent(dx, dy);
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                removeCallbacks(mRevealRunnable);
                int index = ev.findPointerIndex(mActivePointerId);
                index = index == -1 ? 0 : index;
                final int x = (int) ev.getX(index);
                final int y = (int) ev.getY(index);
                onUpEvent(x, y);
                mActivePointerId = INVALID_POINTER;
                mIsDragging = false;
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN:
                final int index = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                mLastMotionX = ev.getX(index);
                mLastMotionY = ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(ev);
                mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }

        return true;
    }

    private void onPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }
}
