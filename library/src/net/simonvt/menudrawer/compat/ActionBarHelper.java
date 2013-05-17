package net.simonvt.menudrawer.compat;

import net.simonvt.menudrawer.BuildConfig;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

public final class ActionBarHelper {

    private static final String TAG = "ActionBarHelper";

    private Activity mActivity;

    private Object mIndicatorInfo;

    private boolean mUsesSherlock;

    public ActionBarHelper(Activity activity) {
        mActivity = activity;

        try {
            Class clazz = activity.getClass();
            Method m = clazz.getMethod("getSupportActionBar");
            mUsesSherlock = true;
        } catch (NoSuchMethodException e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Activity " + activity.getClass().getSimpleName() + " does not use ActionBarSherlock", e);
            }
        }

        mIndicatorInfo = getIndicatorInfo();
    }

    private Object getIndicatorInfo() {
        if (mUsesSherlock && Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return ActionBarHelperSherlock.getIndicatorInfo(mActivity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return ActionBarHelperNative.getIndicatorInfo(mActivity);
        }

        return null;
    }

    public void setActionBarUpIndicator(Drawable drawable, int contentDesc) {
        if (mUsesSherlock && Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ActionBarHelperSherlock.setActionBarUpIndicator(mIndicatorInfo, mActivity, drawable, contentDesc);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBarHelperNative.setActionBarUpIndicator(mIndicatorInfo, mActivity, drawable, contentDesc);
        }
    }

    public void setActionBarDescription(int contentDesc) {
        if (mUsesSherlock && Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ActionBarHelperSherlock.setActionBarDescription(mIndicatorInfo, mActivity, contentDesc);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBarHelperNative.setActionBarDescription(mIndicatorInfo, mActivity, contentDesc);
        }
    }

    public Drawable getThemeUpIndicator() {
        if (mUsesSherlock && Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return ActionBarHelperSherlock.getThemeUpIndicator(mIndicatorInfo);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return ActionBarHelperNative.getThemeUpIndicator(mIndicatorInfo, mActivity);
        }

        return null;
    }

    public void setDisplayShowHomeAsUpEnabled(boolean enabled) {
        if (mUsesSherlock && Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ActionBarHelperSherlock.setDisplayHomeAsUpEnabled(mIndicatorInfo, enabled);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBarHelperNative.setDisplayHomeAsUpEnabled(mActivity, enabled);
        }
    }
}
