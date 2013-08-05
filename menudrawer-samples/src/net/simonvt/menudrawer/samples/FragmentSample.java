package net.simonvt.menudrawer.samples;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentSample extends BaseListSample {

    private static final String STATE_CURRENT_FRAGMENT = "net.simonvt.menudrawer.samples.FragmentSample";

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private String mCurrentFragmentTag;

    @Override
    protected void onCreate(Bundle inState) {
        super.onCreate(inState);

        mFragmentManager = getSupportFragmentManager();

        if (inState != null) {
            mCurrentFragmentTag = inState.getString(STATE_CURRENT_FRAGMENT);
        } else {
            mCurrentFragmentTag = ((Item) mAdapter.getItem(0)).mTitle;
            attachFragment(mMenuDrawer.getContentContainer().getId(), getFragment(mCurrentFragmentTag),
                    mCurrentFragmentTag);
            commitTransactions();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mMenuDrawer.setOnDrawerStateChangeListener(new MenuDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == MenuDrawer.STATE_CLOSED) {
                    commitTransactions();
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {
                // Do nothing
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CURRENT_FRAGMENT, mCurrentFragmentTag);
    }

    @Override
    protected void onMenuItemClicked(int position, Item item) {
        if (mCurrentFragmentTag != null) detachFragment(getFragment(mCurrentFragmentTag));
        attachFragment(mMenuDrawer.getContentContainer().getId(), getFragment(item.mTitle), item.mTitle);
        mCurrentFragmentTag = item.mTitle;
        mMenuDrawer.closeMenu();
    }

    @Override
    protected int getDragMode() {
        return MenuDrawer.MENU_DRAG_WINDOW;
    }

    @Override
    protected Position getDrawerPosition() {
        return Position.LEFT;
    }

    protected FragmentTransaction ensureTransaction() {
        if (mFragmentTransaction == null) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }

        return mFragmentTransaction;
    }

    private Fragment getFragment(String tag) {
        Fragment f = mFragmentManager.findFragmentByTag(tag);

        if (f == null) {
            f = SampleFragment.newInstance(tag);
        }

        return f;
    }

    protected void attachFragment(int layout, Fragment f, String tag) {
        if (f != null) {
            if (f.isDetached()) {
                ensureTransaction();
                mFragmentTransaction.attach(f);
            } else if (!f.isAdded()) {
                ensureTransaction();
                mFragmentTransaction.add(layout, f, tag);
            }
        }
    }

    protected void detachFragment(Fragment f) {
        if (f != null && !f.isDetached()) {
            ensureTransaction();
            mFragmentTransaction.detach(f);
        }
    }

    protected void commitTransactions() {
        if (mFragmentTransaction != null && !mFragmentTransaction.isEmpty()) {
            mFragmentTransaction.commit();
            mFragmentTransaction = null;
        }
    }

    public static class SampleFragment extends Fragment {

        private static final String ARG_TEXT = "net.simonvt.menudrawer.samples.SampleFragment.text";

        public static SampleFragment newInstance(String text) {
            SampleFragment f = new SampleFragment();

            Bundle args = new Bundle();
            args.putString(ARG_TEXT, text);
            f.setArguments(args);

            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_sample, container, false);

            ((TextView) v.findViewById(R.id.text)).setText(getArguments().getString(ARG_TEXT));

            return v;
        }
    }
}
