package net.simonvt.menudrawer.samples;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class MenuScrollView extends ScrollView {

    public interface OnScrollChangedListener {

        void onScrollChanged();
    }

    private OnScrollChangedListener mOnScrollChangedListener;

    public MenuScrollView(Context context) {
        super(context);
    }

    public MenuScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (mOnScrollChangedListener != null) mOnScrollChangedListener.onScrollChanged();
    }

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mOnScrollChangedListener = listener;
    }
}
