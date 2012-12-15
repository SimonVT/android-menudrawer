package net.simonvt.menudrawer.samples;

import net.simonvt.widget.MenuDrawer;

import android.app.Activity;
import android.os.Bundle;

public class LayoutSample extends Activity {

    private static final String TAG = "LayoutSample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layoutsample);
        MenuDrawer md = (MenuDrawer) findViewById(R.id.drawer);
        md.setTouchMode(MenuDrawer.TOUCH_MODE_FULLSCREEN);
    }
}
