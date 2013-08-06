package net.simonvt.menudrawer.compat;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Method;

final class ActionBarHelperNative {

    private static final String TAG = "ActionBarHelperNative";

    private ActionBarHelperNative() {
    }

    private static final int[] THEME_ATTRS = new int[] {
            android.R.attr.homeAsUpIndicator
    };

    public static void setActionBarUpIndicator(Object info, Activity activity, Drawable drawable, int contentDescRes) {

        final SetIndicatorInfo sii = (SetIndicatorInfo) info;
        if (sii.setHomeAsUpIndicator != null) {
            try {
                final ActionBar actionBar = activity.getActionBar();
                sii.setHomeAsUpIndicator.invoke(actionBar, drawable);
                sii.setHomeActionContentDescription.invoke(actionBar, contentDescRes);
            } catch (Throwable t) {
                if (ActionBarHelper.DEBUG) Log.e(TAG, "Couldn't set home-as-up indicator via JB-MR2 API", t);
            }
        } else if (sii.upIndicatorView != null) {
            sii.upIndicatorView.setImageDrawable(drawable);
        } else {
            if (ActionBarHelper.DEBUG) Log.e(TAG, "Couldn't set home-as-up indicator");
        }
    }

    public static void setActionBarDescription(Object info, Activity activity, int contentDescRes) {
        final SetIndicatorInfo sii = (SetIndicatorInfo) info;
        if (sii.setHomeAsUpIndicator != null) {
            try {
                final ActionBar actionBar = activity.getActionBar();
                sii.setHomeActionContentDescription.invoke(actionBar, contentDescRes);
            } catch (Throwable t) {
                if (ActionBarHelper.DEBUG) Log.e(TAG, "Couldn't set content description via JB-MR2 API", t);
            }
        }
    }

    public static Drawable getThemeUpIndicator(Object info, Activity activity) {
        final TypedArray a = activity.obtainStyledAttributes(THEME_ATTRS);
        final Drawable result = a.getDrawable(0);
        a.recycle();
        return result;
    }

    public static Object getIndicatorInfo(Activity activity) {
        return new SetIndicatorInfo(activity);
    }

    public static void setDisplayHomeAsUpEnabled(Activity activity, boolean b) {
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(b);
        }
    }

    private static class SetIndicatorInfo {

        public Method setHomeAsUpIndicator;
        public Method setHomeActionContentDescription;
        public ImageView upIndicatorView;

        SetIndicatorInfo(Activity activity) {
            try {
                setHomeAsUpIndicator = ActionBar.class.getDeclaredMethod("setHomeAsUpIndicator", Drawable.class);
                setHomeActionContentDescription = ActionBar.class.getDeclaredMethod(
                        "setHomeActionContentDescription", Integer.TYPE);

                // If we got the method we won't need the stuff below.
                return;
            } catch (Throwable t) {
                // Oh well. We'll use the other mechanism below instead.
            }

            final View home = activity.findViewById(android.R.id.home);
            if (home == null) {
                // Action bar doesn't have a known configuration, an OEM messed with things.
                return;
            }

            final ViewGroup parent = (ViewGroup) home.getParent();
            final int childCount = parent.getChildCount();
            if (childCount != 2) {
                // No idea which one will be the right one, an OEM messed with things.
                return;
            }

            final View first = parent.getChildAt(0);
            final View second = parent.getChildAt(1);
            final View up = first.getId() == android.R.id.home ? second : first;

            if (up instanceof ImageView) {
                // Jackpot! (Probably...)
                upIndicatorView = (ImageView) up;
            }
        }
    }
}
