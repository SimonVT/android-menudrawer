package net.simonvt.menudrawer.samples;

import net.simonvt.widget.MenuDrawer;
import net.simonvt.widget.MenuDrawerManager;

import android.app.ListActivity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ListActivitySample extends ListActivity {

    private static final String STATE_MENUDRAWER = "net.simonvt.menudrawer.samples.ListActivitySample.menuDrawer";

    private MenuDrawerManager mMenuDrawer;

    private Handler mHandler;
    private Runnable mToggleUpRunnable;
    private boolean mDisplayUp = true;

    @Override
    public void onCreate(Bundle inState) {
        super.onCreate(inState);

        mMenuDrawer = new MenuDrawerManager(this, MenuDrawer.MENU_DRAG_CONTENT);

        TextView menuView = new TextView(this);
        menuView.setGravity(Gravity.CENTER);
        menuView.setTextColor(0xFFFFFFFF);
        final int padding = dpToPx(16);
        menuView.setPadding(padding, padding, padding, padding);
        menuView.setText(R.string.sample_listactivity);
        mMenuDrawer.setMenuView(menuView);
        mMenuDrawer.getMenuDrawer().setOffsetMenuEnabled(false);

        List<String> items = new ArrayList<String>();
        for (int i = 1; i <= 20; i++) {
            items.add("Item " + i);
        }

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));

        // When running on ICS or higher, the "up" button blinks until it is clicked.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mHandler = new Handler();
            getActionBar().setDisplayHomeAsUpEnabled(true);
            mToggleUpRunnable = new Runnable() {
                @Override
                public void run() {
                    mDisplayUp = !mDisplayUp;
                    getActionBar().setDisplayHomeAsUpEnabled(mDisplayUp);
                    mHandler.postDelayed(mToggleUpRunnable, 500);
                }
            };

            mHandler.postDelayed(mToggleUpRunnable, 500);

            mMenuDrawer.getMenuDrawer().setOnDrawerStateChangeListener(new MenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (newState == MenuDrawer.STATE_OPEN) {
                        mHandler.removeCallbacks(mToggleUpRunnable);
                        if (!mDisplayUp) getActionBar().setDisplayHomeAsUpEnabled(true);
                        mMenuDrawer.getMenuDrawer().setOnDrawerStateChangeListener(null);
                    }
                }
            });
        }
    }

    private int dpToPx(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    @Override
    public void setContentView(int layoutResID) {
        // This override is only needed when using MENU_DRAG_CONTENT.
        mMenuDrawer.setContentView(layoutResID);
        onContentChanged();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String str = (String) getListAdapter().getItem(position);
        Toast.makeText(this, "Clicked: " + str, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mMenuDrawer.onRestoreDrawerState(inState.getParcelable(STATE_MENUDRAWER));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_MENUDRAWER, mMenuDrawer.onSaveDrawerState());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mMenuDrawer.toggleMenu();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        final int drawerState = mMenuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mMenuDrawer.closeMenu();
            return;
        }

        super.onBackPressed();
    }
}
