package net.simonvt.menudrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

public class LeftStaticDrawer extends StaticDrawer {

    LeftStaticDrawer(Activity activity, int dragMode) {
        super(activity, dragMode, Position.LEFT);
    }

    public LeftStaticDrawer(Context context) {
        super(context);
    }

    public LeftStaticDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LeftStaticDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setDropShadowColor(int color) {
        final int endColor = color & 0x00FFFFFF;
        mDropShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[] {
                color,
                endColor,
        });
        invalidate();
    }
}
