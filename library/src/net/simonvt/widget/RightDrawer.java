package net.simonvt.widget;

import net.simonvt.menudrawer.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class RightDrawer extends MenuDrawer {

    private static final String TAG = "RightDrawer";

    public RightDrawer(Context context) {
        super(context);
    }

    public RightDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RightDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

        mMenuContainer.layout(width - mMenuWidth, 0, width, height);
        offsetMenu(offsetPixels);

        if (USE_TRANSLATIONS) {
            mContentView.layout(0, 0, width, height);
        } else {
            mContentView.layout(-offsetPixels, 0, width - offsetPixels, height);
        }
    }

    /**
     * Offsets the menu relative to its original position based on the position of the content.
     *
     * @param offsetPixels The number of pixels the content if offset.
     */
    private void offsetMenu(int offsetPixels) {
        if (mOffsetMenu && mMenuWidth != 0) {
            final int menuWidth = mMenuWidth;
            final float openRatio = (menuWidth - (float) offsetPixels) / menuWidth;

            if (USE_TRANSLATIONS) {
                final int offset = (int) (0.25f * (openRatio * menuWidth));
                mMenuContainer.setTranslationX(offset);

            } else {
                final int width = getWidth();
                final int oldMenuRight = mMenuContainer.getRight();
                final int newRight = width + (int) (0.25f * (openRatio * menuWidth));
                final int offset = newRight - oldMenuRight;
                mMenuContainer.offsetLeftAndRight(offset);
            }
        }
    }

    @Override
    protected void drawDropShadow(Canvas canvas, int offsetPixels) {
        final int height = getHeight();
        final int width = getWidth();
        final int left = width - offsetPixels;
        final int right = left + mDropShadowWidth;

        mDropShadowDrawable.setBounds(left, 0, right, height);
        mDropShadowDrawable.draw(canvas);
    }

    @Override
    protected void drawMenuOverlay(Canvas canvas, int offsetPixels) {
        final int height = getHeight();
        final int width = getWidth();
        final int left = width - offsetPixels;
        final int right = width;
        final float openRatio = ((float) offsetPixels) / mMenuWidth;

        mMenuOverlay.setBounds(left, 0, right, height);
        mMenuOverlay.setAlpha((int) (MAX_MENU_OVERLAY_ALPHA * (1.f - openRatio)));
        mMenuOverlay.draw(canvas);
    }

    @Override
    protected void drawArrow(Canvas canvas, int offsetPixels) {
        if (mActiveView != null && mActiveView.getParent() != null) {
            Integer position = (Integer) mActiveView.getTag(R.id.mdActiveViewPosition);
            final int pos = position == null ? 0 : position;

            if (pos == mActivePosition) {
                final int width = getWidth();
                final int menuWidth = mMenuWidth;
                final int arrowWidth = mArrowBitmap.getWidth();

                final int contentRight = width - offsetPixels;
                final float openRatio = ((float) offsetPixels) / menuWidth;

                mActiveView.getDrawingRect(mActiveRect);
                offsetDescendantRectToMyCoords(mActiveView, mActiveRect);

                final float interpolatedRatio = 1.f - ARROW_INTERPOLATOR.getInterpolation((1.f - openRatio));
                final int interpolatedArrowWidth = (int) (arrowWidth * interpolatedRatio);

                final int arrowRight = contentRight + interpolatedArrowWidth;
                final int arrowLeft = arrowRight - arrowWidth;

                final int top = mActiveRect.top + ((mActiveRect.height() - mArrowBitmap.getHeight()) / 2);

                canvas.save();
                canvas.clipRect(contentRight, 0, arrowRight, getHeight());
                canvas.drawBitmap(mArrowBitmap, arrowLeft, top, null);
                canvas.restore();
            }
        }
    }

    @Override
    protected void onOffsetPixelsChanged(int offsetPixels) {
        if (USE_TRANSLATIONS) {
            mContentView.setTranslationX(-offsetPixels);
            offsetMenu(offsetPixels);
            invalidate();
        } else {
            mContentView.offsetLeftAndRight(-offsetPixels - mContentView.getLeft());
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

        return (!mMenuVisible && initialMotionX >= width - mDragBezelSize)
                || (mMenuVisible && initialMotionX <= width - mOffsetPixels);
    }

    @Override
    protected boolean onMoveAllowDrag(MotionEvent ev) {
        final int width = getWidth();
        final int initialMotionX = (int) mInitialMotionX;

        return (!mMenuVisible && initialMotionX >= width - mDragBezelSize)
                || (mMenuVisible && initialMotionX <= width - mOffsetPixels);
    }

    @Override
    protected void onMoveEvent(float dx) {
        setOffsetPixels(Math.min(Math.max(mOffsetPixels - (int) dx, 0), mMenuWidth));
    }

    @Override
    protected void onUpEvent(MotionEvent ev) {
        final int offsetPixels = mOffsetPixels;
        final int width = getWidth();

        if (mIsDragging) {
            mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
            final int initialVelocity = (int) mVelocityTracker.getXVelocity();
            mLastMotionX = ev.getX();
            animateOffsetTo(mVelocityTracker.getXVelocity() > 0 ? 0 : mMenuWidth, initialVelocity, true);

            // Close the menu when content is clicked while the menu is visible.
        } else if (mMenuVisible && ev.getX() < width - offsetPixels) {
            closeMenu();
        }
    }
}
