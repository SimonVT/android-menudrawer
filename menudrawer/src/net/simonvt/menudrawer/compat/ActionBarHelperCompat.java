package net.simonvt.menudrawer.compat;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Method;

final class ActionBarHelperCompat {

    private static final String TAG = "ActionBarHelperCompat";

    private ActionBarHelperCompat() {
    }

    public static void setActionBarUpIndicator(Object info, Activity activity, Drawable drawable, int contentDescRes) {
        final SetIndicatorInfo sii = (SetIndicatorInfo) info;
        if (sii.mUpIndicatorView != null) {
            sii.mUpIndicatorView.setImageDrawable(drawable);
            final String contentDescription = contentDescRes == 0 ? null : activity.getString(contentDescRes);
            sii.mUpIndicatorView.setContentDescription(contentDescription);
        }
    }

    public static void setActionBarDescription(Object info, Activity activity, int contentDescRes) {
        final SetIndicatorInfo sii = (SetIndicatorInfo) info;
        if (sii.mUpIndicatorView != null) {
            final String contentDescription = contentDescRes == 0 ? null : activity.getString(contentDescRes);
            sii.mUpIndicatorView.setContentDescription(contentDescription);
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
                if (ActionBarHelper.DEBUG) {
                    Log.e(TAG, "Unable to call setHomeAsUpEnabled", t);
                }
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

                try {
                    // Attempt to find ActionBarSherlock up indicator
                    final int homeId = activity.getResources().getIdentifier("abs__home", "id", appPackage);
                    View v = activity.findViewById(homeId);
                    ViewGroup parent = (ViewGroup) v.getParent();
                    final int upId = activity.getResources().getIdentifier("abs__up", "id", appPackage);
                    mUpIndicatorView = (ImageView) parent.findViewById(upId);
                } catch (Throwable t) {
                    if (ActionBarHelper.DEBUG) {
                        Log.e(TAG, "ABS action bar not found", t);
                    }
                }

                if (mUpIndicatorView == null) {
                    // Attempt to find AppCompat up indicator
                    final int homeId = activity.getResources().getIdentifier("home", "id", appPackage);
                    View v = activity.findViewById(homeId);
                    ViewGroup parent = (ViewGroup) v.getParent();
                    final int upId = activity.getResources().getIdentifier("up", "id", appPackage);
                    mUpIndicatorView = (ImageView) parent.findViewById(upId);
                }

                Class supportActivity = activity.getClass();
                Method getActionBar = supportActivity.getMethod("getSupportActionBar");

                mActionBar = getActionBar.invoke(activity, null);
                Class supportActionBar = mActionBar.getClass();
                mHomeAsUpEnabled = supportActionBar.getMethod("setDisplayHomeAsUpEnabled", Boolean.TYPE);

            } catch (Throwable t) {
                if (ActionBarHelper.DEBUG) {
                    Log.e(TAG, "Unable to init SetIndicatorInfo for ABS", t);
                }
            }
        }
    }
}
