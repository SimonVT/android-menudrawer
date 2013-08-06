Change Log
==========

Version 3.0.4 *(2013-08-06)*
----------------------------
 * Push aar to maven central
 * Support AppCompat up button
 * Fix: Dropshadow on static drawer was broken

Version 3.0.2 *(2013-06-17)*
----------------------------
 * Menu size now to defaults to 240dp
 * Overlay drawer is revealed when the edge is touched
 * Fix: MethodNotFoundException on api7
 * Fix: Adding a drawer in xml was broken

Version 3.0.1 *(2013-06-10)*
----------------------------
 * Fix: Overlay drawer was broken on pre-ICS devices

Version 3.0.0 *(2013-06-09)*
----------------------------
 * Added drawer that overlays the content
 * Removed most MenuDrawer subclasses.
   Only three exist now:
     * SlidingDrawer - A drawer that's behind the content
     * OverlayDrawer - A drawer that overlays the content
     * StaticDrawer  - The menu is always visible

   The drawers are selected by passing a Type to the `MenuDrawer#attach` method.
   When adding drawers in xml, put one of these subclasses and add the
   `mdPosition` attribute.
 * Added method to disable the overlay that's drawn when the drawer is dragged
 * Relocated the library to `net.simonvt.menudrawer:menudrawer` in maven central

Version 2.0.3 *(2013-05-19)*
----------------------------
 * Add drawer indicator, as per the design guidelines
 * Add new method to drawer listener that notifies about offset changes

Version 2.0.2 *(2013-03-31)*
----------------------------
 * Added listener that makes it possible to disabllow intercepting touch events over
   certain views
 * Added setter for the maximum animation duration
 * Added getter for menu size
 * Added methods that enable/disable indicator animation
 * Fix: Removed log statements
 * Fix: Drawing the active indicator might cause crash if the active view is not a
        child of the MenuDrawer
 * Fix: Crash in static drawer if no active indicator bitmap was set

Version 2.0.1 *(2013-02-12)*
----------------------------
 * Indicator now animates between active views
 * Fixed restoring state for right/bottom drawer

Version 2.0.0 *(2013-01-23)*
----------------------------

 * Major API changes

    * All classes are now in the net.simonvt.menudrawer package.
    * MenuDrawerManager no longet exists. Menu is added with MenuDrawer#attach(...).
    * Drawer position is now selected with Position enums instead of int constants.
    * Width methods/attributes have been renamed to 'size'.

 * Added top/bottom drawer.
 * Added static (non-draggable, always visible) drawers.
 * The touch bezel size is now configurable with MenuDrawer#setTouchBezelSize(int).
 * MenuDrawer#saveState() now only required when dragging the entire window.
 * Drawers can now be used in XML layouts.
 * Fix: Scroller class caused conflicts with other libraries.
 * Fix: No more overdraw when the drawer is closed.
 * Fix: Content no longer falls behind when slowly dragging.


Version 1.0.0 *(2012-10-30)*
----------------------------

Initial release.
