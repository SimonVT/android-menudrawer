package net.simonvt.menudrawer.compat;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

public final class ActionBarHelper {

    private static final String TAG = "ActionBarHelper";

    static final boolean DEBUG = false;

    private Activity mActivity;

    private Object mIndicatorInfo;

    private boolean mUsesCompat;

    public ActionBarHelper(Activity activity) {
        mActivity = activity;

        try {
            Class clazz = activity.getClass();
            Method m = clazz.getMethod("getSupportActionBar");
            mUsesCompat = true;
        } catch (NoSuchMethodException e) {
            if (DEBUG) {
                Log.e(TAG,
                        "Activity " + activity.getClass().getSimpleName() + " does not use a compatibility action bar",
                        e);
            }
        }

        mIndicatorInfo = getIndicatorInfo();
    }

    private Object getIndicatorInfo() {
        if (mUsesCompat && Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return ActionBarHelperCompat.getIndicatorInfo(mActivity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return ActionBarHelperNative.getIndicatorInfo(mActivity);
        }

        return null;
    }

    public void setActionBarUpIndicator(Drawable drawable, int contentDesc) {
        if (mUsesCompat && Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ActionBarHelperCompat.setActionBarUpIndicator(mIndicatorInfo, mActivity, drawable, contentDesc);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBarHelperNative.setActionBarUpIndicator(mIndicatorInfo, mActivity, drawable, contentDesc);
        }
    }

    public void setActionBarDescription(int contentDesc) {
        if (mUsesCompat && Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ActionBarHelperCompat.setActionBarDescription(mIndicatorInfo, mActivity, contentDesc);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBarHelperNative.setActionBarDescription(mIndicatorInfo, mActivity, contentDesc);
        }
    }

    public Drawable getThemeUpIndicator() {
        if (mUsesCompat && Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return ActionBarHelperCompat.getThemeUpIndicator(mIndicatorInfo);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return ActionBarHelperNative.getThemeUpIndicator(mIndicatorInfo, mActivity);
        }

        return null;
    }

    public void setDisplayShowHomeAsUpEnabled(boolean enabled) {
        if (mUsesCompat && Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ActionBarHelperCompat.setDisplayHomeAsUpEnabled(mIndicatorInfo, enabled);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBarHelperNative.setDisplayHomeAsUpEnabled(mActivity, enabled);
        }
    }
}
