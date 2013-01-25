package net.simonvt.menudrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

public class RightStaticDrawer extends StaticDrawer {

    RightStaticDrawer(Activity activity, int dragMode) {
        super(activity, dragMode);
    }

    public RightStaticDrawer(Context context) {
        super(context);
    }

    public RightStaticDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RightStaticDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initDrawer(Context context, AttributeSet attrs, int defStyle) {
        super.initDrawer(context, attrs, defStyle);
        mPosition = Position.RIGHT;
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
}
