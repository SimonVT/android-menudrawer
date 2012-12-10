package net.simonvt.widget;

import net.simonvt.menudrawer.R;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;

public class RightDrawer extends MenuDrawer {

    RightDrawer(Activity activity, int dragMode) {
        super(activity, dragMode);
    }

    @Override
    public void setDropShadowColor(int color) {
        final int endColor = color & 0x00FFFFFF;
        mDropShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[] {
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

        mMenuContainer.layout(width - mMenuSize, 0, width, height);
        offsetMenu(offsetPixels);

        if (USE_TRANSLATIONS) {
            mContentContainer.layout(0, 0, width, height);
        } else {
            mContentContainer.layout(-offsetPixels, 0, width - offsetPixels, height);
        }
    }

    /**
     * Offsets the menu relative to its original position based on the position of the content.
     *
     * @param offsetPixels The number of pixels the content if offset.
     */
    private void offsetMenu(int offsetPixels) {
        if (mOffsetMenu && mMenuSize != 0) {
            final int menuWidth = mMenuSize;
            final float openRatio = (menuWidth - (float) offsetPixels) / menuWidth;

            if (USE_TRANSLATIONS) {
                if (offsetPixels > 0) {
                    final int offset = (int) (0.25f * (openRatio * menuWidth));
                    mMenuContainer.setTranslationX(offset);
                } else {
                    mMenuContainer.setTranslationX(menuWidth);
                }

            } else {
                final int width = getWidth();
                final int oldMenuRight = mMenuContainer.getRight();
                final int newRight = width + (int) (0.25f * (openRatio * menuWidth));
                final int offset = newRight - oldMenuRight;
                mMenuContainer.offsetLeftAndRight(offset);
                mMenuContainer.setVisibility(offsetPixels == 0 ? INVISIBLE : VISIBLE);
            }
        }
    }

    @Override
    protected void drawDropShadow(Canvas canvas, int offsetPixels) {
        final int height = getHeight();
        final int width = getWidth();
        final int left = width - offsetPixels;
        final int right = left + mDropShadowSize;

        mDropShadowDrawable.setBounds(left, 0, right, height);
        mDropShadowDrawable.draw(canvas);
    }

    @Override
    protected void drawMenuOverlay(Canvas canvas, int offsetPixels) {
        final int height = getHeight();
        final int width = getWidth();
        final int left = width - offsetPixels;
        final int right = width;
        final float openRatio = ((float) offsetPixels) / mMenuSize;

        mMenuOverlay.setBounds(left, 0, right, height);
        mMenuOverlay.setAlpha((int) (MAX_MENU_OVERLAY_ALPHA * (1.f - openRatio)));
        mMenuOverlay.draw(canvas);
    }

    @Override
    protected void drawIndicator(Canvas canvas, int offsetPixels) {
        if (mActiveView != null && mActiveView.getParent() != null) {
            Integer position = (Integer) mActiveView.getTag(R.id.mdActiveViewPosition);
            final int pos = position == null ? 0 : position;

            if (pos == mActivePosition) {
                final int width = getWidth();
                final int menuWidth = mMenuSize;
                final int indicatorWidth = mActiveIndicator.getWidth();

                final int contentRight = width - offsetPixels;
                final float openRatio = ((float) offsetPixels) / menuWidth;

                mActiveView.getDrawingRect(mActiveRect);
                offsetDescendantRectToMyCoords(mActiveView, mActiveRect);

                final float interpolatedRatio = 1.f - INDICATOR_INTERPOLATOR.getInterpolation((1.f - openRatio));
                final int interpolatedWidth = (int) (indicatorWidth * interpolatedRatio);

                final int indicatorRight = contentRight + interpolatedWidth;
                final int indicatorLeft = indicatorRight - indicatorWidth;

                final int top = mActiveRect.top + ((mActiveRect.height() - mActiveIndicator.getHeight()) / 2);

                canvas.save();
                canvas.clipRect(contentRight, 0, indicatorRight, getHeight());
                canvas.drawBitmap(mActiveIndicator, indicatorLeft, top, null);
                canvas.restore();
            }
        }
    }

    @Override
    protected void onOffsetPixelsChanged(int offsetPixels) {
        if (USE_TRANSLATIONS) {
            mContentContainer.setTranslationX(-offsetPixels);
            offsetMenu(offsetPixels);
            invalidate();
        } else {
            mContentContainer.offsetLeftAndRight(-offsetPixels - mContentContainer.getLeft());
            offsetMenu(offsetPixels);
            invalidate();
        }
    }

    @Override
    protected boolean isContentTouch(MotionEvent ev) {
        return ev.getX() < getWidth() - mOffsetPixels;
    }

    @Override
    protected boolean onDownAllowDrag(MotionEvent ev) {
        final int width = getWidth();
        final int initialMotionX = (int) mInitialMotionX;

        return (!mMenuVisible && initialMotionX >= width - mTouchSize)
                || (mMenuVisible && initialMotionX <= width - mOffsetPixels);
    }

    @Override
    protected boolean onMoveAllowDrag(MotionEvent ev, float diff) {
        final int width = getWidth();
        final int initialMotionX = (int) mInitialMotionX;

        return (!mMenuVisible && initialMotionX >= width - mTouchSize && (diff < 0))
                || (mMenuVisible && initialMotionX <= width - mOffsetPixels);
    }

    @Override
    protected void onMoveEvent(float dx) {
        setOffsetPixels(Math.min(Math.max(mOffsetPixels - (int) dx, 0), mMenuSize));
    }

    @Override
    protected void onUpEvent(MotionEvent ev) {
        final int offsetPixels = mOffsetPixels;
        final int width = getWidth();

        if (mIsDragging) {
            mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
            final int initialVelocity = (int) mVelocityTracker.getXVelocity();
            mLastMotionX = ev.getX();
            animateOffsetTo(mVelocityTracker.getXVelocity() > 0 ? 0 : mMenuSize, initialVelocity, true);

            // Close the menu when content is clicked while the menu is visible.
        } else if (mMenuVisible && ev.getX() < width - offsetPixels) {
            closeMenu();
        }
    }
}
