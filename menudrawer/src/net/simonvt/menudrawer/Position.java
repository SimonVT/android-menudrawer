package net.simonvt.menudrawer;

import android.util.SparseArray;

/**
 * Enums used for positioning the drawer.
 */
public enum Position {
    // Positions the drawer to the left of the content.
    LEFT(0),

    // Positions the drawer above the content.
    TOP(1),

    // Positions the drawer to the right of the content.
    RIGHT(2),

    // Positions the drawer below the content.
    BOTTOM(3),

    /**
     * Position the drawer at the start edge. This will position the drawer to the {@link #LEFT} with LTR languages and
     * {@link #RIGHT} with RTL languages.
     */
    START(4),

    /**
     * Position the drawer at the end edge. This will position the drawer to the {@link #RIGHT} with LTR languages and
     * {@link #LEFT} with RTL languages.
     */
    END(5);

    final int mValue;

    Position(int value) {
        mValue = value;
    }

    private static final SparseArray<Position> STRING_MAPPING = new SparseArray<Position>();

    static {
        for (Position via : Position.values()) {
            STRING_MAPPING.put(via.mValue, via);
        }
    }

    public static Position fromValue(int value) {
        return STRING_MAPPING.get(value);
    }
}
