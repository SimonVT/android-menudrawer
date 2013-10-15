package net.simonvt.menudrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class StaticDrawer extends MenuDrawer {

    public StaticDrawer(Context context) {
        super(context);
    }

    public StaticDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StaticDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initDrawer(Context context, AttributeSet attrs, int defStyle) {
        super.initDrawer(context, attrs, defStyle);
        super.addView(mMenuContainer, -1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        super.addView(mContentContainer, -1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mIsStatic = true;
    }

    @Override
    protected void drawOverlay(Canvas canvas) {
        // NO-OP
    }

    @Override
    protected void onOffsetPixelsChanged(int offsetPixels) {
        // NO-OP
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;

        switch (getPosition()) {
            case LEFT:
                mMenuContainer.layout(0, 0, mMenuSize, height);
                mContentContainer.layout(mMenuSize, 0, width, height);
                break;

            case RIGHT:
                mMenuContainer.layout(width - mMenuSize, 0, width, height);
                mContentContainer.layout(0, 0, width - mMenuSize, height);
                break;

            case TOP:
                mMenuContainer.layout(0, 0, width, mMenuSize);
                mContentContainer.layout(0, mMenuSize, width, height);
                break;

            case BOTTOM:
                mMenuContainer.layout(0, height - mMenuSize, width, height);
                mContentContainer.layout(0, 0, width, height - mMenuSize);
                break;
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

        switch (getPosition()) {
            case LEFT:
            case RIGHT: {
                final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

                final int menuWidth = mMenuSize;
                final int menuWidthMeasureSpec = MeasureSpec.makeMeasureSpec(menuWidth, MeasureSpec.EXACTLY);

                final int contentWidth = width - menuWidth;
                final int contentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY);

                mContentContainer.measure(contentWidthMeasureSpec, childHeightMeasureSpec);
                mMenuContainer.measure(menuWidthMeasureSpec, childHeightMeasureSpec);
                break;
            }

            case TOP:
            case BOTTOM: {
                final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

                final int menuHeight = mMenuSize;
                final int menuHeightMeasureSpec = MeasureSpec.makeMeasureSpec(menuHeight, MeasureSpec.EXACTLY);

                final int contentHeight = height - menuHeight;
                final int contentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY);

                mContentContainer.measure(childWidthMeasureSpec, contentHeightMeasureSpec);
                mMenuContainer.measure(childWidthMeasureSpec, menuHeightMeasureSpec);
                break;
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    public void toggleMenu(boolean animate) {
        // NO-OP
    }

    @Override
    public void openMenu(boolean animate) {
        // NO-OP
    }

    @Override
    public void closeMenu(boolean animate) {
        // NO-OP
    }

    @Override
    public boolean isMenuVisible() {
        return true;
    }

    @Override
    public void setMenuSize(int size) {
        mMenuSize = size;
        requestLayout();
        invalidate();
    }

    @Override
    public void setOffsetMenuEnabled(boolean offsetMenu) {
        // NO-OP
    }

    @Override
    public boolean getOffsetMenuEnabled() {
        return false;
    }

    @Override
    public void peekDrawer() {
        // NO-OP
    }

    @Override
    public void peekDrawer(long delay) {
        // NO-OP
    }

    @Override
    public void peekDrawer(long startDelay, long delay) {
        // NO-OP
    }

    @Override
    public void setHardwareLayerEnabled(boolean enabled) {
        // NO-OP
    }

    @Override
    public int getTouchMode() {
        return TOUCH_MODE_NONE;
    }

    @Override
    public void setTouchMode(int mode) {
        // NO-OP
    }

    @Override
    public void setTouchBezelSize(int size) {
        // NO-OP
    }

    @Override
    public int getTouchBezelSize() {
        return 0;
    }

    @Override
    public void setSlideDrawable(int drawableRes) {
        // NO-OP
    }

    @Override
    public void setSlideDrawable(Drawable drawable) {
        // NO-OP
    }

    @Override
    public void setupUpIndicator(Activity activity) {
        // NO-OP
    }

    @Override
    public void setDrawerIndicatorEnabled(boolean enabled) {
        // NO-OP
    }

    @Override
    public boolean isDrawerIndicatorEnabled() {
        return false;
    }
}
