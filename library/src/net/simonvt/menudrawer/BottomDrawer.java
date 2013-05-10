package net.simonvt.menudrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class BottomDrawer extends VerticalDrawer {

    BottomDrawer(Activity activity, int dragMode) {
        super(activity, dragMode);
    }

    public BottomDrawer(Context context) {
        super(context);
    }

    public BottomDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void openMenu(boolean animate) {
        animateOffsetTo(-mMenuSize, 0, animate);
    }

    @Override
    public void closeMenu(boolean animate) {
        animateOffsetTo(0, 0, animate);
    }

    @Override
    public void setDropShadowColor(int color) {
        final int endColor = color & 0x00FFFFFF;
        mDropShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {
                        color,
                        endColor,
                });
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;
        final int offsetPixels = (int) mOffsetPixels;
        final int menuSize = mMenuSize;

        mMenuContainer.layout(0, height - menuSize, width, height);
        offsetMenu(offsetPixels);

        if (USE_TRANSLATIONS) {
            mContentContainer.layout(0, 0, width, height);
        } else {
            mContentContainer.layout(0, offsetPixels, width, height + offsetPixels);
        }
    }

    /**
     * Offsets the menu relative to its original position based on the position of the content.
     *
     * @param offsetPixels The number of pixels the content if offset.
     */
    private void offsetMenu(int offsetPixels) {
        if (mOffsetMenu && mMenuSize != 0) {
            final int height = getHeight();
            final int menuSize = mMenuSize;
            final float openRatio = (menuSize + (float) offsetPixels) / menuSize;

            if (USE_TRANSLATIONS) {
                if (offsetPixels != 0) {
                    final int offset = (int) (0.25f * (openRatio * menuSize));
                    mMenuContainer.setTranslationY(offset);
                } else {
                    mMenuContainer.setTranslationY(height + menuSize);
                }

            } else {
                final int oldMenuTop = mMenuContainer.getTop();
                final int offsetBy = (int) (0.25f * (openRatio * menuSize));
                final int offset = height - mMenuSize + offsetBy - oldMenuTop;
                mMenuContainer.offsetTopAndBottom(offset);
                mMenuContainer.setVisibility(offsetPixels == 0 ? INVISIBLE : VISIBLE);
            }
        }
    }

    @Override
    protected void drawMenuOverlay(Canvas canvas, int offsetPixels) {
        final int width = getWidth();
        final int height = getHeight();
        final float openRatio = ((float) Math.abs(offsetPixels)) / mMenuSize;

        mMenuOverlay.setBounds(0, height + offsetPixels, width, height);
        mMenuOverlay.setAlpha((int) (MAX_MENU_OVERLAY_ALPHA * (1.f - openRatio)));
        mMenuOverlay.draw(canvas);
    }

    @Override
    protected void initPeekScroller() {
        final int dx = -mMenuSize / 3;
        mPeekScroller.startScroll(0, 0, dx, 0, PEEK_DURATION);
    }

    @Override
    protected void onOffsetPixelsChanged(int offsetPixels) {
        if (USE_TRANSLATIONS) {
            mContentContainer.setTranslationY(offsetPixels);
            offsetMenu(offsetPixels);
            invalidate();
        } else {
            mContentContainer.offsetTopAndBottom(offsetPixels - mContentContainer.getTop());
            offsetMenu(offsetPixels);
            invalidate();
        }
    }

    //////////////////////////////////////////////////////////////////////
    // Touch handling
    //////////////////////////////////////////////////////////////////////

    @Override
    protected boolean isContentTouch(MotionEvent ev) {
        return ev.getY() < getHeight() + mOffsetPixels;
    }

    @Override
    protected boolean onDownAllowDrag(MotionEvent ev) {
        final int height = getHeight();
        return (!mMenuVisible && mInitialMotionY >= height - mTouchSize)
                || (mMenuVisible && mInitialMotionY <= height + mOffsetPixels);
    }

    @Override
    protected boolean onMoveAllowDrag(MotionEvent ev, float diff) {
        final int height = getHeight();
        return (!mMenuVisible && mInitialMotionY >= height - mTouchSize && (diff < 0))
                || (mMenuVisible && mInitialMotionY <= height + mOffsetPixels);
    }

    @Override
    protected void onMoveEvent(float dx) {
        setOffsetPixels(Math.max(Math.min(mOffsetPixels + dx, 0), -mMenuSize));
    }

    @Override
    protected void onUpEvent(MotionEvent ev) {
        final int offsetPixels = (int) mOffsetPixels;
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);

        if (mIsDragging) {
            mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
            final int initialVelocity = (int) mVelocityTracker.getYVelocity(mActivePointerId);
            mLastMotionY = ev.getY(pointerIndex);
            animateOffsetTo(initialVelocity < 0 ? -mMenuSize : 0, initialVelocity, true);

            // Close the menu when content is clicked while the menu is visible.
        } else if (mMenuVisible && ev.getY(pointerIndex) < getHeight() + offsetPixels) {
            closeMenu();
        }
    }
}
