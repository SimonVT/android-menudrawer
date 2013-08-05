package net.simonvt.menudrawer.samples;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import net.simonvt.menudrawer.MenuDrawer;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

public class ActionBarSherlockSample extends SherlockActivity {

    private static final String TAG = "ActionBarSherlockSample";

    private MenuDrawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar();

        mDrawer = MenuDrawer.attach(this);

        TextView menuView = new TextView(this);
        menuView.setTextColor(0xFFFFFFFF);
        menuView.setText("As the drawer opens, the drawer indicator icon becomes smaller.");
        menuView.setGravity(Gravity.CENTER);
        mDrawer.setMenuView(menuView);

        TextView contentView = new TextView(this);
        contentView.setText(
                "This sample uses ActionBarSherlock to display an ActionBar on older platforms. The drawer indicator, "
                        + "as per the design guidelines, is visible in the top left corner.");
        contentView.setGravity(Gravity.CENTER);
        mDrawer.setContentView(contentView);

        // The drawable that replaces the up indicator in the action bar
        mDrawer.setSlideDrawable(R.drawable.ic_drawer);
        // Whether the previous drawable should be shown
        mDrawer.setDrawerIndicatorEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.toggleMenu();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
