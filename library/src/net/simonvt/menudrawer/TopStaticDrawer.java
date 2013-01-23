package net.simonvt.menudrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

public class TopStaticDrawer extends StaticDrawer {

    TopStaticDrawer(Activity activity, int dragMode) {
        super(activity, dragMode, Position.TOP);
    }

    public TopStaticDrawer(Context context) {
        super(context);
    }

    public TopStaticDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TopStaticDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setDropShadowColor(int color) {
        final int endColor = color & 0x00FFFFFF;
        mDropShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[] {
                color,
                endColor,
        });
        invalidate();
    }
}
