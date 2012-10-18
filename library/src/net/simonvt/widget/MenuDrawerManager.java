package net.simonvt.widget;

import net.simonvt.menudrawer.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class MenuDrawerManager {

    private static final String STATE_LAYOUT = "net.simonvt.widget.MenuDrawerManager.layoutState";

    /**
     * The Activity the drawer is attached to.
     */
    private final Activity mActivity;

    /**
     * The MenuDrawer layout.
     */
    private MenuDrawer mMenuDrawer;

    /**
     * The container for the menu view.
     */
    private ViewGroup mMenuContainer;

    /**
     * The container for the content view.
     */
    private ViewGroup mContentContainer;

    /**
     * The custom menu view set by the user.
     */
    private View mMenuView;

    /**
     * The drag mode of the drawer. Can be either {@link MenuDrawer#MENU_DRAG_CONTENT}
     * or {@link MenuDrawer#MENU_DRAG_WINDOW}.
     */
    private int mDragMode = MenuDrawer.MENU_DRAG_CONTENT;

    /**
     * Indicates whether the menu view has been attached to the Activity's layout.
     */
    private boolean mMenuAttached;

    /**
     * The state passed to {@link #onRestoreDrawerState(Parcelable)}.
     */
    private Bundle mRestoredState;

    /**
     * Constructor to use when creating the menu drawer.
     *
     * @param activity The activity the menu drawer will be attached to.
     * @param dragMode The drag mode of the drawer. Can be either {@link MenuDrawer#MENU_DRAG_CONTENT}
     *                 or {@link MenuDrawer#MENU_DRAG_WINDOW}.
     */
    public MenuDrawerManager(Activity activity, int dragMode) {
        this(activity, dragMode, MenuDrawer.MENU_POSITION_LEFT);
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
        mActivity = activity;
        mDragMode = dragMode;

        mMenuDrawer = gravity == MenuDrawer.MENU_POSITION_RIGHT ? new RightDrawer(activity) : new LeftDrawer(activity);
        mMenuDrawer.setDragMode(dragMode);
        mMenuDrawer.setId(R.id.md__layout);
        mMenuContainer = (ViewGroup) mMenuDrawer.findViewById(R.id.md__menu);
        mContentContainer = (ViewGroup) mMenuDrawer.findViewById(R.id.md__content);

        attachMenuLayout();
    }

    /**
     * Attaches the menu drawer to the activity's layout.
     */
    private void attachMenuLayout() {
        if (!mMenuAttached) {
            mMenuAttached = true;

            switch (mDragMode) {
                case MenuDrawer.MENU_DRAG_CONTENT:
                    attachToContent();
                    break;

                case MenuDrawer.MENU_DRAG_WINDOW:
                    attachToDecor();
                    break;

                default:
                    throw new RuntimeException("Unknown menu mode: " + mDragMode);
            }
        }
    }

    /**
     * Attaches the menu drawer to the content view.
     */
    private void attachToContent() {
        /**
         * Do not call mActivity#setContentView.
         * E.g. if using with a ListActivity, Activity#setContentView is overridden and dispatched to
         * MenuDrawerManager#setContentView, which then again calls Activity#setContentView.
         */
        ViewGroup content = (ViewGroup) mActivity.findViewById(android.R.id.content);
        content.removeAllViews();
        content.addView(mMenuDrawer, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    /**
     * Attaches the menu drawer to the window.
     */
    private void attachToDecor() {
        ViewGroup decorView = (ViewGroup) mActivity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decorView.getChildAt(0);

        decorView.removeAllViews();
        decorView.addView(mMenuDrawer, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mContentContainer.addView(decorChild, decorChild.getLayoutParams());
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
     * Set the active view. If the mdArrowDrawable attribute is set, this View will have an arrow drawn next to it.
     *
     * @param v The active view.
     */
    public void setActiveView(View v) {
        setActiveView(v, 0);
    }

    /**
     * Set the active view. If the mdArrowDrawable attribute is set, this View will have an arrow drawn next to it.
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
        mMenuContainer.removeAllViews();
        mMenuView = mActivity.getLayoutInflater().inflate(layoutResId, mMenuContainer, false);
        mMenuContainer.addView(mMenuView);
    }

    /**
     * Set the menu view to an explicit view.
     *
     * @param view The menu view.
     */
    public void setMenuView(View view) {
        setMenuView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    /**
     * Set the menu view to an explicit view.
     *
     * @param view   The menu view.
     * @param params Layout parameters for the view.
     */
    public void setMenuView(View view, LayoutParams params) {
        mMenuView = view;
        mMenuContainer.removeAllViews();
        mMenuContainer.addView(view, params);
    }

    /**
     * Returns the menu view.
     *
     * @return The menu view.
     */
    public View getMenuView() {
        return mMenuView;
    }

    /**
     * Set the content from a layout resource.
     *
     * @param layoutResId Resource ID to be inflated.
     */
    public void setContentView(int layoutResId) {
        switch (mDragMode) {
            case MenuDrawer.MENU_DRAG_CONTENT:
                mContentContainer.removeAllViews();
                LayoutInflater inflater = mActivity.getLayoutInflater();
                inflater.inflate(layoutResId, mContentContainer, true);
                break;

            case MenuDrawer.MENU_DRAG_WINDOW:
                mActivity.setContentView(layoutResId);
                break;
        }
    }

    /**
     * Set the content to an explicit view.
     *
     * @param view The desired content to display.
     */
    public void setContentView(View view) {
        setContentView(view, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    /**
     * Set the content to an explicit view.
     *
     * @param view   The desired content to display.
     * @param params Layout parameters for the view.
     */
    public void setContentView(View view, LayoutParams params) {
        switch (mDragMode) {
            case MenuDrawer.MENU_DRAG_CONTENT:
                mContentContainer.removeAllViews();
                mContentContainer.addView(view, params);
                break;

            case MenuDrawer.MENU_DRAG_WINDOW:
                mActivity.setContentView(view, params);
                break;
        }
    }

    /**
     * Returns the views current state.
     *
     * @return Returns a Parcelable object containing the MenuDrawer's current state.
     */
    public Parcelable onSaveDrawerState() {
        Bundle state = new Bundle();
        state.putParcelable(STATE_LAYOUT, mMenuDrawer.saveState());
        return state;
    }

    /**
     * Called to restore the MenuDrawer's state that has previously been generated with {@link #onSaveDrawerState()}.
     *
     * @param in The state that had previously been returned by {@link #onSaveDrawerState()}.
     */
    public void onRestoreDrawerState(Parcelable in) {
        mRestoredState = (Bundle) in;
        mMenuDrawer.restoreState(mRestoredState.getParcelable(STATE_LAYOUT));
    }
}
