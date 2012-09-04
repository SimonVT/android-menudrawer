package net.simonvt.menudrawer.samples;

import net.simonvt.widget.MenuDrawer;
import net.simonvt.widget.MenuDrawerManager;

import android.app.ListActivity;
import android.os.Bundle;
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

    @Override
    public void onCreate(Bundle inState) {
        super.onCreate(inState);

        mMenuDrawer = new MenuDrawerManager(this, MenuDrawer.MENU_DRAG_CONTENT);

        TextView menuView = new TextView(this);
        menuView.setGravity(Gravity.CENTER);
        menuView.setTextColor(0xFFFFFFFF);
        menuView.setText("MENU");
        mMenuDrawer.setMenuView(menuView);
        mMenuDrawer.getMenuDrawer().setOffsetMenuEnabled(false);

        List<String> items = new ArrayList<String>();
        for (int i = 1; i <= 20; i++) {
            items.add("Item " + i);
        }

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
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
