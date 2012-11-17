package net.simonvt.widget;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;

public class TopDrawer extends HorizontalMenuDrawer {

    TopDrawer(Activity activity, int dragMode) {
        super(activity, dragMode);
    }

    @Override
    public void setDropShadowColor(int color) {
        final int endColor = color & 0x00FFFFFF;
        mDropShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
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
        final int offsetPixels = mOffsetPixels;

        mMenuContainer.layout(0, 0, width, mMenuSize);
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
            final int menuSize = mMenuSize;
            final float openRatio = (menuSize - (float) offsetPixels) / menuSize;

            if (USE_TRANSLATIONS) {
                final int offset = (int) (0.25f * (-openRatio * menuSize));
                mMenuContainer.setTranslationY(offset);

            } else {
                final int oldMenuTop = mMenuContainer.getTop();
                final int offset = (int) (0.25f * (-openRatio * menuSize)) - oldMenuTop;
                mMenuContainer.offsetTopAndBottom(offset);
            }
        }
    }

    @Override
    protected void drawDropShadow(Canvas canvas, int offsetPixels) {
        final int width = getWidth();

        mDropShadowDrawable.setBounds(0, offsetPixels - mDropShadowSize, width, offsetPixels);
        mDropShadowDrawable.draw(canvas);
    }

    @Override
    protected void drawMenuOverlay(Canvas canvas, int offsetPixels) {
        final int width = getWidth();
        final float openRatio = ((float) offsetPixels) / mMenuSize;

        mMenuOverlay.setBounds(0, 0, width, offsetPixels);
        mMenuOverlay.setAlpha((int) (MAX_MENU_OVERLAY_ALPHA * (1.f - openRatio)));
        mMenuOverlay.draw(canvas);
    }

    @Override
    protected void drawArrow(Canvas canvas, int offsetPixels) {
        // Not implemented yet..
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

    @Override
    protected boolean isContentTouch(MotionEvent ev) {
        return ev.getY() > mOffsetPixels;
    }

    @Override
    protected boolean onDownAllowDrag(MotionEvent ev) {
        return (!mMenuVisible && mInitialMotionY <= mTouchSize)
                || (mMenuVisible && mInitialMotionY >= mOffsetPixels);
    }

    @Override
    protected boolean onMoveAllowDrag(MotionEvent ev, float diff) {
        return (!mMenuVisible && mInitialMotionY <= mTouchSize && (diff > 0))
                || (mMenuVisible && mInitialMotionY >= mOffsetPixels);
    }

    @Override
    protected void onMoveEvent(float dx) {
        setOffsetPixels(Math.min(Math.max(mOffsetPixels + (int) dx, 0), mMenuSize));
    }

    @Override
    protected void onUpEvent(MotionEvent ev) {
        final int offsetPixels = mOffsetPixels;

        if (mIsDragging) {
            mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
            final int initialVelocity = (int) mVelocityTracker.getXVelocity();
            mLastMotionY = ev.getY();
            animateOffsetTo(mVelocityTracker.getYVelocity() > 0 ? mMenuSize : 0, initialVelocity,
                    true);

            // Close the menu when content is clicked while the menu is visible.
        } else if (mMenuVisible && ev.getY() > offsetPixels) {
            closeMenu();
        }
    }
}
