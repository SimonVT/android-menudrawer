package net.simonvt.menudrawer.compat;

import net.simonvt.menudrawer.BuildConfig;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Method;

final class ActionBarHelperSherlock {

    private static final String TAG = "ActionBarHelperSherlock";

    private ActionBarHelperSherlock() {
    }

    public static void setActionBarUpIndicator(Object info, Activity activity, Drawable drawable, int contentDescRes) {
        final SetIndicatorInfo sii = (SetIndicatorInfo) info;
        if (sii.mUpIndicatorView != null) {
            sii.mUpIndicatorView.setImageDrawable(drawable);
            Drawable d = sii.mUpIndicatorView.getDrawable();
            sii.mUpIndicatorView.setContentDescription(activity.getString(contentDescRes));
        }
    }

    public static void setActionBarDescription(Object info, Activity activity, int contentDescRes) {
        final SetIndicatorInfo sii = (SetIndicatorInfo) info;
        if (sii.mUpIndicatorView != null) {
            sii.mUpIndicatorView.setContentDescription(activity.getString(contentDescRes));
        }
    }

    public static Drawable getThemeUpIndicator(Object info) {
        final SetIndicatorInfo sii = (SetIndicatorInfo) info;
        if (sii.mUpIndicatorView != null) {
            return sii.mUpIndicatorView.getDrawable();
        }
        return null;
    }

    public static Object getIndicatorInfo(Activity activity) {
        return new SetIndicatorInfo(activity);
    }

    public static void setDisplayHomeAsUpEnabled(Object info, boolean enabled) {
        final SetIndicatorInfo sii = (SetIndicatorInfo) info;
        if (sii.mHomeAsUpEnabled != null) {
            try {
                sii.mHomeAsUpEnabled.invoke(sii.mActionBar, enabled);
            } catch (Throwable t) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Unable to call setHomeAsUpEnabled", t);
            }
        }
    }

    private static class SetIndicatorInfo {

        public ImageView mUpIndicatorView;
        public Object mActionBar;
        public Method mHomeAsUpEnabled;

        SetIndicatorInfo(Activity activity) {
            try {
                String appPackage = activity.getPackageName();
                final int homeId = activity.getResources().getIdentifier("abs__home", "id", appPackage);
                View v = activity.findViewById(homeId);
                ViewGroup parent = (ViewGroup) v.getParent();
                final int upId = activity.getResources().getIdentifier("abs__up", "id", appPackage);
                mUpIndicatorView = (ImageView) parent.findViewById(upId);
                Log.d(TAG, "Id: " + upId);

                Class sherlockActivity = activity.getClass();
                Method getActionBar = sherlockActivity.getMethod("getSupportActionBar");

                mActionBar = getActionBar.invoke(activity, null);
                Class supportActionBar = mActionBar.getClass();
                mHomeAsUpEnabled = supportActionBar.getMethod("setDisplayHomeAsUpEnabled", Boolean.TYPE);

            } catch (Throwable t) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Unable to init SetIndicatorInfo for ABS", t);
            }
        }
    }
}
