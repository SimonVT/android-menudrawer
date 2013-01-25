package net.simonvt.menudrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

public class BottomStaticDrawer extends StaticDrawer {

    BottomStaticDrawer(Activity activity, int dragMode) {
        super(activity, dragMode);
    }

    public BottomStaticDrawer(Context context) {
        super(context);
    }

    public BottomStaticDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomStaticDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initDrawer(Context context, AttributeSet attrs, int defStyle) {
        super.initDrawer(context, attrs, defStyle);
        mPosition = Position.BOTTOM;
    }

    @Override
    public void setDropShadowColor(int color) {
        final int endColor = color & 0x00FFFFFF;
        mDropShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
                color,
                endColor,
        });
        invalidate();
    }
}
