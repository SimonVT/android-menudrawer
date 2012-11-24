package net.simonvt.widget;

import android.app.Activity;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

/**
 * @deprecated Attach the MenuDrawer by calling {@link MenuDrawer#attach(android.app.Activity, int, int)} instead of
 *             using this class.
 */
public class MenuDrawerManager {

    /**
     * The MenuDrawer layout.
     */
    private MenuDrawer mMenuDrawer;

    /**
     * Constructor to use when creating the menu drawer.
     *
     * @param activity The activity the menu drawer will be attached to.
     * @param dragMode The drag mode of the drawer. Can be either {@link MenuDrawer#MENU_DRAG_CONTENT}
     *                 or {@link MenuDrawer#MENU_DRAG_WINDOW}.
     */
    public MenuDrawerManager(Activity activity, int dragMode) {
        mMenuDrawer = MenuDrawer.attach(activity, dragMode);
    }

    /**
     * Constructor to use when creating the menu drawer.
     *
     * @param activity The activity the menu drawer will be attached to.
     * @param dragMode The drag mode of the drawer. Can be either {@link MenuDrawer#MENU_DRAG_CONTENT}
     *                 or {@link MenuDrawer#MENU_DRAG_WINDOW}.
     * @param gravity  Where to position the menu. Can be either {@link MenuDrawer#MENU_POSITION_LEFT} or
     *                 {@link MenuDrawer#MENU_POSITION_RIGHT}.
     */
    public MenuDrawerManager(Activity activity, int dragMode, int gravity) {
        mMenuDrawer = MenuDrawer.attach(activity, dragMode, gravity);
    }

    /**
     * Returns the MenuDrawer layout.
     *
     * @return The MenuDrawer layout.
     */
    public MenuDrawer getMenuDrawer() {
        return mMenuDrawer;
    }

    /**
     * Set the active view.
     * If the mdActiveIndicator attribute is set, this View will have the indicator drawn next to it.
     *
     * @param v The active view.
     */
    public void setActiveView(View v) {
        mMenuDrawer.setActiveView(v);
    }

    /**
     * Set the active view.
     * If the mdActiveIndicator attribute is set, this View will have the indicator drawn next to it.
     *
     * @param v        The active view.
     * @param position Optional position, usually used with ListView. v.setTag(R.id.mdActiveViewPosition, position)
     *                 must be called first.
     */
    public void setActiveView(View v, int position) {
        mMenuDrawer.setActiveView(v, position);
    }

    /**
     * Toggles the menu open and close.
     */
    public void toggleMenu() {
        mMenuDrawer.toggleMenu();
    }

    /**
     * Toggles the menu open and close.
     *
     * @param animate Whether open/close should be animated.
     */
    public void toggleMenu(boolean animate) {
        mMenuDrawer.toggleMenu(animate);
    }

    /**
     * Opens the menu.
     */
    public void openMenu() {
        mMenuDrawer.openMenu();
    }

    /**
     * Opens the menu.
     *
     * @param animate Whether open/close should be animated.
     */
    public void openMenu(boolean animate) {
        mMenuDrawer.openMenu(animate);
    }

    /**
     * Closes the menu.
     */
    public void closeMenu() {
        mMenuDrawer.closeMenu();
    }

    /**
     * Closes the menu.
     *
     * @param animate Whether open/close should be animated.
     */
    public void closeMenu(boolean animate) {
        mMenuDrawer.closeMenu(animate);
    }

    /**
     * Indicates whether the menu is currently visible.
     *
     * @return True if the menu is open, false otherwise.
     */
    public boolean isMenuVisible() {
        return mMenuDrawer.isMenuVisible();
    }

    /**
     * Returns the state of the drawer. Can be one of {@link MenuDrawer#STATE_CLOSED}, {@link MenuDrawer#STATE_CLOSING},
     * {@link MenuDrawer#STATE_DRAGGING}, {@link MenuDrawer#STATE_OPENING} or {@link MenuDrawer#STATE_OPEN}.
     *
     * @return The drawers state.
     */
    public int getDrawerState() {
        return mMenuDrawer.getDrawerState();
    }

    /**
     * Set the menu view from a layout resource.
     *
     * @param layoutResId Resource ID to be inflated.
     */
    public void setMenuView(int layoutResId) {
        mMenuDrawer.setMenuView(layoutResId);
    }

    /**
     * Set the menu view to an explicit view.
     *
     * @param view The menu view.
     */
    public void setMenuView(View view) {
        mMenuDrawer.setMenuView(view);
    }

    /**
     * Set the menu view to an explicit view.
     *
     * @param view   The menu view.
     * @param params Layout parameters for the view.
     */
    public void setMenuView(View view, LayoutParams params) {
        mMenuDrawer.setMenuView(view, params);
    }

    /**
     * Returns the menu view.
     *
     * @return The menu view.
     */
    public View getMenuView() {
        return mMenuDrawer.getMenuView();
    }

    /**
     * Set the content from a layout resource.
     *
     * @param layoutResId Resource ID to be inflated.
     */
    public void setContentView(int layoutResId) {
        mMenuDrawer.setContentView(layoutResId);
    }

    /**
     * Set the content to an explicit view.
     *
     * @param view The desired content to display.
     */
    public void setContentView(View view) {
        mMenuDrawer.setContentView(view);
    }

    /**
     * Set the content to an explicit view.
     *
     * @param view   The desired content to display.
     * @param params Layout parameters for the view.
     */
    public void setContentView(View view, LayoutParams params) {
        mMenuDrawer.setContentView(view, params);
    }

    /**
     * Returns the views current state.
     *
     * @return Returns a Parcelable object containing the MenuDrawer's current state.
     * @deprecated Use {@link net.simonvt.widget.MenuDrawer#saveState()} instead.
     */
    public Parcelable onSaveDrawerState() {
        return mMenuDrawer.saveState();
    }

    /**
     * Called to restore the MenuDrawer's state that has previously been generated with {@link #onSaveDrawerState()}.
     *
     * @param in The state that had previously been returned by {@link #onSaveDrawerState()}.
     * @deprecated Use {@link MenuDrawer#restoreState(android.os.Parcelable)} instead.
     */
    public void onRestoreDrawerState(Parcelable in) {
        mMenuDrawer.restoreState(in);
    }
}
